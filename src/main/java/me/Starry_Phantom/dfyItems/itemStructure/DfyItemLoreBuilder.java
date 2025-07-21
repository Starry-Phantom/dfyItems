package me.Starry_Phantom.dfyItems.itemStructure;

import me.Starry_Phantom.dfyItems.Core.FileManager;
import me.Starry_Phantom.dfyItems.InternalAbilities.EffectApplicator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class DfyItemLoreBuilder {
    private DfyItem item;
    private ArrayList<DfyEnchantment> enchantments;
    private ArrayList<DfyAbility> effects;

    public DfyItemLoreBuilder(ItemStack item) {
        this.item = DfyItem.getBaseItem(item);
        this.enchantments = DfyEnchantment.getDefaultEnchants(item);
        addEnchants(DfyEnchantment.getAppliedEnchants(item));
        effects(EffectApplicator.getEffects(item));
    }

    public DfyItemLoreBuilder(DfyItem item) {
        this.item = item;
        if (item.getEnchantments() != null) this.enchantments = new ArrayList<>(item.getEnchantments());
        else this.enchantments = null;
        if (item.getEffects() == null) this.effects = null;
        else this.effects = FileManager.getAbilities(item.getEffects());
    }

    public DfyItemLoreBuilder enchants(ArrayList<DfyEnchantment> enchantments) {
        this.enchantments = enchantments;
        return this;
    }

    public DfyItemLoreBuilder addEnchants(ArrayList<DfyEnchantment> enchantments) {
        if (this.enchantments == null) {
            this.enchantments = enchantments;
            return this;
        }
        this.enchantments.addAll(enchantments);
        DfyStructure.sortAlphabetical(enchantments);
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
            DfyStructure.sortAlphabetical(enchantments);
            DfyEnchantment.removeDuplicates(enchantments);
            lore.addAll(DfyItem.getEnchantmentLore(enchantments));
            lore.add(Component.text(""));
        }

        ArrayList<TextComponent> abilityLore = item.getAbilityLore();
        if (abilityLore != null) {
            lore.addAll(abilityLore);
            lore.add(Component.text(""));
        }

        if (effects != null) {
            lore.addAll(DfyItem.getEffectLore(effects));
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

    public DfyItemLoreBuilder effects(ArrayList<DfyAbility> effects) {
        this.effects = effects;
        return this;
    }

    public DfyItemLoreBuilder addEffects(ArrayList<DfyAbility> effects) {
        if (this.effects == null) {
            this.effects = effects;
            return this;
        }
        this.effects.addAll(effects);
        return this;
    }

    public DfyItemLoreBuilder addEffects(String[] effects) {
        if (this.effects == null) {
            this.effects = new ArrayList<>();
        }
        this.effects.addAll(FileManager.getAbilities(effects));
        return this;
    }
}
