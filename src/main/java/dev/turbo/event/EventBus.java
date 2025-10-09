package dev.turbo.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe event bus for registering listeners and posting events.
 */
public class EventBus {
    private static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());
    
    private final Map<Class<? extends Event>, List<BaseListener<?>>> listeners = new ConcurrentHashMap<>();
    private final Map<Class<? extends Event>, Boolean> cancellableCache = new ConcurrentHashMap<>();
    private final Set<Object> registered = ConcurrentHashMap.newKeySet();

    public void register(Object listener) {
        if (registered.contains(listener)) return;
        registered.add(listener);

        for (Method method : listener.getClass().getDeclaredMethods()) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null) continue;

            if (method.getParameterCount() != 1) {
                throw new InvalidListenerException(method);
            }

            Class<?> paramType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(paramType)) {
                throw new InvalidListenerException(method);
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) paramType;

            Listener<?> listenerWrapper = new Listener<>(listener, method, annotation.priority(), eventType);
            addListener(eventType, listenerWrapper);
        }
    }

    public void unregister(Object listener) {
        if (!registered.remove(listener)) return;

        listeners.values().forEach(list -> list.removeIf(l -> {
            if (l instanceof Listener<?> annotationListener) {
                return annotationListener.instance == listener;
            }
            return false;
        }));
        listeners.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public <T extends Event> Subscription subscribe(Class<T> eventType, Consumer<T> handler) {
        return subscribe(eventType, handler, EventPriority.NORMAL);
    }

    public <T extends Event> Subscription subscribe(Class<T> eventType, Consumer<T> handler, int priority) {
        LambdaListener<T> listener = new LambdaListener<>(handler, priority, eventType);
        addListener(eventType, listener);

        return new Subscription() {
            private boolean active = true;

            @Override
            public void unsubscribe() {
                if (!active) return;
                active = false;
                removeListener(eventType, listener);
            }

            @Override
            public boolean isActive() {
                return active;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> T post(T event) {
        Class<?> eventClass = event.getClass();
        boolean isCancellable = isCancellable(eventClass);
        
        for (Class<?> type : getEventHierarchy(eventClass)) {
            List<BaseListener<?>> eventListeners = listeners.get(type);
            if (eventListeners == null) continue;

            for (BaseListener<?> listener : eventListeners) {
                try {
                    ((BaseListener<T>) listener).invoke(event); // possible runtime CCE here
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error invoking listener for " + eventClass.getSimpleName(), e);
                }
                
                if (isCancellable && ((Cancellable) event).isCancelled()) {
                    return event;
                }
            }
        }

        return event;
    }

    public void clear() {
        listeners.clear();
        cancellableCache.clear();
        registered.clear();
    }
    
    private void addListener(Class<? extends Event> eventType, BaseListener<?> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        listeners.get(eventType).sort(null); // very goood thread safety bro very good
        // TODO: fix
    }
    
    private void removeListener(Class<? extends Event> eventType, BaseListener<?> listener) {
        List<BaseListener<?>> list = listeners.get(eventType);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) listeners.remove(eventType);
        }
    }
    
    private boolean isCancellable(Class<?> eventClass) {
        return cancellableCache.computeIfAbsent(
            (Class<? extends Event>) eventClass,
            k -> Cancellable.class.isAssignableFrom(k)
        );
    }
    
    private List<Class<?>> getEventHierarchy(Class<?> eventClass) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = eventClass;
        
        while (Event.class.isAssignableFrom(current)) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }
        
        return hierarchy;
    }
}

