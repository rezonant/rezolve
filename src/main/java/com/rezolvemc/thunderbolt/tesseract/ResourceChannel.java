package com.rezolvemc.thunderbolt.tesseract;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.torchmc.events.Event;
import org.torchmc.events.EventEmitter;
import org.torchmc.events.EventType;

import java.lang.ref.WeakReference;
import java.util.*;

public class ResourceChannel implements EventEmitter {
    public ResourceChannel(String uuid) {
        this.uuid = uuid;
        this.name = uuid;
    }

    public ResourceChannel(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public static final EventType<ProviderEvent> RESOURCE_ADDED = new EventType<>();
    public static final EventType<ProviderEvent> RESOURCE_REMOVED = new EventType<>();
    public static final EventType<Event> CHANNEL_REMOVED = new EventType<>();

    private final String uuid;
    private final String name;
    private final EventMap eventMap = new EventMap();

    @Override
    public EventMap eventMap() {
        return eventMap;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    private WeakHashMap<Object, List<Resource>> resourcesByOwner = new WeakHashMap<>();
    private Map<Resource, WeakReference<Object>> owners = new HashMap<>();

    public void notifyRemoved() {
        emitEvent(CHANNEL_REMOVED);
    }

    public void leave(Object owner) {
        setResources(owner, new ArrayList<>());
        resourcesByOwner.remove(owner);
    }

    public Resource[] getAllResources() {
        var list = new ArrayList<Resource>();

        for (var set : resourcesByOwner.values())
            list.addAll(set);

        return list.toArray(new Resource[list.size()]);
    }

    public Resource[] getResourcesOwnedBy(Object owner) {
        var list = resourcesByOwner.get(owner);
        if (list == null)
            return new Resource[0];

        return list.toArray(new Resource[list.size()]);
    }

    public Resource[] getResourcesNotOwnedBy(Object owner) {
        var list = new ArrayList<Resource>();

        for (var otherOwner : resourcesByOwner.keySet()) {
            if (owner == otherOwner)
                continue;

            list.addAll(resourcesByOwner.get(otherOwner));
        }

        return list.toArray(new Resource[list.size()]);
    }

    public <T> List<T> getResourceCapabilitiesNotOwnedBy(Object owner, Capability<T> capType) {
        var list = new ArrayList<T>();
        for (var resource : getResourcesNotOwnedBy(owner)) {
            var cap = resource.getCapability(capType);
            if (cap.isPresent())
                list.add(cap.orElse(null));
        }

        return list;
    }

    /**
     * Inform the network of what resources the given owning object is making available. If any
     * resource is already owned by another object, it will be ignored. Any existing providers which
     * are not included in the given set will be removed.
     * <p>
     * All changes will be announced using the RESOURCE_ADDED and RESOURCE_REMOVED events.
     *
     * @param owner
     * @param newResources
     */
    public void setResources(Object owner, List<Resource> newResources) {
        var ownedResources = resourcesByOwner.get(owner);
        if (ownedResources == null)
            resourcesByOwner.put(owner, ownedResources = new ArrayList<>());

        var added = new ArrayList<Resource>();
        var removed = new ArrayList<Resource>();
        var ownedByOthers = new ArrayList<Resource>();

        removed.addAll(ownedResources);
        for (var resource : newResources) {
            removed.remove(resource);

            if (resourcesByOwner.containsKey(resource)) // An owner cannot also be a resource.
                continue;

            var existingOwner = owners.get(resource);
            if (existingOwner != null && existingOwner != owner) {
                ownedByOthers.add(resource);
                continue;
            }

            if (!ownedResources.contains(resource))
                added.add(resource);
        }

        // Make the necessary changes and announce them.

        for (var resource : removed) {
            ownedResources.remove(resource);
            owners.remove(resource);
            emitEvent(RESOURCE_REMOVED, new ProviderEvent(resource));
        }

        for (var resource : added) {
            ownedResources.add(resource);
            emitEvent(RESOURCE_ADDED, new ProviderEvent(resource));
        }
    }

    public static class ProviderEvent extends Event {
        public ProviderEvent(ICapabilityProvider provider) {
            this.provider = provider;
        }

        public final ICapabilityProvider provider;
    }
}
