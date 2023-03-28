package com.astronautlabs.mc.rezolve.registry;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;

/**
 * Provides an annotation-driven way to register blocks, items, block entities and menus for the Rezolve mod.
 */
@Mod.EventBusSubscriber(modid = RezolveMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RezolveRegistry {
    private static final Logger LOGGER = LogManager.getLogger();
    public static Map<Class, Object> registryObjects = new HashMap<>();

    private static List<Class<?>> registeredClasses = new ArrayList<>();

    /**
     * Register multiple classes at the same time. The registration type (block, item, etc) is auto-determined based on
     * the base class of each passed class. See the register*() methods for more information about particular types.
     * @param classes
     */
    public static void register(Class<?> ...classes) {
        for (var klass : classes) {
            registeredClasses.add(klass);
        }
    }

    @SubscribeEvent
    public static void handleRegisterEvent(RegisterEvent event) {
        for (var klass : registeredClasses)  {
            if (Block.class.isAssignableFrom(klass)) {
                registerBlock(event, (Class<Block>)klass);
            } else if (Item.class.isAssignableFrom(klass)) {
                registerItem(event, (Class<Item>)klass);
            } else if (BlockEntity.class.isAssignableFrom(klass)) {
                registerBlockEntity(event, (Class<BlockEntity>)klass, new Block[]{}); // TODO: this is probably not useful
            } else if (AbstractContainerMenu.class.isAssignableFrom(klass)) {
                registerMenu(event, (Class<AbstractContainerMenu>)klass);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void handleClientSetup(FMLClientSetupEvent event) {
        for (var klass : registeredClasses) {
            if (AbstractContainerMenu.class.isAssignableFrom(klass)) {
                var menuClass = (Class<? extends AbstractContainerMenu>)klass;
                var screenAnnotation = klass.getAnnotation(WithScreen.class);
                if (screenAnnotation != null) {
                    var screenClass = screenAnnotation.value();
                    registerScreen(event, menuClass, screenClass);
                }
            } else if (Block.class.isAssignableFrom(klass)) {
                var menuAnnotation = klass.getAnnotation(WithMenu.class);
                if (menuAnnotation != null) {
                    var menuClass = menuAnnotation.value();
                    var screenAnnotation = menuClass.getAnnotation(WithScreen.class);
                    if (screenAnnotation != null) {
                        var screenClass = screenAnnotation.value();
                        registerScreen(event, menuClass, screenClass);
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static <MenuT extends AbstractContainerMenu, ScreenT extends Screen & MenuAccess<MenuT>>
    void registerScreen(FMLClientSetupEvent event, Class<MenuT> menuClass, Class<ScreenT> screenClass) {
        try {
            var ctor = screenClass.getDeclaredConstructor(menuClass, Inventory.class, Component.class);
            event.enqueueWork(() -> {
                MenuScreens.register(menuType(menuClass), new MenuScreens.ScreenConstructor<MenuT, ScreenT>() {
                    @Override
                    public ScreenT create(MenuT pMenu, Inventory pInventory, Component pTitle) {
                        try {
                            return ctor.newInstance(pMenu, pInventory, pTitle);
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException(String.format("Failed to construct screen %s", screenClass.getCanonicalName()), e);
                        }
                    }
                });
            });
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(String.format("Failed to register screen %s: %s", screenClass.getCanonicalName(), e.getMessage()), e);
        }
    }

    static String getRegistryId(Class<?> klass) {
        var registryAnnotation = klass.getAnnotation(RegistryId.class);
        if (registryAnnotation != null)
            return registryAnnotation.value();
        return null;
    }

    static String requireRegistryId(Class<?> klass) {
        var registryId = getRegistryId(klass);
        if (registryId == null)
            throw new RuntimeException(String.format("Class %s is missing required @RegistryId()", klass.getCanonicalName()));

        return registryId;
    }

    private static <T extends Item> void registerItem(RegisterEvent register, Class<T> itemClass) {

        if (register.getRegistryKey() != ForgeRegistries.Keys.ITEMS)
            return;

        try {
            var item = itemClass.getDeclaredConstructor().newInstance();
            registryObjects.put(itemClass, item);
            register.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation(RezolveMod.MODID, requireRegistryId(itemClass)), () -> item);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Caught exception while constructing item {}:", itemClass.getCanonicalName());
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }

    private static <T extends Block> void registerBlock(RegisterEvent register, Class<T> blockClass) {
        String id = requireRegistryId(blockClass);

        try {
            Block block;

            if (register.getRegistryKey() == ForgeRegistries.Keys.BLOCKS) {
                block = blockClass.getDeclaredConstructor().newInstance();
                registryObjects.put(blockClass, block);
                register.register(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(RezolveMod.MODID, id), () -> {
                    return block;
                });
            } else {
                block = block(blockClass);
            }

            if (register.getRegistryKey() == ForgeRegistries.Keys.ITEMS) {
                register.register(
                        ForgeRegistries.Keys.ITEMS,
                        new ResourceLocation(RezolveMod.MODID, id),
                        () -> new BlockItem(block, new Item.Properties().tab(CreativeModeTab.TAB_MISC))
                );
            }

            // BlockEntity

            if (register.getRegistryKey() == ForgeRegistries.Keys.BLOCK_ENTITY_TYPES) {
                var blockEntityAnnotation = blockClass.getAnnotation(WithBlockEntity.class);
                if (blockEntityAnnotation != null) {
                    Class<? extends BlockEntity> blockEntityClass = blockEntityAnnotation.value();
                    registerBlockEntity(register, blockEntityClass, new Block[]{ block }, id);
                }
            }

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register Block class "+blockClass.getCanonicalName()+": "+e.getMessage(), e);
        }

        // Menu

        var menuAnnotation = blockClass.getAnnotation(WithMenu.class);
        if (menuAnnotation != null)
            registerMenu(register, menuAnnotation.value(), id);
    }

    private static <T extends AbstractContainerMenu> void registerMenu(RegisterEvent register, Class<T> menuClass) {
        registerMenu(register, menuClass, null);
    }

    private static <T extends AbstractContainerMenu> void registerMenu(RegisterEvent register, Class<T> menuClass, String registryId) {

        if (register.getRegistryKey() != ForgeRegistries.Keys.MENU_TYPES)
            return;

        if (registryId == null)
            registryId = requireRegistryId(menuClass);

        try {
            var ctor = menuClass.getConstructor(int.class, Inventory.class);

            var type = new MenuType((containerId, playerInventory) -> {
                try {
                    return ctor.newInstance(containerId, playerInventory);

                } catch (Exception e) {
                    throw new RuntimeException("Cannot construct " + menuClass.getCanonicalName(), e);
                }
            });

            registryObjects.put(menuClass, type);
            register.register(ForgeRegistries.Keys.MENU_TYPES, new ResourceLocation(RezolveMod.MODID, registryId), () -> type);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register Menu class "+menuClass.getCanonicalName()+": "+e.getMessage(), e);
        }
    }

    private static <T extends BlockEntity> void registerBlockEntity(RegisterEvent register, Class<T> entityClass, Block[] validBlocks) {
        registerBlockEntity(register, entityClass, validBlocks, null);
    }

    private static <T extends BlockEntity> void registerBlockEntity(RegisterEvent register, Class<T> entityClass, Block[] validBlocks, String registryId) {
        if (register.getRegistryKey() != ForgeRegistries.Keys.BLOCK_ENTITY_TYPES)
            return;

        if (registryId == null)
            registryId = requireRegistryId(entityClass);

        try {
            var ctor = entityClass.getConstructor(BlockPos.class, BlockState.class);
            var type = BlockEntityType.Builder.of((pPos, pState) -> {
                try {
                    return (T) ctor.newInstance(pPos, pState);
                } catch (Exception e) {
                    throw new RuntimeException("Cannot construct " + entityClass.getCanonicalName(), e);
                }
            }, validBlocks).build(null);

            registryObjects.put(entityClass, type);
            register.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation(RezolveMod.MODID, registryId), () -> type);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register BlockEntity class "+entityClass.getCanonicalName()+": "+e.getMessage(), e);
        }
    }

    public static <T extends Block> T block(Class<T> klass) {
        return (T)registryObjects.get(klass);
    }

    public static <T extends Item> T item(Class<T> klass) {
        return (T)registryObjects.get(klass);
    }

    public static <T extends BlockEntity> BlockEntityType<T> blockEntityType(Class<T> klass) {
        return (BlockEntityType<T>)registryObjects.get(klass);
    }

    public static <T extends AbstractContainerMenu> MenuType<T> menuType(Class<T> klass) {
        return (MenuType<T>)registryObjects.get(klass);
    }

}
