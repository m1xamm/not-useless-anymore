# Project setup — NeoForge 26.1.2

## Authoritative source: the official MDK

Two variants exist. **ModDevGradle (MDG) is the modern default** and is what the generator produces when asked for.

| Repo | Plugin |
| --- | --- |
| <https://github.com/NeoForgeMDKs/MDK-26.1.2-ModDevGradle> | `net.neoforged.moddev` 2.0.148 |
| <https://github.com/NeoForgeMDKs/MDK-26.1.2-NeoGradle> | `net.neoforged.gradle.userdev` 7.1.39 |

Other available MDKs for 26.1.x: `26.1`, `26.1.1`, `26.1.2`, and also `26.2`, `26.3` (for later porting).
Generator: <https://neoforged.net/mod-generator/>

When any build file is uncertain, diff against the real MDK instead of guessing.

## MDK file tree (ModDevGradle variant)

```
build.gradle
settings.gradle
gradle.properties
gradlew / gradlew.bat
gradle/wrapper/gradle-wrapper.properties
src/main/java/com/example/examplemod/
    ExampleMod.java          <- @Mod entry point
    ExampleModClient.java    <- @Mod(dist = Dist.CLIENT) entry point
    Config.java              <- ModConfigSpec
src/main/resources/
    assets/examplemod/lang/en_us.json
src/main/templates/
    META-INF/neoforge.mods.toml   <- NOTE: templates, not resources (MDG)
src/generated/resources/    <- datagen output (MDG and NeoGradle both)
```

Differences vs NeoGradle: NeoGradle keeps `META-INF/neoforge.mods.toml` in `src/main/resources` and expands it via a `tasks.withType(ProcessResources)` block; MDG keeps the template in `src/main/templates`, runs a `generateModMetadata` task, and adds `neoForge.ideSyncTask generateModMetadata` so the IDE re-runs it on project reload.

## `gradle.properties` (single source of truth)

```properties
org.gradle.jvmargs=-Xmx1G
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true

minecraft_version=26.1.2
minecraft_version_range=[26.1.2]
neo_version=26.1.2.112

mod_id=yourmodid
mod_name=Your Mod Name
mod_license=All Rights Reserved
mod_version=1.0.0
mod_group_id=com.yourname.yourmodid
```

- `minecraft_version` and `neo_version` **must agree** or the artifact cannot be resolved.
- `minecraft_version_range` is a Maven range. `[26.1.2]` means exactly 26.1.2. Use a range like `[26.1.2,26.2)` to allow later 26.1.x patches. Snapshots/pre-releases do not sort correctly in ranges.
- `mod_id` must match the regex `[a-z][a-z0-9_]{1,63}`.
- `mod_group_id` should match the top-level Java package.

## `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

Gradle 9.1.0+ is mandatory for 26.1. Distribution type `BIN`; switching to `ALL` downloads full sources and requires running the `wrapper` task twice.

## `build.gradle` (ModDevGradle, verified against MDK-26.1.2-ModDevGradle)

```groovy
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'net.neoforged.moddev' version '2.0.148'
    id 'idea'
}

tasks.named('wrapper', Wrapper).configure {
    distributionType = Wrapper.DistributionType.BIN
}

version = mod_version
group = mod_group_id

sourceSets.main.resources {
    srcDir('src/generated/resources')
    exclude("**/*.bbmodel")   // BlockBench files
    exclude("src/generated/**/.cache")
}

repositories { /* add extra maven repos here if a dependency needs one */ }

base {
    archivesName = mod_id
}

// Mojang ships Java 25 to end users in 26.1, so mods should target Java 25.
java.toolchain.languageVersion = JavaLanguageVersion.of(25)

neoForge {
    version = project.neo_version
    // accessTransformers = project.files('src/main/resources/META-INF/accesstransformer.cfg')  // optional

    runs {
        client {
            client()
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        server {
            server()
            programArgument '--nogui'
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        gameTestServer {
            type = "gameTestServer"
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        data {
            clientData()
            programArguments.addAll '--mod', project.mod_id, '--all', \
                '--output', file('src/generated/resources/').getAbsolutePath(), \
                '--existing', file('src/main/resources/').getAbsolutePath()
        }
        configureEach {
            systemProperty 'forge.logging.markers', 'REGISTRIES'
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        "${mod_id}" { sourceSet(sourceSets.main) }
    }
}

configurations {
    runtimeClasspath.extendsFrom localRuntime
}

dependencies {
    // Optional runtime-only dep (JEI example). NEVER use `additionalRuntimeClasspath`;
    // it is disallowed for MC >= 1.21.9.
    // compileOnly "mezz.jei:jei-<mc>-common-api:<ver>"
    // localRuntime "mezz.jei:jei-<mc>-neoforge:<ver>"
}

var generateModMetadata = tasks.register("generateModMetadata", ProcessResources) {
    var replaceProperties = [
            minecraft_version      : minecraft_version,
            minecraft_version_range: minecraft_version_range,
            neo_version            : neo_version,
            mod_id                 : mod_id,
            mod_name               : mod_name,
            mod_license            : mod_license,
            mod_version            : mod_version,
    ]
    inputs.properties replaceProperties
    expand replaceProperties
    from "src/main/templates"
    into "build/generated/sources/modMetadata"
}
sourceSets.main.resources.srcDir generateModMetadata
neoForge.ideSyncTask generateModMetadata

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}

idea {
    module {
        downloadSources = true
        downloadJavadoc = true
    }
}
```

Do **not** add a `net.neoforged:neoforge` dependency line: the MDG `neoForge { version = ... }` block supplies the compile classpath.

## `META-INF/neoforge.mods.toml` essentials

Located at `src/main/templates/META-INF/neoforge.mods.toml` under MDG.

```toml
license="${mod_license}"

[[mods]]
modId="${mod_id}"          # must match @Mod(...)
version="${mod_version}"
displayName="${mod_name}"
description='''
One line of summary.
'''

# Optional mixin config declaration:
#[[mixins]]
#config="${mod_id}.mixins.json"

# Optional access transformers (default location is picked up automatically):
#[[accessTransformers]]
#file="META-INF/accesstransformer.cfg"

[[dependencies.${mod_id}]]
    modId="neoforge"
    type="required"          # required | optional | incompatible | discouraged
    versionRange="[${neo_version},)"
    ordering="NONE"          # NONE | BEFORE | AFTER
    side="BOTH"              # BOTH | CLIENT | SERVER

[[dependencies.${mod_id}]]
    modId="minecraft"
    type="required"
    versionRange="${minecraft_version_range}"
    ordering="NONE"
    side="BOTH"
```

Other optional top-level keys: `issueTrackerURL`, `updateJSONURL`, `displayURL`, `logoFile`, `credits`, `authors`, `[[dependencies.<other>]]`, `services=["net.neoforged.neoforgespi.language.IModLanguageProvider"]`, `#[features.<modid>] openGLVersion="[3.2,)"`.

There is **no** `neoforge.mods.toml.toml`. And `pack.mcmeta` is generated at runtime for mods — only bundled datapacks added via `AddPackFindersEvent` need a real one.

## Commands

| Command | Effect |
| --- | --- |
| `gradlew build` | build JAR → `build/libs/<mod_id>-<mod_version>.jar` |
| `gradlew compileJava` | fast compile check |
| `gradlew runClient` | launch dev client (run dir `run/client` under MDG, `runs/client` under NeoGradle) |
| `gradlew runServer` | launch dedicated dev server |
| `gradlew runGameTestServer` | run all registered GameTests and exit; exit code = count of failed tests |
| `gradlew runData` / `runClientData` / `runServerData` | run data generation |
| `gradlew :tasks` | list everything the build offers |

The first sync downloads and decompiles all of Minecraft plus NeoForge sources. It can take a long time (up to an hour on a slow connection). That is expected — do not "fix" it by changing versions.

## Dedicated server test setup

`runs/server/eula.txt`: set `eula=true` or the server exits immediately.
`runs/server/server.properties`: set `online-mode=false` to join as the Dev player.

## Local environment notes (this machine)

- `JAVA_HOME` is `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\` — **Java 21**, which is too old to *run* 26.1.
- A **JDK 25 is installed** at `C:\Program Files\Java\jdk-25.0.3`.
- Gradle's toolchain support should auto-detect the installed JDK 25 for `languageVersion = 25`. If it cannot (Gradle only scans some locations, e.g. it does not read `C:\Program Files\Java` reliably), either:
  - point at it explicitly in `settings.gradle`:
    ```groovy
    java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }
    ```
    or add the JDK path via `org.gradle.java.installations.paths=C:/Program Files/Java/jdk-25.0.3` in `gradle.properties`, or
  - set `JAVA_HOME` to the JDK 25 path for the shell running Gradle.
- Verify before starting a long sync: `java -version`, and `gradlew -version` to see which JVM Gradle and the toolchain resolve to.
- No IntelliJ IDEA or Eclipse is installed. Source browsing then requires reading the decompiled sources in the Gradle cache directly (see the SKILL golden rule). Any other IDE also works for editing, but only IntelliJ/Eclipse have documented source integration.
