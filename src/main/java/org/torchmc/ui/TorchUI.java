package org.torchmc.ui;

import net.minecraft.resources.ResourceLocation;

public class TorchUI {
    public static ResourceLocation builtInTex(String name) {
        return new ResourceLocation("rezolve", "textures/" + name);
    }
}
