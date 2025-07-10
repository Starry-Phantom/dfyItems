package me.Starry_Phantom.dfyItems.itemStructure;

import me.Starry_Phantom.dfyItems.Core.TextUtilities;
import me.Starry_Phantom.dfyItems.Core.TriggerSlot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

public class DfyItem extends DfyStructure {
    private static final String ENCHANT_COLOR = "§d";
    private static final String NEGATIVE_STAT_COLOR = "§c";
    private static final String POSITIVE_STAT_COLOR = "§9+";

    private String name;
    private String rarity, type, model;
    private Material material;
    private String longLore, shortLore;
    boolean glint, glintNull, wearableNull;
    private ArrayList<DfyEnchantment> enchantments;
    private ArrayList<String> abilities;
    private ArrayList<Map<TriggerSlot, ArrayList<Map<String, Object>>>> stats;
    private EquipmentSlot wearable;
    private String equipSound;

    public DfyItem(File loadFile, int index) {
        super(loadFile, index);
        if (THROWN_LOAD_ERROR) return;
    }

    private ItemStack build() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);

        hideFlags(meta);
        addIDtoNBT(meta);

        setName(meta);
        setModel(meta);
        setGlint(meta);
        setWearable(meta);

        // AWAITING PAPER API UPDATE
        //setConsumable(meta)
        //setBlocking(meta)

        ArrayList<TextComponent> lore = new ArrayList<>();

        addShortLore(lore);
        addRarityDisplay(lore);

        addLongLore(lore);

        addEnchantments(lore);
        addEnchantmentNBT(meta);

        addAbilityLore(lore);
        addAbilityNBT(meta);

        // TODO: Mystic Enchants go here! (Adding also in a future update...)

        // TODO: Kill effects go here! (Adding in future update...)

        addStatLore(lore);
        addStatNBT(meta);

        if (lore.getLast().equals(Component.text(""))) lore.removeLast();
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void addStatNBT(ItemMeta meta) {
        if (stats == null) return;
        NamespacedKey topKey = new NamespacedKey(PLUGIN, "stats");

        PersistentDataContainer root = meta.getPersistentDataContainer();
        PersistentDataContainer statStorage = root.getAdapterContext().newPersistentDataContainer();

        for (Map<TriggerSlot, ArrayList<Map<String, Object>>> stat : stats) {
            TriggerSlot slot = stat.keySet().toArray(new TriggerSlot[0])[0];
            NamespacedKey key = new NamespacedKey(PLUGIN, slot.name());
            statStorage.set(key, PersistentDataType.STRING, statNBTBlock(stat));
        }

        root.set(topKey, PersistentDataType.TAG_CONTAINER, statStorage);
    }

    private String statNBTBlock(Map<TriggerSlot, ArrayList<Map<String, Object>>> statBlock) {
        ArrayList<String> statNBTBlock = new ArrayList<>();
        TriggerSlot slot = statBlock.keySet().toArray(new TriggerSlot[0])[0];


        for (Map<String, Object> stat : statBlock.get(slot)) {
            String key = stat.keySet().toArray(new String[0])[0];
            Object value = stat.get(key);

            String combinedValue = key + SUB_DELIMITER + value;

            statNBTBlock.add(combinedValue);
        }

        return String.join(DELIMITER, statNBTBlock);
    }

    private void addStatLore(ArrayList<TextComponent> lore) {
        if (abilities == null) return;
        for (Map<TriggerSlot, ArrayList<Map<String, Object>>> stat : stats) {
            lore.addAll(TextUtilities.insertIntoComponents(createStatBlock(stat)));
            lore.add(Component.text(""));
        }
    }

    private ArrayList<String> createStatBlock(Map<TriggerSlot, ArrayList<Map<String, Object>>> statBlock) {
        ArrayList<String> lore = new ArrayList<>();
        TriggerSlot slot = statBlock.keySet().toArray(new TriggerSlot[0])[0];

        String header;
        if (TriggerSlot.isIrregular(slot)) header = "§7When in slot " + slot.name().toLowerCase() + ":";
        else header = "§7When in "+ TextUtilities.toReadableCase(slot.name()) + ":";
        lore.add(header);

        for (Map<String, Object> stat : statBlock.get(slot)) {
            String key = stat.keySet().toArray(new String[0])[0];
            Object value = stat.get(key);

            String statColor;
            if (value instanceof Number) {
                if (((Number) value).doubleValue() > 0) statColor = POSITIVE_STAT_COLOR;
                else statColor = NEGATIVE_STAT_COLOR;
            } else continue;

            String statName = TextUtilities.toReadableCase(key);
            String specialChar = "";
            if (statName.contains(" Percent")) {
                statName = statName.replace(" Percent", "");
                specialChar = "%";
            }

            lore.add(" " + statColor + value + specialChar + " " + statName);
        }

        return lore;
    }

    private void addEnchantmentNBT(ItemMeta meta) {
        if (enchantments == null) return;
        NamespacedKey key = new NamespacedKey(PLUGIN, "enchantment");
        StringBuilder enchantBlock = new StringBuilder();
        for (int i = 0; i < enchantments.size(); i++) {
            DfyEnchantment e = enchantments.get(i);
            enchantBlock.append(e.getID());
            enchantBlock.append(SUB_DELIMITER);
            enchantBlock.append(e.getLevel());
            if (i < enchantments.size() - 1) enchantBlock.append(DELIMITER);
        }
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, enchantBlock.toString());

    }

    private void addEnchantments(ArrayList<TextComponent> lore) {
        if (enchantments == null) return;
        String enchantString = buildEnchantString();
        lore.addAll(TextUtilities.insertIntoComponents(TextUtilities.wrapText(enchantString, ENCHANT_COLOR)));
        lore.add(Component.text(""));
    }

    private String buildEnchantString() {
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

    private void setWearable(ItemMeta meta) {
        EquippableComponent eq = meta.getEquippable();
        if (!wearableNull && wearable == null) return;
        if (wearable == null) {
            eq.setSlot(EquipmentSlot.HAND);
            meta.setEquippable(eq);
            return;
        }
        eq.setSlot(wearable);

        if (equipSound != null) {
            NamespacedKey key = new NamespacedKey("minecraft", equipSound);
            PLUGIN.severe(key.toString());
            eq.setEquipSound(Registry.SOUNDS.get(key));
        }

        meta.setEquippable(eq);
    }

    private void setGlint(ItemMeta meta) {
        if (glintNull) return;
        PLUGIN.log(Boolean.toString(glintNull));
        PLUGIN.log(Boolean.toString(glint));

        meta.setEnchantmentGlintOverride(glint);
    }

    private void setModel(ItemMeta meta) {
        if (model == null) return;
        if (Material.valueOf(model) != null) {
            meta.setItemModel(new NamespacedKey("minecraft", model.toLowerCase()));
        }
        else {
            meta.setItemModel(new NamespacedKey("dfyitems","held." + model));
            if (!wearableNull) {
                EquippableComponent eq = meta.getEquippable();
                eq.setModel(new NamespacedKey("dfyItems", "worn." + model));
                meta.setEquippable(eq);
            }
        }
    }

    private void addIDtoNBT(ItemMeta meta) {
        NamespacedKey key = new NamespacedKey(PLUGIN, "id");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, STRUCTURE_ID);

    }

    private void addAbilityNBT(ItemMeta meta) {
        String abilityString = String.join(DELIMITER, abilities);
        NamespacedKey key = new NamespacedKey(PLUGIN, "abilities");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, abilityString);

    }

    private void setName(ItemMeta meta) {
        meta.customName(Component.text("§f" + name));
    }

    private static void hideFlags(ItemMeta meta) {
        meta.addItemFlags(
                ItemFlag.HIDE_ARMOR_TRIM,
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_STORED_ENCHANTS
        );
    }

    private void addAbilityLore(ArrayList<TextComponent> lore) {
        if (!abilities.isEmpty()) {
            for (String s : abilities) {
                DfyAbility ability = PLUGIN.getAbility(s);
                if (ability == null) {
                    PLUGIN.warn("Ability " + s + " not found!");
                    continue;
                }
                lore.addAll(TextUtilities.insertIntoComponents(ability.getLoreBlock()));
                lore.add(Component.text(""));
            }
        }
    }

    private void addLongLore(ArrayList<TextComponent> lore) {
        if (longLore != null) {
            lore.addAll(TextUtilities.insertIntoComponents(TextUtilities.wrapText(longLore)));
            lore.add(Component.text(""));
        }
    }

    private void addRarityDisplay(ArrayList<TextComponent> lore) {
        lore.add(Component.text("§8" +
                ((rarity != null) ? rarity : "null") + " §8" +
                ((type != null) ? type : "item")
        ));

        lore.add(Component.text(""));
    }

    private void addShortLore(ArrayList<TextComponent> lore) {
        if (shortLore != null) lore.add(Component.text("§7" + shortLore));
    }

    @Override
    protected boolean loadData() {

        if (!initField("material", null, Material.class)) {
            PLUGIN.warn("Material for " + STRUCTURE_ID + " is invalid!");
            return false;
        }

        abilities = new ArrayList<>();
        loadArrayList("abilities", abilities, String.class);

        wearableNull = initField("wearable", null, EquipmentSlot.class);

        initField("equipSound", null);
        if (equipSound == null) equipSound = "item.armor_equip.generic";

        initField("name", material.toString());
        glintNull = !initField("glint", false);

        loadEnchants();
        loadStats();

        for (String s : new String[]{"rarity", "type", "longLore", "shortLore", "model"}) {
            initField(s, "");
        }

        return true;
    }

    private void loadStats() {
        if (!data.containsKey("stats")) return;
        if (data.get("stats") == null) return;
        stats = new ArrayList<>();
        Map<String, ArrayList<Map<String, Object>>> statLoader = (LinkedHashMap) data.get("stats");
        for (String key : statLoader.keySet().toArray(new String[0])) {
            TriggerSlot slot = TriggerSlot.valueOf(key);

            if (slot == null) continue;

            Map<TriggerSlot, ArrayList<Map<String, Object>>> map = new LinkedHashMap<>();
            map.put(slot, statLoader.get(key));
            stats.add(map);
        }
    }

    private void loadEnchants() {
        if (!data.containsKey("enchantments")) return;
        if (data.get("enchantments") == null) return;
        enchantments = new ArrayList<>();
        ArrayList<Map<String, Object>> enchantLoader = (ArrayList) data.get("enchantments");
        for (Map<String, Object> enchant : enchantLoader) {
            String enchantName = (String) enchant.keySet().toArray()[0];
            Integer level = (Integer) enchant.get(enchantName);
            enchantments.add(new DfyEnchantment(enchantName, level));
        }
    }

    public ItemStack getItem() {
        if (THROWN_LOAD_ERROR) return null;
        return build();
    }

    public static boolean isValidItem(ItemStack item) {
        String ID = getItemID(item);
        return ID != null;
    }

    public static String getItemID(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(new NamespacedKey(PLUGIN,"id"), PersistentDataType.STRING);
    }

    public ArrayList<String> getAbilities() {
        return abilities;
    }
}
