package com.astronautlabs.mc.rezolve.common.gui;

import net.minecraft.world.inventory.AbstractContainerMenu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WithMenu {
    Class<? extends AbstractContainerMenu> value();
}
