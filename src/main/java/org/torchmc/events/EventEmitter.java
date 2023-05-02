package org.torchmc.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface EventEmitter {
    Event EMPTY = new Event();

    EventMap eventMap();

    class EventMap {
        Map<EventType, List<Consumer<Event>>> handlers = new HashMap<>();
    }

    default <T extends Event> Subscription addEventListener(EventType<T> eventType, Consumer<T> handler) {
        var handlers = eventMap().handlers;
        if (!handlers.containsKey(eventType)) {
            handlers.put(eventType, new ArrayList<>());
        }

        var list = handlers.get(eventType);
        list.add((Consumer<Event>)handler);

        return () -> removeEventListener(eventType, handler);
    }

    default <T extends Event> void removeEventListener(EventType<T> eventType, Consumer<T> handler) {
        var handlers = eventMap().handlers;
        if (!handlers.containsKey(eventType))
            return;

        var list = handlers.get(eventType);
        list.remove((Consumer<Event>)handler);
    }

    default void emitEvent(EventType<Event> eventType) {
        emitEvent(eventType, EMPTY);
    }

    default <T extends Event> void emitEvent(EventType<T> eventType, T event) {
        var handlers = eventMap().handlers;
        if (!handlers.containsKey(eventType))
            return;

        var list = handlers.get(eventType);
        for (var handler : list) {
            handler.accept(event);
        }
    }
}
