# Migration checklist — 1.21.x NeoForge → 26.1.x

Read this **before** concluding that a 1.21.x snippet "cannot work". Most of the time it can, just differently. Sources:

- Porting primer: <https://github.com/neoforged/.github/blob/main/primers/26.1/index.md> (vanilla-only, very large, authoritative)
- 26.1 release notes: <https://neoforged.net/news/26.1release/>
- Intermediate: `/news/21.2release/`, `21.4release`, `21.5release`, `21.6release`, `21.9release`, `21.11release`
- Pack/data layout diff: <https://misode.github.io/versions/?id=26.1&tab=changelog>

## 0. Toolchain

```diff
- neo_version=21.1.113
+ neo_version=26.1.2.112
```
```diff
- distributionUrl=...gradle-8.x-bin.zip
+ distributionUrl=...gradle-9.2.1-bin.zip     # 9.1.0+ required
```
```diff
- languageVersion = JavaLanguageVersion.of(21)
+ languageVersion = JavaLanguageVersion.of(25)
```
```diff
- id 'net.neoforged.gradle.userdev' version '7.1.21'   // NeoGradle, userdev
+ id 'net.neoforged.moddev' version '2.0.148'           // ModDevGradle
```
Delete all Parchment config (`neoForge.parchment.*`) — Mojang parameter names are real now.

NeoForm versions changed form: `<mc version>-<neoform build>`, e.g. `26.1-1`.

IDE: IntelliJ ≥ 2025.2, Eclipse ≥ 2025-12 (or 2025-09 + Java 25 plugin).

## 1. `ResourceLocation` → `Identifier`

Landed in 21.11, still required in 26.1. `net.minecraft.resources.Identifier`.
`Identifier.fromNamespaceAndPath(ns, path)`, `Identifier.parse(str)`.

## 2. `ItemStackTemplate` — biggest change

See `registration.md` for the full API and the list of affected signatures. Summary: static/data-context item stacks are `ItemStackTemplate`; `new ItemStack(...)` needs loaded registries.

## 3. GUI rendering renames

| 1.21.11 | 26.1 |
| --- | --- |
| `GuiGraphics` | `GuiGraphicsExtractor` |
| `Screen#render` | `Screen#extractRenderState` |
| `Screen#renderBackground` | `Screen#extractBackground` |
| `AbstractContainerScreen#renderBg` | `Screen#extractBackground` |
| `AbstractContainerScreen#renderLabels` | `AbstractContainerScreen#extractLabels` |
| `AbstractWidget#renderWidget` | `AbstractWidget#extractWidgetRenderState` |

`GuiGraphicsExtractor` methods drop `draw*`/`render*`/`submit*` prefixes and `*RenderState` suffixes:
`renderOutline` → `outline`, `submitEntityRenderState` → `entity`, `hline` → `horizontalLine`, `*String*` → `*Text*`.

**Do not override `render` in `AbstractContainerScreen` subtypes** — the chain now ends by calling `renderTooltip`.
`imageWidth`/`imageHeight` became `final` constructor params (default 176×166): `super(menu, inv, title, 256, 256)`.
`ClickType` → `ContainerInput` (`slotClicked`, `AbstractContainerMenu#clicked`).

Earlier releases: colours need mandatory alpha (`0xffffff` → `0xffffffff`); `blitSprite` uses `RenderPipelines.GUI_TEXTURE`; `blit` takes a `Function<Identifier, RenderType>` as first arg.

## 4. `ChunkPos` is a record

```diff
- new ChunkPos(blockPos)     + ChunkPos.containing(blockPos)
- ChunkPos.asLong(blockPos)  + ChunkPos.pack(blockPos)
- new ChunkPos(packedLong)   + ChunkPos.unpack(packedLong)
```
`toLong`/`asLong` → `pack`.

## 5. Entity changes

- `Entity#interactAt` removed; merged into `Entity#interact`, which now receives a `Vec3`.
- `LivingEntity#brainProvider` removed — use a static `BRAIN_PROVIDER` + `makeBrain(PackedBrain)`.
- `EntityType#is` moved to `TypedInstance#is` on the entity.
- `SpawnEggItem`: `spawnEntity`/`byId`/`getType`/`spawnOffspringFromSpawnEgg` are `static`; `eggs` removed.
- `Zombie#handleAttributes(EntitySpawnReason)`.
- Block class renames: `FarmBlock`→`FarmlandBlock`, `WaterlilyBlock`→`LilyPadBlock`, `RootsBlock`→`NetherRootsBlock`, `FungusBlock`→`NetherFungusBlock`.

## 6. Recipes and serializers

```diff
- public static class Serializer implements RecipeSerializer<MyRecipe> { ... }
+ new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE))
```
- `RecipeSerializer` is now a **record**; `register` removed; entries live in `RecipeSerializers`.
- New `Recipe$CommonInfo` + `Recipe$BookInfo` (`CraftingRecipe$CraftingBookInfo`, `AbstractCookingRecipe$CookingBookInfo`) replace `group`/`CraftingBookCategory` constructor args.
- `RecipeBuilder#getResult` removed; `getDefaultRecipeId(ItemInstance)`.
- `determineBookCategory` → `determineCraftingBookCategory`.
- Builders take `ItemStackTemplate` results.
- `oreSmelting`/`oreBlasting`/`oreCooking`/`generic` now take a `CookingBookCategory`.
- `MapCloningRecipe` → `TransmuteRecipe`. `TippedArrowRecipe` → `ImbueRecipe`.
- **JSON:** `Ingredient` is a plain holder-set (`"minecraft:diamond"` / `"#c:ingots/copper"`); custom ingredients use `"neoforge:ingredient_type"`.

## 7. Loot tables and validation

- `getType()` → `codec()`; registries hold `MapCodec` directly: `LOOT_FUNCTION_TYPE`, `LOOT_POOL_ENTRY_TYPE`, `LOOT_CONDITION_TYPE`, `LOOT_NUMBER_PROVIDER_TYPE`, `LOOT_NBT_PROVIDER_TYPE`, `LOOT_SCORE_PROVIDER_TYPE`, `FLOAT_PROVIDER_TYPE`, `INT_PROVIDER_TYPE`.
- All `*Type` records removed. `CODEC` → `MAP_CODEC`. Singleton fields (`LootItemFunctions.INSTANCE`, etc.) removed — use each class's map codec.
- `IntProvider`/`FloatProvider` are interfaces: `getMinValue`/`getMaxValue` → `min`/`minInclusive` and `max`/`maxInclusive`.
- `CriterionValidator` → `ValidationContextSource` + `Validatable` with `validate(ValidationContext)`.

## 8. Items and attributes

- `SwordItem`, `DiggerItem`, `ArmorItem` **removed** (21.5) → `WEAPON`, `TOOL`, `ARMOR`, `BLOCKS_ATTACKS` components via `Item$Properties#component`.
- `DYE` component replaces dye logic: `new Item(new Item.Properties().component(DataComponents.DYE, DyeColor.WHITE))`.
- `FoodProperties` now has a `Builder`: `new FoodProperties.Builder().nutrition(1).saturationModifier(2f).alwaysEdible().build()`.
- New `EquipmentSlot` for saddles; `EntityEquipment` replaces per-slot lists; `equipOnInteract`.
- `AttributeType#toFloat`, `AttributeTypes.INTEGER`, `AttributeModifier.INTEGER_LIBRARY`, `EnvironmentAttribute*` additions.

## 9. Rendering — treat as its own project

Detailed in `rendering-26.1.md`. Headline changes:
- `BlockRenderDispatcher` and `ItemRenderer` **removed**.
- `ItemModel`/`BlockModel` accept a `Transformation`; `$Unbaked#bake` takes the parent `Matrix4fc`.
- `SpriteGetter` → `MaterialBaker`; raw texture locations → `Material` / `Material$Baked`.
- `BakedQuad` is no longer `int[]`-based; light/tint/overlay consolidated into `QuadInstance`; `putBulkData` → `putBlockBakedQuad` / `putBakedQuad`; `ModelBaker.PartCache` for cached vectors.
- `RenderType` → `RenderTypes`; core-shader JSON → `RenderPipeline`; depth → `DepthStencilState` (`DepthTestFunction` → `CompareOp`); blend/colour → `ColorTargetState`; `BlendOp` removed. `TRIPWIRE_BLOCK`/`TRIPWIRE_TERRAIN` pipelines removed.
- NeoForge added `MutableQuad` for building/modifying quads: `setCubeFaceFromSpriteCoords`, `setCubeFace`, `bakeUvsFromPosition`, `recalculateWinding`, `setSpriteAndMoveUv`.
- `RenderHighlightEvent` removed → `ExtractBlockOutlineRenderStateEvent`.
- Model JSON `neoforge_data` dropped `block_light`/`sky_light`; use `light_emission`.

## 10. Mod registration and FML

- `@Mod` client entry points construct before `Minecraft` (21.5) — no `Minecraft.getInstance()` during loading.
- `FMLEnvironment.dist` → `getDist()`; `.production` → `isProduction()`; `FMLLoader.getGamePath()` → `FMLLoader.getCurrent().getGameDir()` (21.9).
- `additionalRuntimeClasspath` is **disallowed** for MC ≥ 1.21.9 (MDG ≥ 2.0.111). Use `implementation` / `localRuntime`.
- Transfer API rework (21.9.1): `IItemHandler`, `IFluidHandler`, `IFluidHandlerItem`, `IEnergyStorage` and capabilities all changed. See <https://neoforged.net/news/21.9-transfer-rework/>.
- Key mapping categories are `KeyMapping.Category` records, must be registered via `RegisterKeyMappingsEvent.registerCategory` (21.9).
- Block entity save/load moved to `ValueInput`/`ValueOutput` (21.6): `saveAdditional(CompoundTag, HolderLookup.Provider)` → `saveAdditional(ValueOutput)`, `loadAdditional(ValueInput)`. Create with `TagValueOutput.createWithContext` / `TagValueInput.create`. Prefer `ValueIOSerializable`.
- `BlockBehaviour.Properties` needs `.setId(ResourceKey.create(Registries.BLOCK, id))` (21.2).
- `BlockEntityType.Builder` → `new BlockEntityType<>(...)` (21.2).
- Registry accessors renamed (21.2), see `registration.md`.
- `GatherDataEvent` split into `.Client` / `.Server`; Gradle task `data()` → `clientData()`; `includeClient()`/`includeServer()` removed (21.4).
- JSpecify nullability (21.11): `javax.annotation.Nullable` → `org.jspecify.annotations.Nullable`, **type-use only** (`Map.@Nullable Entry`).

## 11. Global vanilla renames worth a sweep

`Level#random` → `level.getRandom()` (now `protected`) · `ItemStack#getItemHolder` → `typeHolder` · `getTags` → `tags` · `TypedInstance` introduced · `Util` renamed (see the 1.21.11 primer).

## Explicitly NOT documented — do not guess

1. **`DeferredRegister` changes.** The primer is vanilla-only and the 26.1 notes say nothing about `DeferredRegister`. Treat it as unchanged unless proven otherwise.
2. **Networking payload API.** No documented change, but also no confirmation. Check the docs and the branch history.
3. **Resource pack folder renames.** Only entity textures moved into subdirectories (e.g. `textures/entity/panda/…`, files like `pig_cold`, `arrow_tipped`). No rename of `models/`, `blocks/`, `textures/`, `blockstates/`, `recipes/`. Use the Misode changelog for the full pack-layout diff.
4. **`FluidStack` / `FluidStackTemplate` method-level renames** — described conceptually only; check sources.
5. **Loot table / tag JSON details** for the unrolled codecs — renames are listed, per-JSON examples are not (except villager trades).
6. **Full `GuiGraphicsExtractor` method table** — only the naming patterns and a few examples are documented.

## Recommended migration workflow (from the 26.1 post)

Keep a working 1.21.1 / 1.21.11 workspace next to the in-progress 26.1 workspace:

1. Find a call that no longer compiles in 26.1.
2. Find the same call in the 1.21.x workspace.
3. Right-click it and jump to a **vanilla** usage (IntelliJ: Minecraft sources attached, search scope **All Places**).
4. Find the same vanilla usage in 26.1 and diff.
5. Apply the same change to your code.

Same method scales up to whole subsystems, e.g. learning how recipe-output datagen changed.
