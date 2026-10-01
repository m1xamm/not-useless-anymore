# Not Useless Anymore

A Minecraft **26.1.2** mod for **NeoForge** that improves small parts of the game without
reinventing what already works.

## Current features

### Copper armour and lightning

The vanilla copper armour set gains behaviour. Nothing is replaced — the vanilla items are used
as-is, so copper armour from the world, from mobs, and from other mods all benefit.

Whenever a **real** lightning bolt strikes you:

| Piece | Effect |
| --- | --- |
| Copper helmet | Negates the strike entirely: no damage, no fire. Same as a lightning rod. |
| Copper chestplate | Resistance for 3 seconds |
| Copper leggings | Haste for 3 seconds |
| Copper boots | Speed for 3 seconds |

Each piece works on its own. The effects do not require a helmet — but without one you take the
damage, and being struck by lightning naturally is rare.

### Storm helmet

A separate item, crafted on purpose:

```
copper helmet + lightning rod  ->  nua:storm_helmet   (shapeless)
```

It behaves exactly like the copper helmet, plus it **attracts lightning**: when a bolt spawns
anywhere within the attraction radius, it is pulled to you. The rod on the helmet is only cosmetic.

This is the same mechanic a vanilla lightning rod uses — vanilla diverts strikes inside a 128
block sphere and takes them safely. The player is the rod.

## Configuration

`config/nua-common.toml`, all values in the common config so both sides agree:

| Key | Default | Meaning |
| --- | --- | --- |
| `helmetImmunity` | `true` | A copper helmet makes you immune to lightning: no damage, no fire |
| `attractRadius` | `128` | Blocks in which the storm helmet pulls lightning to you. `0` disables |
| `effectDuration` | `60` | Ticks each granted effect lasts. 20 = 1 second, `0` disables |
| `effectAmplifier` | `0` | Effect level minus one |
| `chestplateResistance` | `true` | Chestplate grants resistance |
| `leggingsHaste` | `true` | Leggings grant haste |
| `bootsSpeed` | `true` | Boots grant speed |

## Replacing the lightning rod model

The worn storm helmet currently uses a placeholder rod model, a plain copper post on the `head`
bone. Replace it with your own:

1. In BlockBench pick the **GeckoLib** model type and the **Armor** format, so you get a model with
   the `head` bone. GeckoLib attaches the bone to the matching armour segment
   (`HEAD, CHEST, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG, LEFT_FOOT, RIGHT_FOOT`).
2. Export and place the files **named after the item id**, which is `storm_helmet`:

```
src/main/resources/assets/nua/geckolib/models/armor/storm_helmet.geo.json
src/main/resources/assets/nua/geckolib/animations/armor/storm_helmet.animation.json
src/main/resources/assets/nua/textures/armor/storm_helmet.png
```

Note the texture goes to `textures/armor/`, **not** under `geckolib/`.

3. No code changes are needed. `StormHelmetItem` already implements `GeoItem` and
   `NuaClient#registerRenderers` already attaches a `GeoArmorRenderer`. To add an animation, fill
   in `StormHelmetItem#registerControllers` — it is currently empty because the model is static.

The inventory icon is separate and generated: `assets/nua/items/storm_helmet.json` and
`assets/nua/models/item/storm_helmet.json`, with the PNG produced by
`python tools/generate_textures.py`.

GeckoLib 5 is a hard dependency on **both** sides, not just the client: the item class itself
implements `GeoItem`, so a dedicated server cannot load the mod without it.

## Building

Requires **JDK 25** and network access on first build. The first sync downloads and decompiles
Minecraft, which takes a while.

```sh
gradlew build        # jar -> build/libs/nua-<version>.jar
gradlew runClient    # dev client
gradlew runServer    # dedicated server, needed before every release
```

`gradlew runServer` needs `eula=true` in `run/server/eula.txt` and `online-mode=false` in
`run/server/server.properties` to join as the dev player.

## Project layout

```
src/main/java/io/github/m1xamm/nua/
    Nua.java                        mod entry point, config and item registration
    NuaClient.java                  client entry point
    config/NuaConfig.java           all tunables
    item/NuaItems.java              registered items
    feature/armor/CopperArmor.java  what the player is wearing
    feature/lightning/             strike handling and attraction
src/main/resources/
    assets/nua/                     models, item definitions, lang, textures
    data/nua/recipe/                recipes
    data/minecraft/tags/item/       joins vanilla tags
tools/generate_textures.py          texture generator
```

## Version notes

26.1 is not compatible with 1.21.x mods as written. Notable changes this project relies on:

- `ArmorItem` is gone; armour is `Item.Properties#humanoidArmor(ArmorMaterial, ArmorType)`.
- `ResourceLocation` is now `Identifier`.
- `new ItemStack(...)` requires loaded registries, so data files use `ItemStackTemplate`.
- Recipes live in `data/<ns>/recipe/` (singular) and results are `{"id": ...}`.
- Item models need an `assets/<ns>/items/<id>.json` definition.
- Minecraft 26.1 requires Java 25 and no longer ships obfuscated.
