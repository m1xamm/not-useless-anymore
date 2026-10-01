package io.github.m1xamm.nua.item;

import io.github.m1xamm.nua.Nua;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NuaItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Nua.MOD_ID);

    public static final DeferredItem<StormHelmetItem> STORM_HELMET = ITEMS.registerItem("storm_helmet",
            StormHelmetItem::new);

    private NuaItems() {
    }
}
