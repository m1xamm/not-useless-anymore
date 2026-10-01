# Networking and configuration — NeoForge 26.1.2

## Network payloads

Classes: `CustomPacketPayload`, `CustomPacketPayload.Type<T>`, `StreamCodec`, `ByteBufCodecs`, `IPayloadContext`, `RegisterPayloadHandlersEvent` (gives a `PayloadRegistrar`), `RegisterClientPayloadHandlersEvent`, `HandlerThread`, `PacketDistributor`, `ClientPacketDistributor`.

### Payload definition (verbatim from the docs)

```java
public record MyData(String name, int age) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MyData> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("mymod", "my_data"));

    public static final StreamCodec<ByteBuf, MyData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, MyData::name,
        ByteBufCodecs.VAR_INT,  MyData::age,
        MyData::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
```

### Registration (common class, mod event bus)

```java
@SubscribeEvent
public static void register(RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar registrar = event.registrar("1");
    registrar.playBidirectional(
        MyData.TYPE,
        MyData.STREAM_CODEC,
        ServerPayloadHandler::handleDataOnMain
    );
}
```

### Client handler (client-only class, mod event bus)

```java
@SubscribeEvent
public static void register(RegisterClientPayloadHandlersEvent event) {
    event.register(MyData.TYPE, ClientPayloadHandler::handleDataOnMain);
}
```

### Handler and thread choice

```java
public static void handleDataOnMain(final MyData data, final IPayloadContext context) {
    // ...
}

final PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.NETWORK);
```

`executesOn` returns a **new** registrar — reassign it; it affects all subsequent registrations on that object.

### Rules that bite

- Registration methods: `play*`, `configuration*`, `common*`; each with `*Bidirectional`, `*ToClient`, `*ToServer` variants.
- For `*ToClient` and `*Bidirectional`, the **client** handler must be registered on `RegisterClientPayloadHandlersEvent`. For `*ToServer`, the handler is the last argument of the common registration.
- Size limits: clientbound ≤ 1 MiB, serverbound < 32 KiB.
- Send: `ClientPacketDistributor.sendToServer(...)`, `PacketDistributor.sendToPlayer(...)`, `sendToPlayersTrackingChunk(...)`, `sendToAllPlayers(...)`.

## Configuration

`ModConfigSpec`, `ModConfigSpec.Builder`, `ModContainer#registerConfig`, `ModConfig.Type` (`STARTUP`, `CLIENT`, `COMMON`, `SERVER`), `IConfigScreenFactory`, `ConfigurationScreen`, `ModConfigEvent.Loading/Reloading/Unloading`.

### Spec (verbatim from the 26.1.2 MDK `Config.java`)

```java
package com.example.examplemod;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }
}
```

### Registration (in the mod constructor)

```java
public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
    // ...
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
}
```

### Config screen (client entry point)

```java
@Mod(value = ExampleMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ExampleModClient {
    public ExampleModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
```

Add translations for your config options to `en_us.json` (e.g. `config.yourmodid.my_option`), otherwise the generated screen shows raw keys.

### Builder methods worth knowing

`define`, `defineInRange`, `defineInList`, `defineList`, `defineListAllowEmpty`, `defineEnum`, and `comment(...)` / `translation(...)` chaining for GUI text.

### Config types

- `STARTUP` — loaded before the game starts; use for settings that must be known at load time.
- `COMMON` — both sides, must be **identical on server and client** for sync-safe values.
- `CLIENT` — client only (keybinds, GUI toggles).
- `SERVER` — per-world server values, synced to clients that need them.

For a QoL mod, split values deliberately: visual/toggle options → `CLIENT`, gameplay-balance options → `SERVER` or `COMMON`.

## Data-pack registry caveat

Because `ItemStack` construction now requires loaded registries, config validation that constructs stacks must be deferred to a setup phase, not static initialisation. `Config.validateItemName` above uses `BuiltInRegistries.ITEM.containsKey(Identifier.parse(...))`, which is safe.
