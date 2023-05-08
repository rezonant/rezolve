package org.torchmc.events;

import java.util.function.Consumer;

public class Event {
    private Subscription subscription;

    public Subscription getSubscription() {
        return subscription;
    }

    public <T extends Event> void handle(Subscription subscription, Consumer<T> handler) {
        try {
            this.subscription = subscription;
            handler.accept((T)this);
        } finally {
            this.subscription = null;
        }
    }
}
