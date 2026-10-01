package io.github.m1xamm.nua.feature.lightning;

import io.github.m1xamm.nua.config.NuaConfig;
import io.github.m1xamm.nua.feature.armor.CopperArmor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import io.github.m1xamm.nua.Nua;

@EventBusSubscriber(modid = Nua.MOD_ID)
public final class LightningArmorEvents {

    @SubscribeEvent
    static void onStruckByLightning(EntityStruckByLightningEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (NuaConfig.HELMET_IMMUNITY.getAsBoolean() && CopperArmor.wearsHelmet(player)) {
            event.setCanceled(true);
        }

        grantEffects(player);
    }

    @SubscribeEvent
    static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getEntity() instanceof LightningBolt bolt)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        int radius = NuaConfig.ATTRACT_RADIUS.get();
        if (radius <= 0) {
            return;
        }

        double limit = (double) radius * radius;
        ServerPlayer closest = null;
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isSpectator() || !CopperArmor.wearsStormHelmet(player)) {
                continue;
            }
            double distance = player.distanceToSqr(bolt);
            if (distance <= limit) {
                limit = distance;
                closest = player;
            }
        }

        if (closest == null) {
            return;
        }

        bolt.setPos(closest.getX(), closest.getY(), closest.getZ());
        bolt.setCause(closest);
    }

    private static void grantEffects(Player player) {
        int duration = NuaConfig.EFFECT_DURATION.get();
        if (duration <= 0) {
            return;
        }

        int amplifier = NuaConfig.EFFECT_AMPLIFIER.get();
        if (NuaConfig.CHESTPLATE_RESISTANCE.getAsBoolean() && CopperArmor.wearsChestplate(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration, amplifier));
        }
        if (NuaConfig.LEGGINGS_HASTE.getAsBoolean() && CopperArmor.wearsLeggings(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, duration, amplifier));
        }
        if (NuaConfig.BOOTS_SPEED.getAsBoolean() && CopperArmor.wearsBoots(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, amplifier));
        }
    }

    private LightningArmorEvents() {
    }
}
