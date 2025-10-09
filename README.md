# TurboCore Event System

Fast, lightweight event system for Minecraft clients.

## Quick Start

```java
EventBus bus = new EventBus();

// Lambda-based (recommended)
Subscription sub = bus.subscribe(PlayerTickEvent.class, event -> {
    // handle event
});

// Annotation-based
bus.register(new MyModule());

// Post event
bus.post(new PlayerTickEvent(Stage.PRE));
```

## Creating Events

### Simple Event

```java
public class MyEvent extends Event {
    private final String data;
    
    public MyEvent(String data) {
        this.data = data;
    }
    
    public String getData() {
        return data;
    }
}
```

### Cancellable Event

```java
public class MyEvent extends Cancellable.Impl {
    private final String data;
    
    public MyEvent(String data) {
        this.data = data;
    }
    
    public String getData() {
        return data;
    }
}
```

## Listening to Events

### Lambda Listeners

```java
// Simple subscription
Subscription sub = bus.subscribe(MyEvent.class, event -> {
    System.out.println(event.getData());
});

// With priority
bus.subscribe(MyEvent.class, event -> {
    event.setCancelled(true);
}, EventPriority.HIGH);

// Unsubscribe
sub.unsubscribe();
```

### Annotation Listeners

```java
public class MyModule {
    @EventHandler
    public void onTick(PlayerTickEvent event) {
        // your code
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onMyEvent(MyEvent event) {
        if (someCondition) {
            event.setCancelled(true);
        }
    }
}
```

## Priority Levels

- `HIGHEST` (0) - Runs first
- `HIGH` (1)
- `NORMAL` (2) - Default
- `LOW` (3)
- `LOWEST` (4) - Runs last

## Event Hierarchy

Listeners registered for a parent event will receive child events too:

```java
public class BaseEvent extends Event {}
public class ChildEvent extends BaseEvent {}

bus.subscribe(BaseEvent.class, event -> {
    // receives both BaseEvent and ChildEvent
});

bus.post(new ChildEvent()); // triggers listener
```

## Error Handling

Listener exceptions are caught and logged. Other listeners continue executing:

```java
bus.subscribe(MyEvent.class, event -> {
    throw new RuntimeException(); // caught, logged, doesn't stop other listeners
});
```

## Thread Safety

EventBus is thread-safe. Safe to register/unregister from any thread.

