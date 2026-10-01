---
name: Minecraft Mod (NeoForge 26.1)
description: Build, extend and debug Minecraft Java Edition mods for Minecraft 26.1.x on NeoForge with Gradle + ModDevGradle, Java 25. Use for any Minecraft modding task - project scaffolding, registries, blocks/items, block entities, menus/GUI, data components, networking payloads, configs, data generation, resource packs, tags, recipes, loot tables, rendering, mixins, access transformers, porting from 1.21.x to 26.1, build/run/debug errors, and "crash on startup" logs.
---

# Minecraft Modding — NeoForge for MC 26.1.x

Target stack for this project. Do not substitute 1.21.x knowledge.

| Thing | Value |
| --- | --- |
| Mod loader | **NeoForge** (not Forge, not Fabric) |
| Minecraft | **26.1.2** (year-based versioning, 2026 scheme) |
| NeoForge artifact | `net.neoforged:neoforge:26.1.2.112` |
| Java | **25** (toolchain), 64-bit JVM |
| Gradle | **9.2.1** wrapper (9.1.0+ required) |
| Gradle plugin | `net.neoforged.moddev` **2.0.148** (ModDevGradle) — or `net.neoforged.gradle.userdev` 7.1.39 (NeoGradle) |
| Docs | <https://docs.neoforged.net/docs/gettingstarted/> (the `26.1` version) |

Minecraft moved to calendar versioning in 2026: `26.1` "Tiny Takeover" → patch releases `26.1.1`, `26.1.2` → `26.2`. NeoForge versions are 4 components: `26.1.2.112` = MC `26.1.2` + NeoForge release `112`.

## Golden rule: never invent an API name

26.1 removed obfuscation, which means **official Mojang parameter names are real**, and Mojang also renamed a huge amount of stuff during the 1.21.2 → 26.1 cycle. Anything you remember from 1.21.x tutorials is unreliable.

Before writing a single line of mod code, resolve every unfamiliar symbol against real sources, in this order:

1. **Local decompiled Minecraft/NeoForge sources.** After the first successful Gradle sync, the patched sources exist on disk. Find them with `glob` / `grep` under the Gradle cache, e.g.
   `~/.gradle/caches/neoformruntime/**` or the IDE's `External Libraries`.
   With ModDevGradle the sources are in `minecraft-patched-<version>-merged.jar`; with NeoGradle in `ng_dummy_ng.net.neoforged:neoforge:<version>`.
2. **IDE navigation** (IntelliJ / Eclipse only are supported for source browsing): attach Minecraft sources, set search scope to **All Places**, jump to a vanilla usage of the method.
3. **Official docs** for 26.1: <https://docs.neoforged.net/docs/gettingstarted/>
4. **The porting primer** (huge, authoritative, vanilla-only): <https://github.com/neoforged/.github/blob/main/primers/26.1/index.md>

If you cannot verify a name, say so and read the source. Do not guess. A wrong name costs an entire decompile/sync cycle to discover.

## Where to read what

- `references/project-setup.md` — Gradle files, toolchain, MDK layout, run/build tasks, JAR output, dev environment (JDK 25 note).
- `references/registration.md` — registries, blocks, items, creative tabs, block entities, menus, data components, attachments, events, sidedness.
- `references/resources-datagen.md` — data generation, models, blockstates, textures, lang, tags, recipes, loot tables, pack layout.
- `references/networking-config.md` — network payloads, config specs.
- `references/rendering-26.1.md` — the new submission-based render pipeline, entity/BER renderers, GUI/screens.
- `references/migration-26.1.md` — the 1.21.x → 26.1 breaking-change checklist. **Read this before porting or before concluding a 1.21.x snippet cannot work.**
- `references/qol-mod-patterns.md` — patterns for "improve all aspects of the game" style mods: keybinds, mixins, access transformers, event-driven tweaks, compatibility, performance.
- `references/troubleshooting.md` — build/run/debug failure playbook and common error messages.
- `references/sources.md` — every canonical URL (docs, MDK repos, release notes, primer) plus established version facts. Fetch from here instead of guessing.

## Workflow

1. **Read the task.** Decide the surface area: content (blocks/items/recipes), mechanics (events/game logic), client QoL (GUI, keybinds, rendering), or a mix.
2. **Check `references/migration-26.1.md` first** for anything you remember from 1.21.x.
3. **Scaffold or extend** — keep `gradle.properties` as the single source of truth for `mod_id`, `mod_name`, `mod_version`, `mod_group_id`, `minecraft_version`, `minecraft_version_range`, `neo_version`. Never hardcode versions in `build.gradle`.
4. **Write code.** Follow the exact import paths in `references/registration.md`. Keep registration in dedicated `*Registry`-style holder classes, not in the main mod class.
5. **Generate data instead of hand-writing it** where a provider exists (models, blockstates, lang, tags, recipes, loot). See `references/resources-datagen.md`.
6. **Compile early and often:** `gradlew build` (or `compileJava`) before writing large chunks.
7. **Run and test:** `gradlew runClient` for visual, `gradlew runServer` for a dedicated-server smoke test. **Always test on a dedicated server** — a client-only mod that touches client classes on the server must crash there, and that is a bug you must catch.
8. **Report honestly.** If something was unverified, say so.

## Non-negotiables

- **`@Mod` id must equal `mod_id` in `gradle.properties` and `modId` in `neoforge.mods.toml`.** All three must match or the mod does not load.
- **mod id regex: `[a-z][a-z0-9_]{1,63}`.** Lowercase ASCII, no dashes, no uppercase.
- **Never touch client-only classes from common code.** Use `@Mod(value = ..., dist = Dist.CLIENT)`, `@EventBusSubscriber(value = Dist.CLIENT, modid = "...")`, or `DistExecutor`-style guards. Also: `Minecraft.getInstance()` is **not** available during mod loading (since 21.5) — use `FMLClientSetupEvent`.
- **`ResourceLocation` does not exist. Use `Identifier`** (`net.minecraft.resources.Identifier`, `Identifier.fromNamespaceAndPath` / `Identifier.parse`).
- **`new ItemStack(...)` requires loaded registries.** In data files, recipes, and any static context, use `ItemStackTemplate`; `null` means empty; convert with `template.create()` / `ItemStackTemplate.fromNonEmptyStack(stack)`.
- **Block entities use `ValueInput`/`ValueOutput`, not `CompoundTag`.** `saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)`.
- **Nullability is JSpecify:** `org.jspecify.annotations.Nullable` (type-use position, e.g. `Map.@Nullable Entry`), not `javax.annotation.Nullable`.
- **No Parchment** — official parameter names exist, delete `neoForge.parchment.*`.
- **No `additionalRuntimeClasspath`.** Use the `localRuntime` configuration for optional runtime-only deps (JEI, etc.).
- **Do not commit generated or build output:** `build/`, `run/`, `runs/`, `.gradle/`, `src/generated/`.
- Keep `org.gradle.jvmargs` at `-Xmx1G` unless profiling shows it is a bottleneck; bump via `-Dorg.gradle.jvmargs=-Xmx4G` on the command line rather than editing the file silently.
