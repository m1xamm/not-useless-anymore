# Canonical sources

Fetch these rather than trusting memory or third-party tutorials. 1.21.x tutorials are actively wrong for 26.1.

## Official

| What | URL |
| --- | --- |
| Mod generator | <https://neoforged.net/mod-generator/> |
| 26.1 docs root | <https://docs.neoforged.net/docs/gettingstarted/> |
| 26.1 registries | <https://docs.neoforged.net/docs/concepts/registries> |
| 26.1 blocks | <https://docs.neoforged.net/docs/blocks/> |
| 26.1 items / data components | <https://docs.neoforged.net/docs/items/> |
| 26.1 entities | <https://docs.neoforged.net/docs/entities/> |
| 26.1 block entities | <https://docs.neoforged.net/docs/blockentities/> |
| 26.1 resources | <https://docs.neoforged.net/docs/resources/> |
| 26.1 inventories / containers | <https://docs.neoforged.net/docs/inventories/container> |
| 26.1 data storage (NBT, value I/O) | <https://docs.neoforged.net/docs/datastorage/nbt> |
| 26.1 worldgen | <https://docs.neoforged.net/docs/worldgen/biomemodifier> |
| 26.1 networking | <https://docs.neoforged.net/docs/networking/> |
| 26.1 rendering | <https://docs.neoforged.net/docs/rendering/feature> |
| 26.1 access transformers | <https://docs.neoforged.net/docs/advanced/accesstransformers> |
| 26.1 extensible enums | <https://docs.neoforged.net/docs/advanced/extensibleenums> |
| 26.1 feature flags | <https://docs.neoforged.net/docs/advanced/featureflags> |
| 26.1 config | <https://docs.neoforged.net/docs/misc/config> |
| 26.1 sidedness | <https://docs.neoforged.net/docs/concepts/sides> |
| 26.1 mod files | <https://docs.neoforged.net/docs/gettingstarted/modfiles> |
| 26.1 structuring your mod | <https://docs.neoforged.net/docs/gettingstarted/structuring> |
| 26.1 versioning | <https://docs.neoforged.net/docs/gettingstarted/versioning> |
| Older doc versions | prefix the path, e.g. <https://docs.neoforged.net/docs/1.21.11/gettingstarted/> |

The docs site has a version switcher in the top-left; if a page looks like 1.21.x, you are on the wrong version.

## MDK repositories

| Repo | Plugin |
| --- | --- |
| <https://github.com/NeoForgeMDKs/MDK-26.1.2-ModDevGradle> | ModDevGradle 2.0.148 |
| <https://github.com/NeoForgeMDKs/MDK-26.1.2-NeoGradle> | NeoGradle 7.1.39 |
| <https://github.com/NeoForgeMDKs> | all: 26.1, 26.1.1, 26.1.2, 26.2, 26.3 and every 1.20.x/1.21.x |

Raw file pattern, useful for diffing:
`https://raw.githubusercontent.com/NeoForgeMDKs/MDK-26.1.2-ModDevGradle/main/<path>`

## Migration / change history

| What | URL |
| --- | --- |
| 26.1 porting primer (huge, vanilla-only) | <https://github.com/neoforged/.github/blob/main/primers/26.1/index.md> |
| NeoForge for 26.1 release notes | <https://neoforged.net/news/26.1release/> |
| 1.21.11 notes | <https://neoforged.net/news/21.11release/> |
| 1.21.9 notes | <https://neoforged.net/news/21.9release/> |
| 1.21.9 transfer API rework | <https://neoforged.net/news/21.9-transfer-rework/> |
| 1.21.6 notes | <https://neoforged.net/news/21.6release/> |
| 1.21.5 notes | <https://neoforged.net/news/21.5release/> |
| 1.21.4 notes | <https://neoforged.net/news/21.4release/> |
| 1.21.2 notes | <https://neoforged.net/news/21.2release/> |
| 2025: Big Changes Are Coming | <https://neoforged.net/news/2025-retrospection/> |
| Mojang: obfuscation removed | <https://www.minecraft.net/en-us/article/removing-obfuscation-in-java-edition> |
| Mojang: new version numbering | <https://www.minecraft.net/en-us/article/minecraft-new-version-numbering-system> |
| Pack/data layout changelog | <https://misode.github.io/versions/?id=26.1&tab=changelog> |
| Fabric's 26.1 highlights (different angle) | <https://fabricmc.net/2026/03/14/261.html> |
| NeoForge artifacts | <https://projects.neoforged.net/neoforged/neoforge> |
| AT spec | <https://github.com/NeoForged/AccessTransformers/blob/main/FMLAT.md> |
| Update checker JSON spec | <https://docs.neoforged.net/docs/misc/updatechecker/> |
| Modding Discord | <https://discord.neoforged.net/> |

## Community (use with care, verify against sources)

- <https://github.com/neoforged/documentation> — docs source
- <https://github.com/neoforged/ModDevGradle> · <https://github.com/neoforged/NeoGradle>
- Modrinth / CurseForge for library APIs (JEI, REI, Cloth Config, Architectury)

## Version facts established for this project

- Minecraft 26.1 = "Tiny Takeover", released March 2026; 26.1.1 and 26.1.2 are hotfixes; 26.2 = "Chaos Cubed".
- Calendar-based versioning started in 2026: `<year>.<drop>.<patch>`. Bedrock keeps its own major numbering.
- 26.1 requires **Java 25**. Java is not optional.
- 26.1 removed obfuscation from Java Edition, so Mojang parameter names are authoritative.
- Gradle 9.1.0+ required; MDK ships 9.2.1.
- ModDevGradle 2.0.148 / NeoGradle 7.1.39 current at time of writing; check <https://github.com/neoforged/ModDevGradle> for newer.
- NeoForge `26.1.2.112` is the stable artifact for MC 26.1.2; the 26.1 line used `-beta` suffixes that are being dropped as the branch stabilises.
- Parchment is obsolete.
