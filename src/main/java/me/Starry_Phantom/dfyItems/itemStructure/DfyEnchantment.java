package me.Starry_Phantom.dfyItems.itemStructure;

import me.Starry_Phantom.dfyItems.Core.TextUtilities;
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
        ArrayList<DfyEnchantment> allEnchants = getAppliedEnchants(item, enchants);
        allEnchants.addAll(DfyEnchantment.getDefaultEnchants(item));
        DfyEnchantment.sortAlphabetical(allEnchants);

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

        for (int i = 0; i < appliedEnchants.size() - 1; i++) {
            DfyEnchantment first = appliedEnchants.get(i);
            DfyEnchantment second = appliedEnchants.get(i + 1);
            if (first.equalsID(second)) {
                if (first.getLevel() >= second.getLevel()) appliedEnchants.remove(i + 1);
                else appliedEnchants.remove(i);
                i--;
            }
        }
        return appliedEnchants;
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
