package io.github.m1xamm.nua;

import io.github.m1xamm.nua.config.NuaConfig;
import io.github.m1xamm.nua.item.NuaItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Nua.MOD_ID)
public class Nua {

    public static final String MOD_ID = "nua";

    public Nua(IEventBus modBus, ModContainer container) {
        NuaItems.ITEMS.register(modBus);
        container.registerConfig(ModConfig.Type.COMMON, NuaConfig.SPEC);
    }
}
