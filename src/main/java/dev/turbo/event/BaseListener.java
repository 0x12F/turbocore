package dev.turbo.event;

/**
 * Base interface for all listener types.
 */
interface BaseListener<T extends Event> extends Comparable<BaseListener<?>> {
    void invoke(T event);
    int getPriority();
    Class<T> getEventType();
    
    @Override
    default int compareTo(BaseListener<?> other) {
        return Integer.compare(this.getPriority(), other.getPriority());
    }
}

