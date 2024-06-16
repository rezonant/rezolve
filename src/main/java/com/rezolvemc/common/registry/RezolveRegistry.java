package com.rezolvemc.common.registry;

import com.rezolvemc.Rezolve;
import com.rezolvemc.RezolveCreativeTab;
import com.rezolvemc.common.ItemBase;
import com.rezolvemc.common.blocks.BlockBase;
import com.rezolvemc.common.machines.Operation;
import com.rezolvemc.common.machines.WithOperation;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.util.RezolveReflectionUtil;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
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
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Provides an annotation-driven way to register blocks, items, object entities and menus for the Rezolve mod.
 */
@Mod.EventBusSubscriber(modid = Rezolve.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
//        for (var klass : classes) {
//            registeredClasses.add(klass);
//
//            if (RezolvePacket.class.isAssignableFrom(klass)) {
//                registerPacket((Class<RezolvePacket>)klass);
//            }
//        }
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

    private static boolean hasDiscoveredClasses = false;

    @SubscribeEvent
    public static void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        for (var obj : registryObjects.values()) {
            if (obj instanceof BlockBase block) {
                if (getCreativeModeTab(block.getClass()) == event.getTab().getClass())
                    event.accept(block);
            } else if (obj instanceof ItemBase item) {
                if (getCreativeModeTab(item.getClass()) == event.getTab().getClass())
                    event.accept(item);
            }
        }
    }

    private static List<Class<?>> getRegisteredClasses() {
        if (!hasDiscoveredClasses) {
            hasDiscoveredClasses = true;
            for (var klass : RezolveReflectionUtil.findAnnotatedClasses(RegistryId.class)) {
                if (!registeredClasses.contains(klass))
                    registeredClasses.add(klass);
            }
        }

        return registeredClasses;
    }

    private static List<CreativeModeTab> registeredTabs = new ArrayList<>();

    @SubscribeEvent
    public static void handleRegisterEvent(RegisterEvent event) {
        LOGGER.info("Registry {}", event.getRegistryKey());
        RezolvePacket.init();

        for (var klass : getRegisteredClasses())  {
            if (Block.class.isAssignableFrom(klass)) {
                registerBlock(event, (Class<Block>)klass);
            } else if (Item.class.isAssignableFrom(klass)) {
                registerItem(event, (Class<Item>)klass);
            } else if (RezolvePacket.class.isAssignableFrom(klass)) {
                registerPacket((Class<RezolvePacket>)klass);
            } else if (CreativeModeTab.class.isAssignableFrom((klass))) {
                registerCreativeModeTab(event, (Class<CreativeModeTab>)klass);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void handleClientSetup(FMLClientSetupEvent event) {
        var klasses = RezolveReflectionUtil.findAnnotatedClasses(ScreenFor.class);

        for (var klass : klasses) {
            var screenFor = klass.getAnnotation(ScreenFor.class);
            var screenClass = (Class<? extends AbstractContainerScreen>)klass;
            var menuClass = (Class<? extends AbstractContainerMenu>)screenFor.value();
            registerScreen(event, menuClass, screenClass);
        }
    }

    private static List<Class<? extends Screen>> screenClasses = new ArrayList<>();

    public static Class<? extends Screen>[] screenClasses() {
        return screenClasses.toArray(new Class[screenClasses.size()]);
    }

    @OnlyIn(Dist.CLIENT)
    private static <MenuT extends AbstractContainerMenu, ScreenT extends Screen & MenuAccess<MenuT>>
    void registerScreen(FMLClientSetupEvent event, Class<MenuT> menuClass, Class<ScreenT> screenClass) {
        screenClasses.add(screenClass);

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

    public static Class<? extends CreativeModeTab> getCreativeModeTab(Class<?> klass) {
        var registryAnnotation = klass.getAnnotation(WithinCreativeModeTab.class);
        if (registryAnnotation != null)
            return registryAnnotation.value();

        return RezolveCreativeTab.class;
    }

    /**
     * Obtain the registry ID for the given Rezolve registerable class.
     * @param klass
     * @return
     */
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
            register.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation(Rezolve.ID, requireRegistryId(itemClass)), () -> item);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Caught exception while constructing item {}:", itemClass.getCanonicalName());
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }

    private static <T extends CreativeModeTab> void registerCreativeModeTab(RegisterEvent register, Class<T> tabClass) {
        String id = requireRegistryId(tabClass);
        try {
            CreativeModeTab tab = tabClass.getDeclaredConstructor().newInstance();
            registryObjects.put(tabClass, tab);
            register.register(Registries.CREATIVE_MODE_TAB, new ResourceLocation(Rezolve.ID, id), () -> tab);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register CreativeModeTab class "+tabClass.getCanonicalName()+": "+e.getMessage(), e);
        }
    }

    private static <T extends Block> void registerBlock(RegisterEvent register, Class<T> blockClass) {
        String id = requireRegistryId(blockClass);

        try {
            Block block;

            if (register.getRegistryKey() == ForgeRegistries.Keys.BLOCKS) {
                block = blockClass.getDeclaredConstructor().newInstance();
                registryObjects.put(blockClass, block);
                register.register(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(Rezolve.ID, id), () -> block);
            } else {
                block = block(blockClass);
            }

            if (register.getRegistryKey() == ForgeRegistries.Keys.ITEMS) {
                register.register(
                        ForgeRegistries.Keys.ITEMS,
                        new ResourceLocation(Rezolve.ID, id),
                        () -> {
                            var blockItem = new BlockItem(block, new Item.Properties());
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

        if (registryId == null)
            registryId = requireRegistryId(menuClass);

        try {
            LOGGER.debug("Registering menu " + menuClass.getCanonicalName());

            var type = getMenuType(menuClass);
            registryObjects.put(menuClass, type);
            register.register(ForgeRegistries.Keys.MENU_TYPES, new ResourceLocation(Rezolve.ID, registryId), () -> type);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register Menu class "+menuClass.getCanonicalName()+": "+e.getMessage(), e);
        }
    }

    @NotNull
    private static <T extends AbstractContainerMenu> MenuType<T> getMenuType(Class<T> menuClass) throws NoSuchMethodException {
        Constructor<T> ctor;

        try {
            ctor = menuClass.getConstructor(int.class, Inventory.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Menu class "+menuClass.getCanonicalName()+" is missing constructor(int, Inventory): "+e.getMessage(), e);
        }

        return new MenuType<>((containerId, playerInventory) -> {
            try {
                return ctor.newInstance(containerId, playerInventory);
            } catch (Exception e) {
                // RuntimeExceptions get absorbed somewhere up the stack without being printed, so we need
                // to take care to print to the log here for debuggability.
                LOGGER.error("Cannot construct {}: {}", menuClass.getCanonicalName(), e.getMessage());
                throw new RuntimeException("Cannot construct " + menuClass.getCanonicalName(), e);
            }
        }, FeatureFlags.DEFAULT_FLAGS);
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
            register.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation(Rezolve.ID, registryId), () -> type);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register BlockEntity class "+entityClass.getCanonicalName()+": "+e.getMessage(), e);
        }

        var operationAnnotation = entityClass.getAnnotation(WithOperation.class);
        if (operationAnnotation != null) {
            registerOperation(operationAnnotation.value());
        }
    }

    public static <T extends Block> T block(Class<T> klass) {
        return (T)registryObjects.get(klass);
    }

    public static <T extends CreativeModeTab> T creativeModeTab(Class<T> klass) {
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
        Tagger<T> tag(TagKey<T> tag);
        Tagger<T> tag(String tag);
    }

    private record TagProvider<T>(T object, Consumer<Tagger<T>> configurer) {}
    private static final List<TagProvider<Block>> blocksForTagging = new ArrayList<>();
    private static final List<TagProvider<Item>> itemsForTagging = new ArrayList<>();

    public static void registerForTagging(Block block, Consumer<Tagger<Block>> configurer) {
        blocksForTagging.add(new TagProvider(block, configurer));
    }

    public static void registerForTagging(Item item, Consumer<Tagger<Item>> configurer) {
        itemsForTagging.add(new TagProvider(item, configurer));
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var blockTagsProvider = new BlockTagsGenerator(event, event.getLookupProvider());

        event.getGenerator().addProvider(true, blockTagsProvider);
        event.getGenerator().addProvider(true, new ItemTagsGenerator(event, event.getLookupProvider(), blockTagsProvider.contentsGetter()));
    }

    public static class BlockTagsGenerator extends BlockTagsProvider {
        public BlockTagsGenerator(GatherDataEvent event, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(event.getGenerator().getPackOutput(), lookupProvider, Rezolve.ID, event.getExistingFileHelper());
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
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
        public ItemTagsGenerator(GatherDataEvent event, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
            super(event.getGenerator().getPackOutput(), lookupProvider, blockTags, Rezolve.ID, event.getExistingFileHelper());
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
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
