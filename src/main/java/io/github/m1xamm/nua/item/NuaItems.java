package io.github.m1xamm.nua.item;

import io.github.m1xamm.nua.Nua;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NuaItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Nua.MOD_ID);

    public static final DeferredItem<Item> STORM_HELMET = ITEMS.registerItem("storm_helmet",
            props -> new Item(props.humanoidArmor(ArmorMaterials.COPPER, ArmorType.HELMET)));

    private NuaItems() {
    }
}
