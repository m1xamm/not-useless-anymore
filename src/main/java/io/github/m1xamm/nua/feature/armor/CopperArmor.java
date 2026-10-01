package io.github.m1xamm.nua.feature.armor;

import io.github.m1xamm.nua.item.NuaItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Reads what the player is actually wearing. Vanilla copper armor and the nua storm helmet share
 * the same behaviour, the storm helmet only adds lightning attraction.
 */
public final class CopperArmor {

    public static boolean wearsHelmet(Player player) {
        return wears(player, EquipmentSlot.HEAD, Items.COPPER_HELMET, NuaItems.STORM_HELMET.get());
    }

    public static boolean wearsStormHelmet(Player player) {
        return wears(player, EquipmentSlot.HEAD, NuaItems.STORM_HELMET.get());
    }

    public static boolean wearsChestplate(Player player) {
        return wears(player, EquipmentSlot.CHEST, Items.COPPER_CHESTPLATE);
    }

    public static boolean wearsLeggings(Player player) {
        return wears(player, EquipmentSlot.LEGS, Items.COPPER_LEGGINGS);
    }

    public static boolean wearsBoots(Player player) {
        return wears(player, EquipmentSlot.FEET, Items.COPPER_BOOTS);
    }

    private static boolean wears(Player player, EquipmentSlot slot, Item... accepted) {
        ItemStack stack = player.getItemBySlot(slot);
        for (Item item : accepted) {
            if (stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    private CopperArmor() {
    }
}
