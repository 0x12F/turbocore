package dev.turbo.event;

/**
 * Interface for events that can be cancelled.
 */
public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancelled);
    
    /**
     * Base implementation for cancellable events.
     */
    abstract class Impl extends Event implements Cancellable {
        private boolean cancelled = false;
        
        @Override
        public boolean isCancelled() {
            return cancelled;
        }
        
        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}

