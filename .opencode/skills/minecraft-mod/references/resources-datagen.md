# Resources and data generation — NeoForge 26.1.2

## Run configuration

Gradle tasks: `runData`, `runClientData`, `runServerData`. The MDK `data` run passes:

```
--mod <mod_id> --all --output <abs path to src/generated/resources> --existing <abs path to src/main/resources>
```

Other supported args: `--existing-mod`, `--includeDev`, `--includeReports`.

`src/generated/resources` must be on the resources source set (the MDK does this). Generated files land there; do not hand-edit them.

`GatherDataEvent` is split into **`GatherDataEvent.Client`** and **`GatherDataEvent.Server`** (21.4). `event.includeClient()` / `includeServer()` are gone.

## Pack layout

Resource pack:
```
assets/<modid>/
    items/<registry_name>.json         <- 26.1 item definition, see below
    lang/en_us.json
    models/block/<name>.json
    models/item/<name>.json
    blockstates/<name>.json
    textures/block/<name>.png
    textures/item/<name>.png
    sounds.json  (+ sounds/<...>)
    particles/<name>.json
    equipment/<name>.json
```

Data pack:
```
data/<modid>/
    recipe/<name>.json               <- SINGULAR in 26.1
    advancement/<name>.json
    loot_table/blocks/<name>.json
    tags/item/<name>.json        tags/block/...  tags/fluid/...
    worldgen/...
    structures/*.nbt
    test_instance/*.json          test_environment/*.json   world_clock/*.json
```

> **Folder names were unpluralised in the 26.1 cycle**: `recipe/`, `advancement/`, `loot_table/`.
> A mod that ships `recipes/` loads nothing and produces no error at build time. Verify a folder
> name against the vanilla jar before assuming — list it with
> `jar tf <minecraft_26.1.2_client.jar> | Select-String "data/minecraft/recipe/"`.
> Entity textures also moved into per-entity subdirectories (`textures/entity/panda/…`).

`pack.mcmeta` is generated at runtime for mods — you do not write it. Only bundled datapacks injected via `AddPackFindersEvent` need a real one.

## Data providers

All extend `DataProvider`. Register with `event.createProvider(...)` on the appropriate `GatherDataEvent`.

| Provider | Purpose |
| --- | --- |
| `ModelProvider` | block + item models (`registerModels(BlockModelGenerators, ItemModelGenerators)`) |
| `LanguageProvider` | `en_us.json` |
| `LootTableProvider` | loot tables |
| `RecipeProvider` | `buildRecipes(...)` |
| `RecipePrioritiesProvider` | recipe ordering |
| `TagsProvider` / `KeyTagProvider` | `addTags(HolderLookup.Provider)` |
| `AdvancementProvider` | advancements |
| `GlobalLootModifierProvider` | global loot modifiers |
| `SoundDefinitionsProvider` | `sounds.json` |
| `SpriteSourceProvider` | GUI sprite sources |
| `ParticleDescriptionProvider` | particles |
| `EquipmentAssetProvider` | equipment assets (`net.minecraft.client.data.models`) |
| `DatapackBuiltinEntriesProvider` | datapack builtin entries |
| `JsonCodecProvider` | raw JSON |
| `PackMetadataGenerator` | `pack.mcmeta` |

Helpers on the event: `createProvider(...)`, `createDatapackRegistryObjects(...)`, `createBlockAndItemTags(...)`, `getGenerator()`, `getPackOutput()`, `getResourceManager(PackType)`, `getLookupProvider()`, `includeDev()`, `includeReports()`.

```java
public class ExampleModelProvider extends ModelProvider {
    public ExampleModelProvider(PackOutput output) {
        super(output, "examplemod");
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // ...
    }
}

@SubscribeEvent  // mod event bus
public static void gatherData(GatherDataEvent.Client event) {
    event.createProvider(ExampleModelProvider::new);
}
```

## Blockstates

`blockModels.blockStateOutput.accept(...)` with:
- `MultiVariantGenerator.dispatch(block, variant)` — single model for all states
- `MultiPartGenerator.multiPart(partialState)` — per-property/parts
- `PropertyDispatch.modify(...)` — per-property variant selection

`variant(...)` for rotation-sensitive models (`VariantGenerator.create(...)` with `applyRotation`).

## Recipes

`RecipeProvider` overrides `buildRecipes(...)`. 26.1 specifics:
- Recipe **results are `ItemStackTemplate`**, not `ItemStack`.
- `RecipeBuilder#getResult` is gone.
- `determineBookCategory` → `determineCraftingBookCategory`.
- `oreSmelting`/`oreBlasting`/`oreCooking`/`generic` need a `CookingBookCategory`.
- Custom recipe classes register a `RecipeSerializer` record with a `MapCodec` + `StreamCodec` (no more inner `Serializer` classes, no `register` method).
- `ShapelessRecipe`/`ShapedRecipe` take `Recipe.CommonInfo` + `CraftingRecipe.CraftingBookInfo`; the book's fields are `category` (`building`/`redstone`/`equipment`/`misc`) and optional `group`.
- `Ingredient.CODEC` is a `HolderSetCodec`, so an ingredient is a bare id string.

### Hand-written JSON (verified against the 26.1.2 jar)

Shapeless:
```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "equipment",
  "ingredients": [
    "minecraft:copper_helmet",
    "minecraft:lightning_rod"
  ],
  "result": { "id": "nua:storm_helmet", "count": 1 }
}
```

Shaped:
```json
{
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "key": { "X": "minecraft:copper_ingot" },
  "pattern": ["XXX", "X X"],
  "result": { "id": "minecraft:copper_helmet" }
}
```

Notes:
- Result is `{"id": ...}` — **not** `{"item": ...}`. `count` is optional, defaults to 1.
- An ingredient is a bare item id or a bare tag id (`"#minecraft:ingots/copper"`). No `{"item": ...}` wrapper.
- A `key` value may also be a list of alternatives.

## Item definitions (26.1 "Client Items")

Item models are no longer resolved from a blockstate. Every item needs a definition at
`assets/<ns>/items/<registry_name>.json`, keyed off `DataComponents#ITEM_MODEL`, which defaults to
the item's registry id. It points at a model under `assets/<ns>/models/item/`:

```json
{ "model": { "type": "minecraft:model", "model": "nua:item/storm_helmet" } }
```

```json
{ "parent": "minecraft:item/generated", "textures": { "layer0": "nua:item/storm_helmet" } }
```

Texture at `assets/<ns>/textures/item/storm_helmet.png`. Definition `type` can also be
`minecraft:select`, `minecraft:composite`, `minecraft:range_dispatch`, `minecraft:special`, or
`neoforge:fluid_container` — vanilla trimmable armour uses `select` on `minecraft:trim_material`.

**Forgetting the `assets/<ns>/items/` file is not a build error. It shows up in game as a missing
model.**

## Tags

`KeyTagProvider` subclasses, `addTags(HolderLookup.Provider)`. NeoForge adds `createBlockAndItemTags(...)` convenience. Plant support moved to `support_*` block/fluid tags in 26.1; override `canSurvive` or `VegetationBlock#mayPlaceOn`.

To join a vanilla tag, ship your own file at the vanilla path with `replace: false`:
```json
{ "replace": false, "values": ["nua:storm_helmet"] }
```

## Lang file

`assets/<modid>/lang/en_us.json`:
```json
{
  "itemGroup.yourmodid.my_tab": "Your Mod",
  "item.yourmodid.my_item": "My Item",
  "block.yourmodid.my_block": "My Block",
  "container.yourmodid.my_menu": "My Menu",
  "config.yourmodid.my_option": "My Option"
}
```
Config option keys use the `translation(...)` you set in the config builder, conventionally `modid.config.<name>`.

## GameTests

Data/datapack driven — no test annotations in 26.1.

Files: `data/<ns>/structure/*.nbt`, `data/<ns>/test_environment/*.json`, `data/<ns>/test_instance/*.json`, `data/<ns>/world_clock/*.json`.

Register the test function:
```java
public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTION =
        DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, "examplemod");

public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EXAMPLE_FUNCTION =
        TEST_FUNCTION.register("example_function", () -> ExampleFunctions::exampleTest);
```

Pure-code registration:
```java
@SubscribeEvent  // mod event bus
public static void registerTests(RegisterGameTestsEvent event) {
    Holder<TestEnvironmentDefinition<?>> environment =
            event.registerEnvironment(EXAMPLE_ENVIRONMENT.identifier(), new ExampleEnvironmentType(0, true));

    event.registerTest(EXAMPLE_TEST_INSTANCE.identifier(),
            new ExampleTestInstance(0, true, new TestData<>(
                    environment,
                    Identifier.fromNamespaceAndPath("examplemod", "example_structure"),
                    400, 50, true, Rotation.CLOCKWISE_90, true, 3, 1, false)));
}
```

`GameTestHelper` API: `succeed`, `succeedIf`, `succeedWhen`, `succeedOnTickWhen`, `runAtTickTime`, `runAfterDelay`, `onEachTick`, `absolutePos`, `relativePos`. Failure throws `GameTestAssertException`.

Run: `gradlew runGameTestServer` — exit code is the number of failed tests. Other run configs need the `neoforge.enableGameTest` system property; GameTest is otherwise on under the `/test` command (`run`, `runall`, `runclosest`, `runthese`, `runfailed`, `pos <var>`).

Classes: `TestData`, `TestEnvironmentDefinition<SavedDataType>` (`BuiltInRegistries.TEST_ENVIRONMENT_DEFINITION_TYPE`), `GameTestInstance` (`BuiltInRegistries.TEST_INSTANCE_TYPE`), `FunctionGameTestInstance`, `BlockBasedTestInstance`, `TestBlockMode`.

## Data storage API surface

`HolderLookup.Provider` is still the registry-lookup type. Value I/O is the block entity API — **not** `CompoundTag`.

NBT itself: `CompoundTag`, `ListTag`, `StringTag.valueOf`, `getIntOr`/`getStringOr`/`getDoubleOr`/`getListOrEmpty`/`getCompoundOrEmpty`, and optional-returning `getInt`/`getLong`/`getString`/`getIntArray`.

NBT-backed value IO: `TagValueOutput.createWithContext(ProblemReporter, HolderLookup.Provider)` + `#buildResult()`, `TagValueInput.create(reporter, lookupProvider, tag)`, `ProblemReporter.DISCARDING`, `RootFieldPathElement`.

Reserved block entity keys: `id`, `x`, `y`, `z`, `NeoForgeData`, `neoforge:attachments`.

> Note: there is **no `CustomData` class** in the 26.1 NBT / Value I/O / capabilities / attachments pages of the docs. If you need a generic NBT blob, verify the actual API in the decompiled sources rather than assuming.
