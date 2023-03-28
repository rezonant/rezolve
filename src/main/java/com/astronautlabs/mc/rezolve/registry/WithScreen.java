package com.astronautlabs.mc.rezolve.registry;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WithScreen {
    Class<? extends AbstractContainerScreen> value();
}
