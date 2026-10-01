package io.github.m1xamm.nua;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = Nua.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Nua.MOD_ID, value = Dist.CLIENT)
public class NuaClient {

    public NuaClient(IEventBus modBus) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }
}
