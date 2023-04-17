package com.astronautlabs.mc.rezolve.common.registry;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.blocks.BlockBase;
import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import com.astronautlabs.mc.rezolve.common.gui.WithScreen;
import com.astronautlabs.mc.rezolve.common.machines.WithOperation;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.astronautlabs.mc.rezolve.common.machines.Operation;
import com.astronautlabs.mc.rezolve.common.network.WithPacket;
import com.astronautlabs.mc.rezolve.common.util.RezolveReflectionUtil;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides an annotation-driven way to register blocks, items, object entities and menus for the Rezolve mod.
 */
@Mod.EventBusSubscriber(modid = RezolveMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RezolveRegistry {
    private static final Logger LOGGER = LogManager.getLogger();
    public static Map<Class, Object> registryObjects = new HashMap<>();

    private static List<Class<?>> registeredClasses = new ArrayList<>();

    /**
     * Register multiple classes at the same time. The registration type (object, item, etc) is auto-determined based on
     * the base class of each passed class. See the register*() methods for more information about particular types.
     * @param classes
     */
    public static void register(Class<?> ...classes) {
        for (var klass : classes) {
            registeredClasses.add(klass);

            if (RezolvePacket.class.isAssignableFrom(klass)) {
                registerPacket((Class<RezolvePacket>)klass);
            }
        }
    }

    private static Map<String, Class<? extends RezolvePacket>> packetsById = new HashMap<>();
    private static Map<Class<? extends RezolvePacket>, String> packetIdsByClass = new HashMap<>();

    private static void registerPacket(Class<? extends RezolvePacket> message) {
        if (packetIdsByClass.containsKey(message))
            return;

        LOGGER.info("Registering packet {}", message.getCanonicalName());
        var id = requireRegistryId(message);
        packetsById.put(id, message);
        packetIdsByClass.put(message, id);
    }

    public static String getPacketId(Class<RezolvePacket> message) {
        return packetIdsByClass.get(message);
    }

    public static Class<? extends RezolvePacket> getPacketClass(String id) {
        return packetsById.get(id);
    }

    private static void registerAssociatedPackets(Class<?> klass) {
        var annotations = RezolveReflectionUtil.getHeirarchicalAnnotations(klass, WithPacket.class);
        for (var annotation : annotations)
            registerPacket(annotation.value());
    }

    private static Map<String, Class<? extends Operation>> operationsById = new HashMap<>();
    private static Map<Class<? extends Operation>, String> operationIdsByClass = new HashMap<>();

    private static void registerOperation(Class<? extends Operation> operation) {
        if (operationIdsByClass.containsKey(operation))
            return;

        LOGGER.info("Registering operation {}", operation.getCanonicalName());
        var id = requireRegistryId(operation);
        operationsById.put(id, operation);
        operationIdsByClass.put(operation, id);
    }

    public static String getOperationId(Class<Operation> message) {
        return operationIdsByClass.get(message);
    }

    public static Class<? extends Operation> getOperationClass(String id) {
        return operationsById.get(id);
    }

    @SubscribeEvent
    public static void handleRegisterEvent(RegisterEvent event) {
        RezolvePacket.init();

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
                            // Must print this error as it gets eaten up the stack.
                            LOGGER.error("Failed to construct screen {}: {}", screenClass.getCanonicalName(), e.toString());
                            throw new RuntimeException(String.format("Failed to construct screen %s", screenClass.getCanonicalName()), e);
                        }
                    }
                });
            });
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(String.format("Failed to register screen %s: %s", screenClass.getCanonicalName(), e.getMessage()), e);
        }
    }

    public static String getRegistryId(Class<?> klass) {
        var registryAnnotation = klass.getAnnotation(RegistryId.class);
        if (registryAnnotation != null)
            return registryAnnotation.value();
        return null;
    }

    public static String requireRegistryId(Class<?> klass) {
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
            register.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation(RezolveMod.ID, requireRegistryId(itemClass)), () -> item);
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
                register.register(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(RezolveMod.ID, id), () -> {
                    return block;
                });
            } else {
                block = block(blockClass);
            }

            if (register.getRegistryKey() == ForgeRegistries.Keys.ITEMS) {
                register.register(
                        ForgeRegistries.Keys.ITEMS,
                        new ResourceLocation(RezolveMod.ID, id),
                        () -> {
                            var blockItem = new BlockItem(block, new Item.Properties().tab(RezolveMod.CREATIVE_MODE_TAB));
                            if (block instanceof BlockBase blockBase) {
                                blockBase.initializeItem(blockItem);
                            }

                            return blockItem;
                        }
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

        registerAssociatedPackets(menuClass);

        if (registryId == null)
            registryId = requireRegistryId(menuClass);

        try {
            var ctor = menuClass.getConstructor(int.class, Inventory.class);

            var type = new MenuType((containerId, playerInventory) -> {
                try {
                    return ctor.newInstance(containerId, playerInventory);

                } catch (Exception e) {
                    // RuntimeExceptions get absorbed somewhere up the stack without being printed, so we need
                    // to take care to print to the log here for debuggability.
                    LOGGER.error("Cannot construct {}: {}", menuClass.getCanonicalName(), e.getMessage());
                    throw new RuntimeException("Cannot construct " + menuClass.getCanonicalName(), e);
                }
            });

            registryObjects.put(menuClass, type);
            register.register(ForgeRegistries.Keys.MENU_TYPES, new ResourceLocation(RezolveMod.ID, registryId), () -> type);

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
            register.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation(RezolveMod.ID, registryId), () -> type);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register BlockEntity class "+entityClass.getCanonicalName()+": "+e.getMessage(), e);
        }

        registerAssociatedPackets(entityClass);

        var operationAnnotation = entityClass.getAnnotation(WithOperation.class);
        if (operationAnnotation != null) {
            registerOperation(operationAnnotation.value());
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

    public interface Tagger<T> {
        Tagger tag(TagKey<T> tag);
        Tagger tag(String tag);
    }

    private record TagProvider<T>(T object, Consumer<Tagger> configurer) {}
    private static List<TagProvider<Block>> blocksForTagging = new ArrayList<>();
    private static List<TagProvider<Item>> itemsForTagging = new ArrayList<>();

    public static void registerForTagging(Block block, Consumer<Tagger<Block>> configurer) {
        blocksForTagging.add(new TagProvider(block, configurer));
    }

    public static void registerForTagging(Item item, Consumer<Tagger<Item>> configurer) {
        itemsForTagging.add(new TagProvider(item, configurer));
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var blockTagsProvider = new BlockTagsGenerator(event);

        event.getGenerator().addProvider(true, blockTagsProvider);
        event.getGenerator().addProvider(true, new ItemTagsGenerator(event, blockTagsProvider));
    }

    public static class BlockTagsGenerator extends BlockTagsProvider {
        public BlockTagsGenerator(GatherDataEvent event) {
            super(event.getGenerator(), RezolveMod.ID, event.getExistingFileHelper());
        }

        @Override
        protected void addTags() {
            try {
                for (var provider : blocksForTagging) {
                    provider.configurer.accept(new Tagger<Block>() {
                        @Override
                        public Tagger tag(TagKey<Block> tag) {
                            BlockTagsGenerator.this.tag(tag).add(provider.object);
                            return null;
                        }

                        @Override
                        public Tagger tag(String tag) {
                            return tag(BlockTags.create(ResourceLocation.tryParse(tag)));
                        }
                    });
                }
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    public static class ItemTagsGenerator extends ItemTagsProvider {
        public ItemTagsGenerator(GatherDataEvent event, BlockTagsProvider blockTagsProvider) {
            super(event.getGenerator(), blockTagsProvider, RezolveMod.ID, event.getExistingFileHelper());
        }

        @Override
        protected void addTags() {
            try {
                for (var provider : itemsForTagging) {
                    provider.configurer.accept(new Tagger<Item>() {
                        @Override
                        public Tagger tag(TagKey<Item> tag) {
                            ItemTagsGenerator.this.tag(tag).add(provider.object);
                            return null;
                        }

                        @Override
                        public Tagger tag(String tag) {
                            return tag(ItemTags.create(ResourceLocation.tryParse(tag)));
                        }
                    });
                }
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }
}
