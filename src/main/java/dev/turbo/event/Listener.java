package dev.turbo.event;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Annotation-based event listener implementation using reflection.
 */
class Listener<T extends Event> implements BaseListener<T> {
    final Object instance;
    final Method method;
    private final int priority;
    private final Class<T> eventType;
    private final Consumer<T> invoker;

    Listener(Object instance, Method method, int priority, Class<T> eventType) {
        this.instance = instance;
        this.method = method;
        this.priority = priority;
        this.eventType = eventType;
        this.invoker = createInvoker();
    }

    @SuppressWarnings("unchecked")
    private Consumer<T> createInvoker() {
        try {
            method.setAccessible(true); // TODO: fix this for java 9+
            return event -> {
                try {
                    method.invoke(instance, event);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke listener", e);
                }
            };
        } catch (Exception e) { // tf is this try-catch doing brotha?
            throw new InvalidListenerException(method);
        }
    }

    @Override
    public void invoke(T event) {
        invoker.accept(event);
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    @Override
    public Class<T> getEventType() {
        return eventType;
    }
}

