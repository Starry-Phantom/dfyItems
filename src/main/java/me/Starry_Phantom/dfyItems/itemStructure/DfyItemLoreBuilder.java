package me.Starry_Phantom.dfyItems.itemStructure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;

public class DfyItemLoreBuilder {
    private DfyItem item;
    private ArrayList<DfyEnchantment> enchantments;

    public DfyItemLoreBuilder(DfyItem item) {
        this.item = item;
        this.enchantments = item.getEnchantments();
    }

    public DfyItemLoreBuilder enchants(ArrayList<DfyEnchantment> enchantments) {
        this.enchantments = enchantments;
        return this;
    }

    public ArrayList<TextComponent> build() {
        ArrayList<TextComponent> lore = new ArrayList<>();

        TextComponent shortLore = item.getShortLore();
        if (shortLore != null) lore.add(shortLore);

        if (item.getRarity() != null && item.getType() != null) {
            lore.add(item.getRarityDisplayLore());
            lore.add(Component.text(""));
        }

        ArrayList<TextComponent> longLore = item.getLongLore();
        if (longLore != null) {
            lore.addAll(longLore);
            lore.add(Component.text(""));
        }

        if (enchantments != null) {
            lore.addAll(item.getEnchantmentLore(enchantments));
            lore.add(Component.text(""));
        }

        ArrayList<TextComponent> abilityLore = item.getAbilityLore();
        if (abilityLore != null) {
            lore.addAll(abilityLore);
            lore.add(Component.text(""));
        }

        // TODO: Mystic Enchants go here! (Adding also in a future update...)

        // TODO: Kill effects go here! (Adding in future update...)

        ArrayList<TextComponent> statLore = item.getStatLore();

        if (statLore != null) {
            lore.addAll(statLore);
        }

        while (!lore.isEmpty() && lore.getLast().content().isBlank()) lore.removeLast();

        return lore;
    }

}
