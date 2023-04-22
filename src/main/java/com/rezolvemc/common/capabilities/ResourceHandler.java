package com.rezolvemc.common.capabilities;

import java.util.function.BiFunction;

/**
 * A proxyable capability for sending/receiving resources of arbitrary types.
 * Pipes should utilize this capability when interfacing with endpoint blocks.
 * All resources sent/received via this capability must be represented as an
 * appropriate "stack" type (ItemStack, FluidStack, or the EnergyStack class
 * provided along with this capability)
 *
 * @param <T> The type of resource. Should be ItemStack, FluidStack, EnergyStack,
 *           or some other custom type.
 */
public interface ResourceHandler<T> {
    /**
     * Push a resource into this handler. Mode specifies simulation versus execution.
     * @param stack The resource to push
     * @param mode Whether to simulate or execute
     * @return The unused portion of the resource
     */
    T push(T stack, Mode mode);

    /**
     * Subscribe to outgoing resource operations from this handler.
     * The provided callback is executed whenever a new operation is available.
     *
     * @param observer The callback to use for the subscription. Receives the resource and the mode of the operation
     *                 (simulation/execution) and returns the unused portion of the resource (remainder). Return the
     *                 input resource to opt not to receive the resource.
     * @return
     */
    Subscription pull(BiFunction<T, Mode, T> observer);

    enum Mode {
        SIMULATE,
        EXECUTE
    }

    interface Subscription {
        void unsubscribe();
    }
}
