package com.rezolvemc.common.registry;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WithBlockEntity {
    public Class<? extends BlockEntity> value();
}
