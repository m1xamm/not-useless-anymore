package io.github.m1xamm.nua.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NuaConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue HELLMET_IMMUNITY = BUILDER
            .comment("Copper helmet negates lightning damage")
            .define("helmetImmunity", true);

    public static final ModConfigSpec.BooleanValue HELLMET_ABSORB_FIRE = BUILDER
            .comment("Copper helmet stops the player from catching fire when struck")
            .define("helmetAbsorbFire", true);

    public static final ModConfigSpec.IntValue ATTRACT_RADIUS = BUILDER
            .comment("Radius in blocks in which the copper helmet pulls lightning to the player")
            .defineInRange("attractRadius", 128, 0, 512);

    public static final ModConfigSpec.IntValue EFFECT_DURATION = BUILDER
            .comment("Duration of the granted effect in ticks, 20 ticks is 1 second")
            .defineInRange("effectDuration", 60, 0, 1200);

    public static final ModConfigSpec.IntValue EFFECT_AMPLIFIER = BUILDER
            .comment("Level of the granted effect minus one")
            .defineInRange("effectAmplifier", 0, 0, 255);

    public static final ModConfigSpec.BooleanValue CHESTPLATE_RESISTANCE = BUILDER
            .comment("Copper chestplate grants resistance when lightning strikes")
            .define("chestplateResistance", true);

    public static final ModConfigSpec.BooleanValue LEGGINGS_HASTE = BUILDER
            .comment("Copper leggings grant haste when lightning strikes")
            .define("leggingsHaste", true);

    public static final ModConfigSpec.BooleanValue BOOTS_SPEED = BUILDER
            .comment("Copper boots grant speed when lightning strikes")
            .define("bootsSpeed", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private NuaConfig() {
    }
}
