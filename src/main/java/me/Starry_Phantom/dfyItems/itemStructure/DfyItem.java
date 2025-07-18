package me.Starry_Phantom.dfyItems.itemStructure;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import me.Starry_Phantom.dfyItems.Core.FileManager;
import me.Starry_Phantom.dfyItems.Core.TextUtilities;
import me.Starry_Phantom.dfyItems.Core.TriggerSlot;
import me.Starry_Phantom.dfyItems.InternalAbilities.EffectApplicator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class DfyItem extends DfyStructure {
    private static final String ENCHANT_COLOR = "§r§d";
    private static final String NEGATIVE_STAT_COLOR = "§r§c";
    private static final String POSITIVE_STAT_COLOR = "§r§9+";

    private ItemStack item;
    private String name;
    private String rarity, type, model;
    private Material material;
    private String longLore, shortLore;
    boolean glint, glintNull;
    private ArrayList<DfyEnchantment> enchantments;
    private ArrayList<String> abilities, effects;
    private ArrayList<Map<TriggerSlot, ArrayList<Map<String, Object>>>> stats;

    private Map<String, Object> consumable, equippable;

    public DfyItem(File loadFile, int index) {
        super(loadFile, index);
        if (THROWN_LOAD_ERROR) return;
    }

    public static DfyItem getBaseItem(ItemStack item) {
        if (!isValidItem(item)) return null;
        return FileManager.getItem(getItemID(item));
    }

    public static void setEnchants(ItemStack item, ArrayList<DfyEnchantment> enchants, String type) {
        if (enchants == null) return;
        if (enchants.isEmpty()) return;
        ItemMeta meta = item.getItemMeta();

        NamespacedKey enchantKey = new NamespacedKey(PLUGIN, "enchantments");

        PersistentDataContainer root = meta.getPersistentDataContainer();
        PersistentDataContainer enchantContainer = root.get(enchantKey, PersistentDataType.TAG_CONTAINER);
        if (enchantContainer == null) {
            enchantContainer =  root.getAdapterContext().newPersistentDataContainer();
        }

        enchantContainer.set(new NamespacedKey(PLUGIN, type), PersistentDataType.STRING, DfyEnchantment.buildEnchantNBTString(enchants));

        root.set(enchantKey, PersistentDataType.TAG_CONTAINER, enchantContainer);
        item.setItemMeta(meta);
    }

    public static void setAppliedEnchants(ItemStack item, ArrayList<DfyEnchantment> enchants) {
        setEnchants(item, enchants, "applied");
    }

    public static void setDefaultEnchants(ItemStack item, ArrayList<DfyEnchantment> enchants) {
        setEnchants(item, enchants, "default");
    }

    public static int getEpochOf(ItemStack item) {
        if (!isValidItem(item)) return -1;
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "epochs"), PersistentDataType.TAG_CONTAINER).get(new NamespacedKey(PLUGIN, getItemID(item)), PersistentDataType.INTEGER);
    }

    public static int getGlobalEpochOf(ItemStack item) {
        if (!isValidItem(item)) return -1;
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(PLUGIN, "epochs"), PersistentDataType.TAG_CONTAINER).get(new NamespacedKey(PLUGIN, "GLOBAL"), PersistentDataType.INTEGER);
    }

    public static ItemStack rebuildLore(ItemStack item) {
        if (!isValidItem(item)) return item;

        DfyItem base = getBaseItem(item);
        ArrayList<TextComponent> lore = loreBuilder(item).build();
        ItemMeta meta = item.getItemMeta();
        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }


    private ItemStack build() {
        ItemStack item = new ItemStack(material);
        buildNBT(item);

        ItemMeta meta = item.getItemMeta();
        meta.lore(this.loreBuilder().build());
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack buildSkeleton() {
        ItemStack item = new ItemStack(material);
        buildNBT(item);
        return item;
    }

    public DfyItemLoreBuilder loreBuilder() {
        return new DfyItemLoreBuilder(this);
    }

    public static DfyItemLoreBuilder loreBuilder(ItemStack item) {
        return new DfyItemLoreBuilder(item);
    }

    private void buildNBT(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);

        addIDtoNBT(meta);

        setName(meta);
        setModel(meta);
        setGlint(meta);

        if (rarity != null && type != null) {
            addRarityNBT(meta);
        }


        if (enchantments != null) {
            addEnchantmentNBT(meta);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        }

        if (!abilities.isEmpty()) {
            addAbilityNBT(meta);
        }

        // TODO: Mystic Enchants go here! (Adding also in a future update...)

        if (stats != null) {
            addStatNBT(meta);
        }

        addEpochNBT(meta);

        item.setItemMeta(meta);

        setEquippable(item);
        setConsumable(item);
        EffectApplicator.addEffectToItem(item, effects.toArray(new String[0]));
        hideFlags(item);
    }

    private void addEpochNBT(ItemMeta meta) {
        PersistentDataContainer root = meta.getPersistentDataContainer();
        PersistentDataContainer epochs = root.getAdapterContext().newPersistentDataContainer();

        epochs.set(new NamespacedKey(PLUGIN, "GLOBAL"), PersistentDataType.INTEGER, FileManager.getGlobalEpoch());
        epochs.set(new NamespacedKey(PLUGIN, STRUCTURE_ID), PersistentDataType.INTEGER, FileManager.getItemEpoch(STRUCTURE_ID));
        root.set(new NamespacedKey(PLUGIN, "epochs"), PersistentDataType.TAG_CONTAINER, epochs);
    }

    private void setConsumable(ItemStack item) {
        if (consumable == null) return;

        Consumable.Builder c = Consumable.consumable();

        if (consumable.containsKey("consume_seconds")) {
            Number time = (Number) consumable.get("consume_seconds");
            c.consumeSeconds(time.floatValue());
        }

        if (consumable.containsKey("animation")) {
            ItemUseAnimation animation = ItemUseAnimation.valueOf((String) consumable.get("animation"));
            c.animation(animation);
        }

        if (consumable.containsKey("sound")) {
            c.sound(new NamespacedKey("minecraft", (String) consumable.get("sound")));
        }

        if (consumable.containsKey("has_consume_particles")) {
            c.hasConsumeParticles((Boolean) consumable.get("has_consume_particles"));
        }

        item.setData(DataComponentTypes.CONSUMABLE, c.build());
    }

    private void setEquippable(ItemStack item) {
        if (equippable == null && data.containsKey("equippable")) {
            item.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HAND));
            return;
        }
        if (equippable == null) return;

        if (!equippable.containsKey("slot")) {
            item.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HAND));
            return;
        }

        Equippable.Builder e = Equippable.equippable(EquipmentSlot.valueOf((String) equippable.get("slot")));

        if (equippable.containsKey("asset_id")) {
            String id = (String) equippable.get("asset_id");
            NamespacedKey key;
            if (Arrays.asList("diamond", "chainmail", "iron", "netherite", "leather", "turtle_scute", "gold", "elytra").contains(id)) {
                key = new NamespacedKey("minecraft", id);
            } else key = new NamespacedKey("dfyitems", id);

            e.assetId(key);
        }

        if (equippable.containsKey("equip_sound")) e.equipSound(new NamespacedKey("minecraft", (String) equippable.get("equip_sound")));
        if (equippable.containsKey("swappable")) e.swappable((Boolean) equippable.get("swappable"));

        item.setData(DataComponentTypes.EQUIPPABLE, e.build());
    }

    private void addRarityNBT(ItemMeta meta) {
        NamespacedKey key;
        if (rarity != null) {
            key = new NamespacedKey(PLUGIN, "rarity");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, rarity.toUpperCase());
        }
        if (type != null) {
            key = new NamespacedKey(PLUGIN, "type");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.toUpperCase());
        }
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

    public ArrayList<TextComponent> getStatLore() {
        if (stats == null) return null;
        ArrayList<TextComponent> lore = new ArrayList<>();
        for (Map<TriggerSlot, ArrayList<Map<String, Object>>> stat : stats) {
            lore.addAll(TextUtilities.insertIntoComponents(createStatBlock(stat)));
            lore.add(Component.text(""));
        }
        lore.removeLast();
        return lore;
    }

    private ArrayList<String> createStatBlock(Map<TriggerSlot, ArrayList<Map<String, Object>>> statBlock) {
        ArrayList<String> lore = new ArrayList<>();
        TriggerSlot slot = statBlock.keySet().toArray(new TriggerSlot[0])[0];

        String header;
        if (TriggerSlot.isIrregular(slot)) header = "§r§7When in slot " + slot.name().toLowerCase() + ":";
        else header = "§r§7When in "+ TextUtilities.toReadableCase(slot.name()) + ":";
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
        NamespacedKey topKey = new NamespacedKey(PLUGIN, "enchantments");

        PersistentDataContainer root = meta.getPersistentDataContainer();
        PersistentDataContainer enchantStorage = root.getAdapterContext().newPersistentDataContainer();

        NamespacedKey enchantmentKey = new NamespacedKey(PLUGIN, "default");
        enchantStorage.set(enchantmentKey, PersistentDataType.STRING, DfyEnchantment.buildEnchantNBTString(enchantments));
        enchantStorage.set(new NamespacedKey(PLUGIN, "applied"), PersistentDataType.STRING, "");

        root.set(topKey, PersistentDataType.TAG_CONTAINER, enchantStorage);

    }

    public static ArrayList<TextComponent> getEnchantmentLore(ArrayList<DfyEnchantment> enchantments) {
        if (enchantments == null) return null;
        String enchantString = DfyEnchantment.buildEnchantString(enchantments);
        return TextUtilities.insertIntoComponents(TextUtilities.wrapText(enchantString, ENCHANT_COLOR));
    }

    public static ArrayList<TextComponent> getEffectLore(ArrayList<DfyAbility> effects) {
        if (effects == null) return null;
        ArrayList<TextComponent> lore = new ArrayList<>();

        for (DfyAbility effect : effects) {
            lore.addAll(TextUtilities.insertIntoComponents(effect.getLoreBlock()));
            lore.add(Component.text("§8§l✔ §8Effect applied!"));
            lore.add(Component.text(""));
        }

        return lore;
    }

    private void setGlint(ItemMeta meta) {
        if (glintNull) return;
        meta.setEnchantmentGlintOverride(glint);
    }

    private void setModel(ItemMeta meta) {
        if (model == null) return;
        try {
            Material m = Material.valueOf(model);
            meta.setItemModel(new NamespacedKey("minecraft", model.toLowerCase()));
        } catch (IllegalArgumentException e) {
            meta.setItemModel(new NamespacedKey("dfyitems", model));
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
        meta.customName(TextUtilities.applyHexColoring("§r§f" + name));
    }

    private void hideFlags(ItemStack item) {
        TooltipDisplay.Builder c = TooltipDisplay.tooltipDisplay();

        c.addHiddenComponents(
                DataComponentTypes.BANNER_PATTERNS,
                DataComponentTypes.BLOCK_DATA,
                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER,
                DataComponentTypes.POTION_CONTENTS,
                DataComponentTypes.POTION_DURATION_SCALE,
                DataComponentTypes.CHARGED_PROJECTILES,
                DataComponentTypes.ENCHANTMENTS,
                DataComponentTypes.BASE_COLOR,
                DataComponentTypes.JUKEBOX_PLAYABLE,
                DataComponentTypes.DYED_COLOR,
                DataComponentTypes.FIREWORK_EXPLOSION,
                DataComponentTypes.FIREWORKS,
                DataComponentTypes.MAP_ID,
                DataComponentTypes.PROVIDES_TRIM_MATERIAL,
                DataComponentTypes.UNBREAKABLE,
                DataComponentTypes.WRITABLE_BOOK_CONTENT,
                DataComponentTypes.WRITTEN_BOOK_CONTENT,
                DataComponentTypes.BUNDLE_CONTENTS,
                DataComponentTypes.STORED_ENCHANTMENTS,
                DataComponentTypes.TRIM,
                DataComponentTypes.TOOL
        );

        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, c.build());
    }

    public ArrayList<TextComponent> getAbilityLore() {
        if (!abilities.isEmpty()) {
            ArrayList<TextComponent> lore = new ArrayList<>();
            for (String s : abilities) {
                DfyAbility ability = FileManager.getAbility(s);
                if (ability == null) {
                    PLUGIN.warn("Ability " + s + " not found!");
                    continue;
                }
                lore.addAll(TextUtilities.insertIntoComponents(ability.getLoreBlock()));
                lore.add(Component.text(""));
            }
            lore.removeLast();
            return lore;
        }
        return null;
    }

    public ArrayList<TextComponent> getLongLore() {
        if (longLore != null) return TextUtilities.insertIntoComponents(TextUtilities.wrapText(longLore));
        return null;
    }

    public TextComponent getRarityDisplayLore() {
        if (rarity == null && type == null) return null;
        return TextUtilities.applyHexColoring("§r§8" +
                ((rarity != null) ? rarity : "null") + " §r§8" +
                ((type != null) ? type : "item"));
    }

    public TextComponent getShortLore() {
        if (shortLore != null) return TextUtilities.applyHexColoring("§r§7" + shortLore);
        return null;
    }

    @Override
    protected boolean loadData() {

        if (!initField("material", null, Material.class)) {
            PLUGIN.warn("Material for " + STRUCTURE_ID + " is invalid!");
            return false;
        }

        abilities = new ArrayList<>();
        loadArrayList("abilities", abilities, String.class);

        effects = new ArrayList<>();
        loadArrayList("effects", effects, String.class);

        initField("name", material.toString());
        glintNull = !initField("glint", false);

        loadEnchants();
        if (enchantments != null) DfyStructure.sortAlphabetical(enchantments);

        loadStats();

        if (data.containsKey("consumable")) consumable = (Map<String, Object>) data.get("consumable");
        else consumable = null;

        if (data.containsKey("equippable")) equippable = (Map<String, Object>) data.get("equippable");
        else equippable = null;

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
            enchantments.add(new DfyEnchantment(enchantName, level, false));
        }
    }

    public ItemStack getItem() {
        if (THROWN_LOAD_ERROR) return null;
        if (item == null) item = build();
        return item;
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


    public static ItemStack transmuteItem(ItemStack item, String id) {
        return transmuteItem(item, FileManager.getItem(id));
    }

    public static ItemStack updateItem(ItemStack item) {
        if (!DfyItem.isValidItem(item)) return item;

        return transmuteItem(item, DfyItem.getBaseItem(item));
    }




    public static ItemStack transmuteItem(ItemStack item, DfyItem target) {
        if (!isValidItem(item)) {
            PLUGIN.warn("Failed to transmute item! Target is invalid!");
            return item;
        }

        if (!DfyItem.isValidItem(item)) return item;
        DfyItem base = target;

        ItemStack updateItem = base.buildSkeleton();

        ArrayList<DfyEnchantment> enchants = DfyEnchantment.getAppliedEnchants(item);
        DfyEnchantment.applyEnchantments(updateItem, enchants);

        EffectApplicator.addEffectToItem(updateItem, EffectApplicator.getEffectsAsStrings(item));

        updateItem.setAmount(item.getAmount());
        DfyItem.rebuildLore(updateItem);

        return updateItem;
    }

    public ArrayList<String> getAbilities() {
        return abilities;
    }

    public ArrayList<String> getEffects() {
        return effects;
    }

    public ArrayList<DfyEnchantment> getEnchantments() {
        return enchantments;
    }

    public String getRarity() {
        return rarity;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getRawLongLore() {
        return longLore;
    }

    public String getRawShortLore() {
        return shortLore;
    }

    public String getModel() {
        return model;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean isGlint() {
        return glint;
    }

    public boolean isGlintNull() {
        return glintNull;
    }

    public ArrayList<Map<TriggerSlot, ArrayList<Map<String, Object>>>> getStats() {
        return stats;
    }

    public Map<String, Object> getConsumable() {
        return consumable;
    }

    public Map<String, Object> getEquippable() {
        return equippable;
    }

    public boolean deepEquals(DfyItem item) {
        if (!Objects.equals(STRUCTURE_ID, item.getID())) return false;

        if (!Objects.equals(name, item.getName())) return false;
        if (!Objects.equals(type, item.getType())) return false;
        if (material != item.getMaterial()) return false;
        if (!Objects.equals(rarity, item.getRarity())) return false;
        if (!Objects.equals(longLore, item.getRawLongLore())) return false;
        if (!Objects.equals(shortLore, item.getRawShortLore())) return false;
        if (!Objects.equals(model, item.getModel())) return false;

        if (!Objects.equals(abilities, item.getAbilities())) return false;
        if (!Objects.equals(stats, item.getStats())) return false;
        if (!Objects.equals(effects, item.getEffects())) return false;

        if (!Objects.equals(enchantments, item.getEnchantments())) return false;
        if (glintNull != item.isGlintNull()) return false;
        if (glint != item.isGlint()) return false;

        if (!Objects.equals(equippable, item.getEquippable())) return false;
        if (!Objects.equals(consumable, item.getConsumable())) return false;

        return true;
    }
}
