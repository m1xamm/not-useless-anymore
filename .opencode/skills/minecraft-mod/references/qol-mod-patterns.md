# Patterns for a "mod that improves all aspects of the game"

A broad QoL / improvement mod is mostly event listeners, config gates, and careful sidedness — not new content. The failure mode to avoid is shipping something that works in the dev client and crashes or desyncs on a dedicated server or in another modpack.

## Architecture

```
src/main/java/com/<ns>/<modid>/
    <ModId>Mod.java              @Mod entry, config registration, event wiring
    <ModId>Client.java           @Mod(dist = Dist.CLIENT) entry
    config/                      one ModConfigSpec per domain
        GeneralConfig.java       toggles for everything
        ClientConfig.java        GUI, keybinds, visual toggles  (ModConfig.Type.CLIENT)
        ServerConfig.java        gameplay-balance values       (ModConfig.Type.SERVER)
    feature/                     one package/class per feature
        Feature.java             tiny interface: `boolean isEnabled(ServerConfig cfg)`
    compat/                      optional-dependency integration, all guarded
    event/                       @EventBusSubscriber gameplay listeners
    mixin/                       only where no event exists
    network/                     payload records
```

Rule: **one feature = one toggle**. Users of a broad mod will disable half of it. Every feature must be independently switchable at runtime without a restart where reasonable (`ModConfigEvent.Reloading`).

## Side-safety checklist (run this before every release)

- [ ] No client-only class is referenced from common code, even transitively. `net.minecraft.client.*` must not appear in any class reachable on the server.
- [ ] Event handlers touching client state are `@EventBusSubscriber(modid = "...", value = Dist.CLIENT)` and `static`.
- [ ] `Minecraft.getInstance()` only inside `FMLClientSetupEvent` or later — never during mod loading.
- [ ] Rendering and screens live only in `dist = Dist.CLIENT` classes.
- [ ] `runServer` with the mod enabled starts cleanly. This is mandatory, not optional.
- [ ] Config values read on the server are not read from a `CLIENT` config.

## Feature implementation order (lowest risk first)

1. **Data-only**: tags, recipes, loot tables, attributes, lang. No code, no crash risk, and the benefit is immediately visible. Start here.
2. **Event handlers**: block breaking/placement, player interact, item use, entity tick, living-damage, loot generation. Every one of these has a NeoForge event — check before writing a mixin.
3. **Replacement block/item/item-property classes** for changed behaviour, registered through the normal registries.
4. **Menu / screen** additions.
5. **Mixin** — last resort.

## Event-first rule

Before writing a mixin, confirm there is no event. The events index lives at <https://docs.neoforged.net/docs/> under Concepts/Miscellaneous. Known-good extension points include:

- Block: `BlockEvent.BreakEvent`, `BlockEvent.PlaceEvent`, `BlockEvent.NeighborNotifyEvent`, `BlockEvent.EntityPlaceEvent`
- Player/entity: `EntityLivingDropsEvent`, `LivingIncomingDamageEvent`/`LivingDamageEvent`, `PlayerEvent.PlayerLoggedInEvent`/`Clone`, `PlayerInteractEvent.*`, `EntityJoinLevelEvent`, `LivingChangeTickEvent`
- Server/tick: `ServerTickEvent.Post`, `LevelTickEvent.Post`, `ChunkEvent.Load`
- Loot: `LootTableLoadEvent`, `GlobalLootModifierProvider` (datagen)
- Creative: `BuildCreativeModeTabContentsEvent`
- Lifecycle: `FMLCommonSetupEvent`, `FMLClientSetupEvent`, `FMLLoadCompleteEvent`, `FMLConstructModEvent`
- Networking: `RegisterPayloadHandlersEvent`, `RegisterClientPayloadHandlersEvent`
- Rendering: `EntityRenderersEvent.*`, `BlockEntityRenderersEvent`, `RegisterRenderStateModifiersEvent`
- Config: `ModConfigEvent.Loading/Reloading/Unloading`
- Gametests: `RegisterGameTestsEvent`

Game event bus = `NeoForge.EVENT_BUS`; registry/datagen/lifecycle events go on the mod event bus.

## Tags: the highest-leverage tool for a broad mod

A huge fraction of "improve all aspects" features are just tag membership. No code, no crash surface, and pack authors can override it.

- Item tags: `#minecraft:logs`, `#c:ingots/*`, `#c:nuggets/*`, `#forge:ores/*` (confirm current tag namespace in 26.1 — NeoForge consolidated many tags under `c:`)
- Block tags: `#minecraft:mineable/*`, `#minecraft:logs`, `#forge:storage_blocks`
- `support_*` block/fluid tags for plant survival checks (26.1 moved plant support here)
- Read membership with `stack.is(TagKey)`; never with hardcoded item lists

## Mixins

- Declare in `neoforge.mods.toml`:
  ```toml
  [[mixins]]
  config="yourmodid.mixins.json"
  ```
- Config file with `"required": true`, `"minVersion"`, `"package"`, `"compatibilityLevel"`, `"refmap"`, `"mixins"`, `"client"`, `"server"`, and injectors with `defaultRequire`:
  ```json
  {
    "required": true,
    "minVersion": "0.8",
    "package": "com.example.mixin",
    "compatibilityLevel": "JAVA_21",
    "refmap": "yourmodid.refmap.json",
    "mixins": [],
    "client": ["com.example.mixin.ClientMixin"],
    "injectors": { "defaultRequire": 1 }
  }
  ```
- Always target a method that exists in 26.1. Renames like `interactAt` → `interact` break mixins silently, and a `defaultRequire` of 1 will surface them at startup.
- Keep mixins to `@Inject` at HEAD/RETURN with a small body. Redirect the rest into a normal overridable method or an event handler.
- Client/server separation goes in the `"client"` / `"server"` arrays, not in code.

## Access transformers

MDG auto-detects `src/main/resources/META-INF/accesstransformer.cfg`. For other locations, declare in `neoforge.mods.toml` and add to `build.gradle`:
```groovy
neoForge { accessTransformers.from 'src/additions/resources/accesstransformer_additions.cfg' }
```

Syntax:
```
# classes
public net.minecraft.util.Crypt$ByteArrayToKeyFunction
# fields (modifier may carry +f / -f to add/remove final)
protected-f net.minecraft.server.MinecraftServer random
# methods: descriptor syntax, slashes for reference types
public net.minecraft.Util makeExecutor(Ljava/lang/String;)Lnet/minecraft/TracingExecutor;
public net.minecraft.core.UUIDUtil leastMostToIntArray(JJ)[I
```

Descriptors: `B C D F I J S Z`, `[` per array dimension (`[[S` = `short[][]`), `L<class/name>;`, `(` params, `V` void.

**Caveat from the docs:** a directive only modifies the method it names; overriding methods are *not* transformed. Transforming a non-final method whose overrides stay restrictive causes a JVM error. Safest targets are `final` methods, `private` methods, and statics. Adding or changing an AT requires a Gradle project refresh.

## Optional mod dependencies

```toml
[[dependencies.yourmodid]]
    modId="jei"
    type="optional"
    versionRange="[1.21,)"
    ordering="AFTER"
    side="BOTH"
```

Then guard at runtime — never assume presence:
```java
if (ModList.get() != null && ModList.get().isLoaded("jei")) { /* reflectively or via a compileOnly API */ }
```
Declare the JEI API as `compileOnly` and the full artifact as `localRuntime` (see `project-setup.md`).

Prefer event and tag based integration over API calls — it degrades gracefully and survives upstream API churn.

## Performance discipline

- Broad mods run code on every tick, every block update, every damage event. Cache aggressively; do not allocate in hot paths.
- Bucket expensive checks: instead of scanning a `BlockPos` neighbourhood every tick, mark and sweep on a schedule.
- Do not run client-only lookups (ray tracing, block state lookups on the client) on the server.
- Respect `Dist` in every subscriber.
- Set `org.gradle.jvmargs` higher only when a profile says so; measure with the built-in profiler (`runClient` + Spark is a common optional `localRuntime` addition).

## Testing

- `gradlew runServer` — mandatory dedicated-server smoke test for every build.
- `gradlew runGameTestServer` for logic that is naturally expressible as a test (see `resources-datagen.md`).
- `gradlew runClient` for anything visual.
- For a broad mod, add a config that disables everything, so you can bisect which feature causes a report.
