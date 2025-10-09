package dev.turbo.event;

import java.util.function.Consumer;

/**
 * Lambda-based event listener implementation.
 */
class LambdaListener<T extends Event> implements BaseListener<T> {
    private final Consumer<T> handler;
    private final int priority;
    private final Class<T> eventType;

    LambdaListener(Consumer<T> handler, int priority, Class<T> eventType) {
        this.handler = handler;
        this.priority = priority;
        this.eventType = eventType;
    }

    @Override
    public void invoke(T event) {
        handler.accept(event);
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

