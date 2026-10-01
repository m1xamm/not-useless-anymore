package io.github.m1xamm.nua.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NuaConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue HELMET_IMMUNITY = BUILDER
            .comment("A worn copper helmet makes the player immune to lightning, so no damage and no fire")
            .define("helmetImmunity", true);

    public static final ModConfigSpec.IntValue ATTRACT_RADIUS = BUILDER
            .comment("Radius in blocks in which the storm helmet pulls lightning to the player, 0 disables attraction")
            .defineInRange("attractRadius", 128, 0, 512);

    public static final ModConfigSpec.IntValue EFFECT_DURATION = BUILDER
            .comment("Duration in ticks of the granted effect, 20 ticks is 1 second, 0 disables effects")
            .defineInRange("effectDuration", 60, 0, 1200);

    public static final ModConfigSpec.IntValue EFFECT_AMPLIFIER = BUILDER
            .comment("Level of the granted effect minus one")
            .defineInRange("effectAmplifier", 0, 0, 255);

    public static final ModConfigSpec.BooleanValue CHESTPLATE_RESISTANCE = BUILDER
            .comment("Copper chestplate grants resistance when the player is struck")
            .define("chestplateResistance", true);

    public static final ModConfigSpec.BooleanValue LEGGINGS_HASTE = BUILDER
            .comment("Copper leggings grant haste when the player is struck")
            .define("leggingsHaste", true);

    public static final ModConfigSpec.BooleanValue BOOTS_SPEED = BUILDER
            .comment("Copper boots grant speed when the player is struck")
            .define("bootsSpeed", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private NuaConfig() {
    }
}
