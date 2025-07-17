package me.Starry_Phantom.dfyItems.itemStructure;

import me.Starry_Phantom.dfyItems.Core.TextUtilities;
import me.Starry_Phantom.dfyItems.DfyItems;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class DfyEnchantment extends DfyStructure {
    private String name;
    private int level;
    private final boolean MYSTICAL;

    public static String buildEnchantString(ArrayList<DfyEnchantment> enchantments) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < enchantments.size(); i++) {
            DfyEnchantment e = enchantments.get(i);
            builder.append(e.getName());
            builder.append(" ");
            builder.append(e.getRomanLevel());
            if (i < enchantments.size()-1) builder.append(", ");
        }
        return builder.toString();
    }

    public static ArrayList<DfyEnchantment> getEnchantments(ItemStack item, String type) {
        if (!(type.equals("default") || type.equals("applied"))) return null;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "enchantments"), PersistentDataType.TAG_CONTAINER);
        if (container == null) return null;

        String enchants = container.get(new NamespacedKey(PLUGIN, type), PersistentDataType.STRING);
        if (enchants == null) return null;
        if (enchants.isEmpty()) return new ArrayList<>();

        String[] enchantBlocks = enchants.split(TextUtilities.makeRegexSafe(DELIMITER));
        ArrayList<DfyEnchantment> enchantments = new ArrayList<>();
        for (String s : enchantBlocks) {
            String[] split = s.split(TextUtilities.makeRegexSafe(SUB_DELIMITER));
            enchantments.add(new DfyEnchantment(split[0], Integer.parseInt(split[1]), false));
        }

        return enchantments;
    }

    public static ArrayList<DfyEnchantment> getAppliedEnchants(ItemStack item) {
        return getEnchantments(item, "applied");
    }

    public static ArrayList<DfyEnchantment> getDefaultEnchants(ItemStack item) {
        return getEnchantments(item, "default");
    }

    public static void applyEnchantments(ItemStack item, ArrayList<DfyEnchantment> enchants) {
        if (!DfyItem.isValidItem(item)) return;
        if (enchants == null) return;
        if (enchants.isEmpty()) return;

        ArrayList<DfyEnchantment> allEnchants = getAppliedEnchants(item);
        if (allEnchants == null) allEnchants = new ArrayList<>();
        allEnchants.addAll(enchants);
        DfyStructure.sortAlphabetical(allEnchants);
        DfyItem.setAppliedEnchants(item, allEnchants);

        ArrayList<DfyEnchantment> defEnchants = DfyEnchantment.getDefaultEnchants(item);
        if (defEnchants != null) allEnchants.addAll(defEnchants);
        DfyEnchantment.sortAlphabetical(allEnchants);
        removeDuplicates(allEnchants);

        ItemMeta meta = item.getItemMeta();
        DfyItem baseItem = DfyItem.getBaseItem(item);
        if (!meta.hasEnchant(Enchantment.MENDING)) meta.addEnchant(Enchantment.MENDING, 1, true);

        ArrayList<TextComponent> lore = baseItem.buildLore(allEnchants);
        meta.lore(lore);

        item.setItemMeta(meta);
    }

    public static ArrayList<DfyEnchantment> getAppliedEnchants(ItemStack item, ArrayList<DfyEnchantment> enchantments) {
        ArrayList<DfyEnchantment> appliedEnchants = new ArrayList<>(enchantments);
        appliedEnchants.addAll(DfyEnchantment.getAppliedEnchants(item));
        DfyStructure.sortAlphabetical(appliedEnchants);

        removeDuplicates(appliedEnchants);
        return appliedEnchants;
    }

    private static void removeDuplicates(ArrayList<DfyEnchantment> appliedEnchants) {
        for (int i = 0; i < appliedEnchants.size() - 1; i++) {
            DfyEnchantment first = appliedEnchants.get(i);
            DfyEnchantment second = appliedEnchants.get(i + 1);
            if (first.equalsID(second)) {
                if (first.getLevel() >= second.getLevel()) appliedEnchants.remove(i + 1);
                else appliedEnchants.remove(i);
                i--;
            }
        }
    }

    public static boolean removeEnchantments(ItemStack item, ArrayList<DfyEnchantment> enchants) {
        if (!DfyItem.isValidItem(item)) return false;

        DfyItem base = DfyItem.getBaseItem(item);
        ArrayList<DfyEnchantment> appliedEnchants = DfyEnchantment.getAppliedEnchants(item);
        if (appliedEnchants == null) appliedEnchants = new ArrayList<>();
        ArrayList<DfyEnchantment> defEnchants = DfyEnchantment.getDefaultEnchants(item);
        if (defEnchants == null) defEnchants = new ArrayList<>();
        if (defEnchants.isEmpty() && appliedEnchants.isEmpty()) return false;
        boolean removedDefault = false;

        for (DfyEnchantment enchant : enchants) {
            removeEnchant(enchant, appliedEnchants);
            if (removeEnchant(enchant, defEnchants)) {
                removedDefault = true;
            }
        }

        ItemMeta meta = item.getItemMeta();
        ArrayList<DfyEnchantment> allEnchants = new ArrayList<>(appliedEnchants);
        allEnchants.addAll(defEnchants);
        meta.lore(base.buildLore(allEnchants));
        item.setItemMeta(meta);

        DfyItem.setAppliedEnchants(item, appliedEnchants);
        if (removedDefault) DfyItem.setDefaultEnchants(item, defEnchants);
        return removedDefault;
    }

    private static boolean removeEnchant(DfyEnchantment enchant, ArrayList<DfyEnchantment> appliedEnchants) {
        if (DfyStructure.contains(enchant, appliedEnchants)) {
            if (enchant.getLevel() == -1) {
                return DfyStructure.removeFromList(enchant, appliedEnchants) != null;
            }
            else {
                DfyEnchantment enchantment = DfyStructure.findFromList(enchant, appliedEnchants);
                if (enchantment == null) return false;
                if (enchant.getLevel() == enchantment.getLevel()) {
                    DfyStructure.removeFromList(enchant, appliedEnchants);
                    return true;
                }
            }
        }
        return false;
    }

    public static String buildEnchantNBTString(ArrayList<DfyEnchantment> enchantments) {
        StringBuilder enchantBlock = new StringBuilder();
        for (int i = 0; i < enchantments.size(); i++) {
            DfyEnchantment e = enchantments.get(i);
            enchantBlock.append(e.getID());
            enchantBlock.append(SUB_DELIMITER);
            enchantBlock.append(e.getLevel());
            if (i < enchantments.size() - 1) enchantBlock.append(DELIMITER);
        }
        return enchantBlock.toString();
    }

    public String getRomanLevel() {
        return TextUtilities.getRomanNumeral(level);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    @Override
    protected boolean loadData() {
        return false;
    }

    public DfyEnchantment(String id, int level, boolean mystical) {
        super(null, 0);
        this.STRUCTURE_ID = id;
        this.name = TextUtilities.toReadableCase(id);
        this.level = level;
        this.MYSTICAL = mystical;
    }
}
