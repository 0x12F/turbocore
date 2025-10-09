package dev.turbo.event;

import java.lang.reflect.Method;

/**
 * Thrown when an invalid listener method is registered.
 */
public class InvalidListenerException extends RuntimeException {
    public InvalidListenerException(Method method) {
        super("Invalid listener method '" + method.getDeclaringClass().getName() + "." + method.getName() + "'.");
    }
}

