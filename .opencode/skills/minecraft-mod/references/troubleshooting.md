# Build, run and debug playbook — NeoForge 26.1.2

## First sync

`gradlew build` / first `runClient` downloads and decompiles all of Minecraft plus NeoForge sources. Up to an hour on a slow connection. **This is normal.** Do not change versions or the toolchain to "fix" it.

## Verifying the toolchain before a long run

```
java -version
gradlew -version
```

- Gradle itself may run on JDK 21, but `java.toolchain.languageVersion = JavaLanguageVersion.of(25)` must resolve a JDK 25 for compiling and for `runClient`.
- This machine has JDK 21 as `JAVA_HOME` (`C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\`) and JDK 25 at `C:\Program Files\Java\jdk-25.0.3`. If Gradle cannot find it, set `org.gradle.java.installations.paths=C:/Program Files/Java/jdk-25.0.3` in `gradle.properties` or point `JAVA_HOME` at JDK 25.
- `gradlew -version` prints the JVM and the detected toolchains. Confirm the `25` toolchain is listed as detected/auto-detected.

## Gradle tasks

| Task | Use |
| --- | --- |
| `gradlew compileJava` | fast compile check while iterating |
| `gradlew build` | full build → `build/libs/<mod_id>-<mod_version>.jar` |
| `gradlew runClient` | dev client |
| `gradlew runServer` | dedicated dev server (needs `eula.txt` + `online-mode=false`) |
| `gradlew runGameTestServer` | run GameTests; exit code = failed test count |
| `gradlew runData` / `runClientData` / `runServerData` | data generation |
| `gradlew :tasks` | discover everything else |

## Debugging without an IDE

The full source jar is downloaded by the build, so sources are available even with no IDE:

- ModDevGradle: `minecraft-patched-<version>-merged.jar` under the Gradle cache
- NeoGradle: `ng_dummy_ng.net.neoforged:neoforge:<version>`

Locate them with `glob` on `~/.gradle/caches/**/minecraft-patched-*.jar` and `~/.gradle/caches/**/neoforge-*.jar`, then read/grep the sources. Only IntelliJ IDEA and Eclipse have documented source integration, so for a project of this size installing one is worth it.

## Reading a crash

1. The log is in the run directory: `run/client/logs/latest.log` (MDG) or `runs/client/logs/latest.log` (NeoGradle). For the server: the same under `server`.
2. `latest.log` is the full log. The crash report is `crash-reports/crash-*.txt`.
3. Read **top to bottom**: the first `Caused by:` is usually the real cause, the rest are consequences.
4. Distinguish failure classes:
   - **`NoSuchMethodError` / `NoClassDefFoundError` / `AbstractMethodError`** — you compiled against a different API version than the one running. Usually a stale Gradle cache or a wrong `neo_version`. Re-sync, and confirm `neo_version` matches the `net.neoforged:neoforge` artifact.
   - **`ClassNotFoundException: net.minecraft.client.*` on a server** — sidedness leak. See `qol-mod-patterns.md`.
   - **`ExceptionInInitializerError` in a registry class** — class-load ordering between your holder classes.
   - **Missing model/texture** (pink-black or missing model in-game) — resource path or namespace problem, not a crash. Check `assets/<modid>/models/...` and the registry id.
   - **Mixin apply failure** at startup — target method renamed in 26.1. Cross-check `migration-26.1.md`.
   - **Registration error** for a registry object — id mismatch, duplicate id, or `mod_id` ≠ `@Mod` value.
5. Enable more logging with the run-config system properties already in `build.gradle`:
   `forge.logging.markers=REGISTRIES` and `logLevel = org.slf4j.event.Level.DEBUG`.

## Common startup errors and causes

| Message | Cause |
| --- | --- |
| `Mod file ... is missing a neoforge.mods.toml` | file not at `src/main/templates/META-INF/` (MDG) and `generateModMetadata` not wired, or not in the resources source set |
| `Missing or unsupported mandatory dependencies` | `versionRange` in `neoforge.mods.toml` does not match the installed `neo_version` |
| `Failed to load class ... ItemStackTemplate` | stale cache; re-sync Gradle |
| `Attempted to create a new ItemStack before registries loaded` | used `new ItemStack(...)` in a static/datagen context — use `ItemStackTemplate` |
| `Registry Object not present` on `.get()` | accessed a `Deferred*` before registration, or from the wrong lifecycle phase |
| `ClassCastException: X cannot be cast to ... Block` | tagged/mapped wrong, or a block id resolves to a different class |
| `Duplicate registration` for an id | id declared twice across mod load |

## Performance

- Set `org.gradle.jvmargs=-Xmx1G` in `gradle.properties` (the MDK default). Override per-invocation with `-Dorg.gradle.jvmargs=-Xmx4G` rather than editing the file silently.
- `org.gradle.daemon`, `org.gradle.parallel`, `org.gradle.caching` and `org.gradle.configuration-cache` are all on in the MDK. Leave them.
- Modded client RAM: launch with more heap than the vanilla default; the dev launcher args live in the run config, not in `build.gradle`.
- Profile hot paths in-game before assuming a Gradle setting is the problem.

## Releasing

- `gradlew build` produces `build/libs/<mod_id>-<mod_version>.jar`. Confirm `neoforge.mods.toml` in the JAR has expanded values (no literal `${mod_id}`).
- `gradlew build` with datagen output committed means resources are present. Exclude `src/generated/**/.cache`.
- Always run `runServer` with the release JAR in a real NeoForge instance before publishing.

## Do not commit

`build/`, `run/`, `runs/`, `.gradle/`, `src/generated/`, IDE folders, `*.iml`, `.classpath`, `.project`, `.settings/`.
