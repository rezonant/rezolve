package com.astronautlabs.mc.rezolve.common.capabilities;

public class EnergyStack {
    public EnergyStack(int amount) {
        this.amount = amount;
    }

    public EnergyStack() {

    }

    public static EnergyStack of(int amount) {
        return new EnergyStack(amount);
    }

    int amount = 0;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
