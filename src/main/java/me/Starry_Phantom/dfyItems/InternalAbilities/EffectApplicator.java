package me.Starry_Phantom.dfyItems.InternalAbilities;

import me.Starry_Phantom.dfyItems.Core.TextUtilities;
import me.Starry_Phantom.dfyItems.Core.TriggerCase;
import me.Starry_Phantom.dfyItems.Core.TriggerSlot;
import me.Starry_Phantom.dfyItems.itemStructure.DfyAbility;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import me.Starry_Phantom.dfyItems.itemStructure.DfyStructure;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectApplicator {
    public static final String STRUCTURE_ID = "applicator";
    private static DfyAbility ability;

    public static void trigger(Event e) {
        PlayerToggleSneakEvent event;
        if (e instanceof PlayerToggleSneakEvent ev) event = ev;
        else return;

        if (!event.isSneaking()) return;
        applyEffectToEvent(event);
    }

    public static DfyAbility getAbilityObject() {
        if (ability != null) return ability;
        String displayName = null;
        String lore = "Shift with this in your offhand to apply the following effect:";
        String path = null;
        String className = null;
        boolean enabled = true;
        String id = "applicator";
        ArrayList<TriggerSlot> slot = new ArrayList<>();
        slot.add(TriggerSlot.OFF_HAND);
        ArrayList<TriggerCase> triggerCase = new ArrayList<>();
        triggerCase.add(TriggerCase.CROUCH);

        ability = new DfyAbility(
                displayName, lore,
                slot, triggerCase,
                path, className, enabled,
                id);
        return ability;
    }

    private static void applyEffectToEvent(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        PlayerInventory inv = player.getInventory();
        ItemStack mainHandItem = inv.getItemInMainHand();
        ItemStack effect = inv.getItemInOffHand();

        if (!DfyItem.isValidItem(effect)) return;
        String[] abilities = DfyAbility.getItemAbilities(effect);
        if (!Arrays.asList(abilities).contains(STRUCTURE_ID)) return;

        if (mainHandItem.getType() == Material.AIR) {
            player.sendMessage("§cYou cannot apply an effect to air!");
            return;
        }

        if (!DfyItem.isValidItem(mainHandItem)) {
            player.sendMessage("§cYou cannot apply an to a normal item!");
            return;
        }

        if (mainHandItem.getAmount() > 1) {
            player.sendMessage("§cYou cannot apply an effect to more than 1 item!");
            return;
        }

        applyEffectToItem(player, mainHandItem, effect);
    }

    private static void applyEffectToItem(Player player, ItemStack item, ItemStack effect) {
        ItemStack newItem = addEffectToItem(item.clone(), effect.clone());

        if (newItem == null) {
            player.sendMessage("§cYou have already applied this effect to your item!");
            return;
        }

        PlayerInventory inv = player.getInventory();
        if (inv.getItemInMainHand().equals(item) && inv.getItemInOffHand().equals(effect)) {
            inv.setItemInMainHand(newItem);
            if (effect.getAmount() > 1) {
                effect.setAmount(effect.getAmount() - 1);
                inv.setItemInOffHand(effect);
            } else {
                inv.setItemInOffHand(new ItemStack(Material.AIR));
            }
        }
    }

    public static ItemStack addEffectToItem(ItemStack item, ItemStack effect) {
        String[] temp = DfyAbility.getItemAbilities(effect);
        if (temp == null || temp.length == 0) return null;
        String[] effects = new String[temp.length - 1];
        int offSet = 0;
        for (int i = 0; i < temp.length; i++) {
            if (temp[i].equals(STRUCTURE_ID)) {
                offSet = 1;
                continue;
            }
            effects[i - offSet] = temp[i];
        }

        return addEffectToItem(item, effects);

    }

    public static ItemStack addEffectToItem(ItemStack item, String[] newEffects) {
        String[] oldEffects = getEffects(item);
        ArrayList<String> applyEffects = new ArrayList<>();
        if (oldEffects == null) applyEffects.addAll(Arrays.asList(newEffects));
        else {
            List effects = Arrays.asList(oldEffects);
            for (String s : newEffects) {
                if (!effects.contains(s)) applyEffects.add(s);
            }
            if (applyEffects.isEmpty()) return null;
        }

        return applyEffects(item, applyEffects.toArray(new String[0]));
    }

    public static ItemStack applyEffects(ItemStack item, String[] effects) {
        NamespacedKey effectKey = new NamespacedKey("dfyitems", "effects");
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String effectBlock = container.get(effectKey, PersistentDataType.STRING);
        String newEffectBlock = String.join(DfyStructure.DELIMITER, effects);

        if (effectBlock == null) effectBlock = newEffectBlock;
        else effectBlock = String.join(DfyStructure.DELIMITER, effectBlock, newEffectBlock);

        container.set(effectKey, PersistentDataType.STRING, effectBlock);
        item.setItemMeta(meta);
        return item;
    }

    public static String[] getEffects(ItemStack item) {
        NamespacedKey effectKey = new NamespacedKey("dfyitems", "effects");
        String effectBlock = item.getItemMeta().getPersistentDataContainer().get(effectKey, PersistentDataType.STRING);

        if (effectBlock == null) return null;
        return effectBlock.split(TextUtilities.makeRegexSafe(DfyStructure.DELIMITER));
    }
}
