# Registration, content and events — NeoForge 26.1.2

All import paths below are verified against the 26.1.2 MDK example sources and the 26.1 docs.

## Key import paths (changed since 1.21.x)

| Class | Package |
| --- | --- |
| `@Mod` | `net.neoforged.fml.common.Mod` |
| `@EventBusSubscriber` | `net.neoforged.fml.common.EventBusSubscriber` |
| `ModContainer` | `net.neoforged.fml.ModContainer` |
| `IEventBus` | `net.neoforged.bus.api.IEventBus` (**not** `net.neoforged.fml.IEventBus`) |
| `@SubscribeEvent` | `net.neoforged.bus.api.SubscribeEvent` (**not** `net.neoforged.neoforge.*`) |
| `ModConfig`, `ModConfigSpec` | `net.neoforged.fml.config.ModConfig`, `net.neoforged.neoforge.common.ModConfigSpec` |
| `Dist` | `net.neoforged.api.distmarker.Dist` |
| `NeoForge.EVENT_BUS` | `net.neoforged.neoforge.common.NeoForge` |
| `DeferredRegister`, `DeferredHolder`, `DeferredBlock`, `DeferredItem` | `net.neoforged.neoforge.registries.*` |
| `Identifier` | `net.minecraft.resources.Identifier` (replaces `ResourceLocation`) |
| `BuiltInRegistries` | `net.minecraft.core.registries.BuiltInRegistries` |
| `Registries` | `net.minecraft.core.registries.Registries` |
| `ItemStack` / `ItemStackTemplate` | `net.minecraft.world.item.ItemStack` / `...ItemStackTemplate` — verify the exact package in sources |
| `IConfigScreenFactory`, `ConfigurationScreen` | `net.neoforged.neoforge.client.gui.*` |
| `FoodProperties` | `net.minecraft.world.food.FoodProperties` (now has a `Builder`) |

## Two event buses

- **Mod event bus** — lifetime of the mod object. Registry events, config, datagen, client setup. Received via the `IEventBus` constructor parameter.
- **Game event bus** (`NeoForge.EVENT_BUS`) — gameplay: server tick, player events, block updates, chunk load, loot, etc.

A mod class with `@SubscribeEvent` methods must also register itself: `NeoForge.EVENT_BUS.register(this)`.

## Main mod class (verbatim from the 26.1.2 MDK)

```java
package com.example.examplemod;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<Block> EXAMPLE_BLOCK =
            BLOCKS.registerSimpleBlock("example_block", p -> p.mapColor(MapColor.STONE));
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    public static final DeferredItem<Item> EXAMPLE_ITEM =
            ITEMS.registerSimpleItem("example_item", p -> p.food(new FoodProperties.Builder()
                    .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.examplemod"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(EXAMPLE_ITEM.get()))
                    .build());

    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) { /* ... */ }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) { /* ... */ }
}
```

### Constructor rules

- Exactly **one public constructor**.
- Parameters FML injects, in any order: `IEventBus`, `ModContainer`, `FMLModContainer`, `Dist`.
- `dist = Dist.CLIENT` / `Dist.DEDICATED_SERVER` restricts the entry point to a physical side.

### Client entry point (verbatim from the 26.1.2 MDK)

```java
package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ExampleMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ExampleModClient {
    public ExampleModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        ExampleMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        ExampleMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
```

## DeferredRegister

```java
// Convenience factories
DeferredRegister.createBlocks(MODID);   // DeferredRegister.Blocks
DeferredRegister.createItems(MODID);    // DeferredRegister.Items
DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

// Generic form
DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, MODID);
DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
```

Useful `register*` helpers (all give a `Deferred*` wrapper whose `.get()` is safe once registries load):

```java
BLOCKS.registerSimpleBlock("my_block", p -> p.mapColor(MapColor.STONE));
BLOCKS.registerBlock("my_block", registryName -> new Block(BlockBehaviour.Properties.of()
        .setId(ResourceKey.create(Registries.BLOCK, registryName))
        .destroyTime(2.0f)
        .explosionResistance(10.0f)
        .sound(SoundType.GRAVEL)
        .lightLevel(state -> 7)));

ITEMS.registerSimpleItem("my_item");
ITEMS.registerItem("my_item", Item::new, props -> props);
ITEMS.registerSimpleBlockItem("my_block", MY_BLOCK);

DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTION =
        DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, MODID);
```

**`setId(...)` is mandatory on `BlockBehaviour.Properties` since 21.2.** The `registerBlock` overload supplies the `registryName` for you; `registerSimpleBlock` handles it internally.

**Classload ordering:** if blocks live in a different class from items, force the blocks class to load first (touch a field, or register from a class that references the block holder) or you will get a class-init race.

## Creative tabs

Own tab (preferred):

```java
CREATIVE_MODE_TABS.register("my_tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.yourmodid.my_tab"))
        .withTabsBefore(CreativeModeTabs.COMBAT)   // also withTabsAfter / withTabsBetween
        .icon(() -> MY_ITEM.get().getDefaultInstance())
        .displayItems((params, output) -> output.accept(MY_ITEM.get()))
        .build());
```

Add to a vanilla tab (mod event bus; fires on logical client only):

```java
@SubscribeEvent
static void buildContents(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
        event.accept(MY_ITEM.get());
    }
}
```

Lang keys: `itemGroup.<modid>`, `item.<modid>.<name>`, `block.<modid>.<name>`, `container.<modid>.<name>`.

## Block entities

`BlockEntityType.Builder` was removed in 21.2 — use the constructor directly.

```java
public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

public static final Supplier<BlockEntityType<MyBlockEntity>> MY_BLOCK_ENTITY =
        BLOCK_ENTITY_TYPES.register("my_block_entity", () -> new BlockEntityType<>(
                MyBlockEntity::new,   // (BlockPos, BlockState) ctor
                false,                // allow create-block-entity-from-item shortcut
                MyBlocks.MY_BLOCK.get()));
```

Block side:

```java
public class MyBlock extends Block implements EntityBlock {
    public MyBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MyBlockEntity(pos, state);
    }
}
```

**Persistence uses Value I/O, not `CompoundTag`** (since 21.6):

```java
@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    output.putInt("value", this.value);
}

@Override
protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    this.value = input.getIntOr("value", 0);
}
```

Verify whether the docs render these as `protected` or `public` in 26.1 — match the vanilla superclass in the decompiled sources rather than trusting a snippet.

Sync-related members: `getUpdateTag(HolderLookup.Provider)`, `saveWithoutMetadata(...)`, `handleUpdateTag(ValueInput)`, `getUpdatePacket()`, `onDataPacket(Connection, ValueInput)`, `ClientboundBlockEntityDataPacket.create(this)`, `preRemoveSideEffects(...)`.

Entity equivalents: `addAdditionalSaveData(ValueOutput)` / `readAdditionalSaveData(ValueInput)`.

Prefer NeoForge's `ValueIOSerializable` over `INBTSerializable`.

## Menus and containers

```java
public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, MODID);

public static final Supplier<MenuType<MyMenu>> MY_MENU =
        MENUS.register("my_menu", () -> new MenuType<>(MyMenu::new, FeatureFlags.DEFAULT_FLAGS));
// with extra data from the opener:
MENUS.register("my_menu2", () -> IMenuTypeExtension.create(MyMenu::new));
```

```java
public class MyMenu extends AbstractContainerMenu {
    public MyMenu(int containerId, Inventory playerInv) {
        super(MY_MENU.get(), containerId);
    }
}
```

Opening from a block:

```java
@Override
public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
    return new SimpleMenuProvider((id, inv, player) -> new MyMenu(id, inv),
            Component.translatable("container.yourmodid.my_menu"));
}

@Override
protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                           Player player, BlockHitResult result) {
    if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
        serverPlayer.openMenu(state.getMenuProvider(level, pos));
    }
    return InteractionResult.SUCCESS;
}
```

Prefer `ItemStacksResourceHandler` over `Container`. NeoForge provides `StackCopySlot` for slots that mutate `ItemStack`s, and `ResourceHandlerSlot`.

## Data components

Custom component type (no more NBT on items since 1.20.5):

```java
public static final DeferredRegister.DataComponents REGISTRAR =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "examplemod");

public static final Supplier<DataComponentType<ExampleRecord>> BASIC_EXAMPLE =
        REGISTRAR.registerComponentType("basic", builder -> builder
                .persistent(BASIC_CODEC)
                .networkSynchronized(BASIC_STREAM_CODEC));
```

`Item.Properties`:
- `component(DataComponents.X, value)` — eager.
- `delayedComponent(DataComponents.X, ctx -> ...)` — lazy, for `Holder`-bound components (resolved on datapack reload).
- `delayedHolderComponent(DataComponents.X, key)`.

Read with `stack.get(DataComponents.X)` or `stack.getOrDefault(...)`. Vanilla item classes were removed too: `SwordItem`, `DiggerItem`, `ArmorItem` no longer exist — weapons/tools/armour are now purely `WEAPON`, `TOOL`, `ARMOR`, `BLOCKS_ATTACKS` components.

## ItemStack / ItemStackTemplate — the 26.1 change that breaks the most code

`new ItemStack(...)` now requires loaded registries, so it is illegal in static/data contexts.

```java
ItemStackTemplate template = new ItemStackTemplate(
        Items.APPLE.builtInRegistryHolder(),
        5,
        DataComponentPatch.builder().set(DataComponents.ITEM_NAME, Component.literal("Apple?")).build());

ItemStack stack = template.create();                                  // template -> stack
ItemStackTemplate back = ItemStackTemplate.fromNonEmptyStack(stack);  // non-empty stack -> template
```

- `null` is the "empty" template (there are no empty `ItemStackTemplate` instances).
- `ItemStack` now implements `ItemInstance`; use `stack.typeHolder()` (was `getItemHolder()`), `stack.count()`, `stack.tags()`.
- `ItemStack#matches.isSameItemSameComponents(stack, template)` and `ItemResource#matches(template)`.
- `ItemStackTemplate` has `CODEC`, `MAP_CODEC`, `STREAM_CODEC`.
- Removed: `ItemStack.SINGLE_ITEM_CODEC`, `STRICT_CODEC`, `STRING_SINGLE_ITEM_CODEC`, `SIMPLE_ITEM_CODEC`.
- `getMaxStackSize` moved to `ItemInstance`.

Signatures that now take `ItemStackTemplate`: every vanilla recipe result (`ShapedRecipe`, `ShapelessRecipe`, `SmeltingRecipe`, `AbstractCookingRecipe`, `StonecutterRecipe`, `SmithingTransformRecipe`, `TransmuteRecipe`, `ImbueRecipe`, `SingleItemRecipe`), `ItemPredicate`, `ItemBody`, `BundleContents`, `ChargedProjectile`, `UseRemainder`, `Item#getCraftingRemainder`, `ItemContainerContents#nonEmptyItems`, `SlotDisplay$ItemStackSlotDisplay`, `HoverEvent$ShowItem`, `ItemParticleOption`, `Advancement$Builder#display`, `DisplayInfo`.

Fluids mirror this: `FluidStack`/`FluidResource` need loaded registries, `FluidStackTemplate` was added, and fluids can have default data components.

## Attachments (modern alternative to capabilities)

Registered on `NeoForgeRegistries.ATTACHMENT_TYPES` with an `IAttachmentSerializer` / `ValueIOSerializable`. Access via `getData` / `hasData` / `setData` / `syncData`. Prefer attachments over injecting new data into existing objects.

## Sidedness rules

- `Dist` = **physical** side (`Dist.CLIENT`, `Dist.DEDICATED_SERVER`). `FMLEnvironment.getDist()`.
- `LogicalSide` = logical. `Level#isClientSide()`.
- `@EventBusSubscriber` handlers must be **static**; always set `modid`. Use `value = Dist.CLIENT` for client-only.
- Since 21.5, client `@Mod` entry points are constructed **before** `Minecraft` exists. `Minecraft.getInstance()` is unusable during mod loading; move such code into `FMLClientSetupEvent`.

## Mod lifecycle order

1. Mod constructor
2. All `@EventBusSubscriber` classes
3. `FMLConstructModEvent`
4. Registry events: `NewRegistryEvent`, `DataPackRegistryEvent.NewRegistry`, `RegisterEvent`
5. `FMLCommonSetupEvent`
6. `FMLClientSetupEvent` / `FMLDedicatedServerSetupEvent`
7. InterModComms
8. `FMLLoadCompleteEvent`

## Registry lookups (21.2 renames)

- `registry.getHolderOrThrow(x)` → `getOrThrow(x)`
- `registry.get(x)` → `getValue(x)`
- `RegistryAccess.registry(OrThrow)` → `lookup(OrThrow)`
- `ServerLevel`-only methods → guard with `instanceof ServerLevel`
- `Level#random` is `protected` → use `level.getRandom()`

## FML renames (21.9)

- `FMLEnvironment.dist` → `getDist()`
- `FMLEnvironment.production` → `isProduction()`
- `FMLLoader.getGamePath()` → `FMLLoader.getCurrent().getGameDir()`

## Key mappings (21.9)

`KeyMapping.Category` is now a record and **must** be registered explicitly:

```java
@SubscribeEvent  // mod event bus
static void registerKeys(RegisterKeyMappingsEvent event) {
    event.registerCategory(CATEGORY);
    event.register(KEY);
}
```
