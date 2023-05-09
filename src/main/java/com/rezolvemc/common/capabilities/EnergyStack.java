package com.rezolvemc.common.capabilities;

import java.util.Objects;

public class EnergyStack {
    public EnergyStack(int amount) {
        this.amount = amount;
    }

    public EnergyStack() {
    }

    public EnergyStack(int amount, boolean immutable) {
        this.amount = amount;
        this.immutable = immutable;
    }

    private boolean immutable = false;

    public boolean isImmutable() {
        return immutable;
    }

    public boolean isEmpty() {
        return amount <= 0;
    }

    public static final EnergyStack EMPTY = new EnergyStack(0, true);

    public static EnergyStack of(int amount) {
        return new EnergyStack(amount);
    }

    int amount = 0;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        if (immutable)
            throw new RuntimeException("Cannot modify an immutable energy stack (this is probably EnergyStack.EMPTY)");

        this.amount = amount;
    }

    public EnergyStack split(int amount) {
        if (immutable)
            throw new RuntimeException("Cannot modify an immutable energy stack (this is probably EnergyStack.EMPTY)");

        amount = Math.min(this.amount, amount);
        this.amount -= amount;
        return new EnergyStack(amount);
    }

    public EnergyStack copy() {
        return new EnergyStack(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnergyStack that = (EnergyStack) o;
        return amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
}
