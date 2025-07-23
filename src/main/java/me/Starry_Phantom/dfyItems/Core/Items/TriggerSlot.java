package me.Starry_Phantom.dfyItems.Core.Items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public static void setSlotItem(Player player, TriggerSlot slot, ItemStack newItem) {
        if (isIrregular(slot)) {
            int index = Arrays.asList(irregularSlots).indexOf(slot) + 6;
            player.getInventory().setItem(index - 1, newItem);
        } else {
            player.getInventory().setItem(EquipmentSlot.valueOf(slot.name()), newItem);
        }
    }

    public static void setSlotItem(Player player, TriggerSlot slot, ItemStack newItem, ItemStack validationItem) {
        PlayerInventory inv = player.getInventory();
        if (isIrregular(slot)) {
            int index = Arrays.asList(irregularSlots).indexOf(slot) + 6;
            if (Objects.equals(inv.getItem(index - 1), validationItem)) inv.setItem(index - 1, newItem);
        } else {
            EquipmentSlot eq = EquipmentSlot.valueOf(slot.name());
            if (Objects.equals(inv.getItem(eq), validationItem)) inv.setItem(eq, newItem);
        }
    }
}
