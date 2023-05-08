package org.torchmc.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface EventEmitter {
    EventMap eventMap();

    class EventMap {
        final Map<EventType, List<Consumer<Event>>> handlers = new HashMap<>();
    }

    /**
     * Add a listener for the given event type. The listener will be called for all instances of this event until
     * removeEventListener() is called or the unsubscribe() method of the returned Subscription object is called.
     * To add a "one shot" listener, see listenForNextEvent() instead. A handler can unsubscribe from further invocations
     * by using event.getSubscription().unsubscribe().
     * @param eventType
     * @param handler
     * @return
     * @param <T>
     */
    default <T extends Event> Subscription addEventListener(EventType<T> eventType, Consumer<T> handler) {
        var handlers = eventMap().handlers;
        if (!handlers.containsKey(eventType)) {
            handlers.put(eventType, new ArrayList<>());
        }

        var list = handlers.get(eventType);
        list.add((Consumer<Event>)handler);

        return () -> removeEventListener(eventType, handler);
    }

    /**
     * Listen for a single instance of the given event, and then remove the event listener automatically.
     * Equivalent to calling event.getSubscription.unsubscribe() from within the event handler.
     *
     * @param eventType
     * @param handler
     * @return
     * @param <T>
     */
    default <T extends Event> Subscription listenForNextEvent(EventType<T> eventType, Consumer<T> handler) {
        return addEventListener(eventType, e -> {
            handler.accept(e);
            e.getSubscription().unsubscribe();
        });
    }

    /**
     * Remove the previously added event handler for the given event type.
     * @param eventType
     * @param handler
     * @param <T>
     */
    default <T extends Event> void removeEventListener(EventType<T> eventType, Consumer<T> handler) {
        var handlers = eventMap().handlers;
        if (!handlers.containsKey(eventType))
            return;

        var list = handlers.get(eventType);
        list.remove((Consumer<Event>)handler);
    }

    default void emitEvent(EventType<Event> eventType) {
        emitEvent(eventType, new Event());
    }

    default <T extends Event> void emitEvent(EventType<T> eventType, T event) {
        var handlers = eventMap().handlers;
        if (!handlers.containsKey(eventType))
            return;

        List<Consumer<Event>> list = new ArrayList<>();
        list.addAll(handlers.get(eventType));

        for (var handler : list) {
            event.handle(() -> removeEventListener(eventType, (Consumer<T>)handler), handler);
        }
    }
}
