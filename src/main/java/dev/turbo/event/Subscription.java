package dev.turbo.event;

/**
 * Represents an active event subscription that can be unsubscribed.
 */
public interface Subscription {
    void unsubscribe();
    boolean isActive();
}

