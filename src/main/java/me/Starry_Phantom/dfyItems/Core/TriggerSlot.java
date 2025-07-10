package me.Starry_Phantom.dfyItems.Core;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum TriggerSlot {
    HAND, OFF_HAND, FEET, LEGS, CHEST, HEAD, SEVEN, EIGHT, NINE;

    private final static TriggerSlot[] irregularSlots = new TriggerSlot[]{TriggerSlot.SEVEN, TriggerSlot.EIGHT, TriggerSlot.NINE};

    public static ItemStack getSlotItem(TriggerSlot slot, Player player) {
        if (isIrregular(slot)) {
            int index = Arrays.asList(irregularSlots).indexOf(slot) + 6;
            return player.getInventory().getItem(index - 1);
        } else {
            return player.getInventory().getItem(EquipmentSlot.valueOf(slot.name()));
        }
    }

    public static Map<TriggerSlot, ItemStack> getAllSlotItems(Player player) {
        TriggerSlot[] slots = TriggerSlot.values();
        Map<TriggerSlot, ItemStack> items = new HashMap<>();

        for (int i = 0; i < slots.length; i++) {
            items.put(slots[i], TriggerSlot.getSlotItem(slots[i], player));
        }
        return items;
    }

    public static boolean isIrregular(TriggerSlot slot) {
        return Arrays.asList(irregularSlots).contains(slot);
    }
}
