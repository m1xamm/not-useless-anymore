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
    lang/en_us.json
    models/block/<name>.json
    models/item/<name>.json
    blockstates/<name>.json
    textures/block/<name>.png
    textures/item/<name>.png
    sounds.json  (+ sounds/<...>)
    particles/<name>.json
    equipment/<name>.json
    neoforge/animations/entity/<path>.json    # NeoForge Gecko animations
```

Data pack:
```
data/<modid>/
    recipes/<name>.json
    advancements/<name>.json
    loot_tables/blocks/<name>.json
    tags/item/<name>.json        tags/block/...  tags/fluid/...
    worldgen/...
    structures/*.nbt
    test_instance/*.json          test_environment/*.json   world_clock/*.json
```

`pack.mcmeta` is generated at runtime for mods — you do not write it. Only bundled datapacks injected via `AddPackFindersEvent` need a real one.

Entity textures moved into per-entity subdirectories in 26.1 (`textures/entity/panda/…`). This is the only documented pack-layout rename — see `migration-26.1.md` §11.

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
| `EquipmentAssetProvider` | equipment assets |
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
- JSON `Ingredient` is a plain holder-set: `"minecraft:diamond"` or `"#c:ingots/copper"`.

## Tags

`KeyTagProvider` subclasses, `addTags(HolderLookup.Provider)`. NeoForge adds `createBlockAndItemTags(...)` convenience. Plant support moved to `support_*` block/fluid tags in 26.1; override `canSurvive` or `VegetationBlock#mayPlaceOn`.

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
