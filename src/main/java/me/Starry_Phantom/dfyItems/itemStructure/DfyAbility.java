package me.Starry_Phantom.dfyItems.itemStructure;

import me.Starry_Phantom.dfyItems.Core.FileManager;
import me.Starry_Phantom.dfyItems.Core.TextUtilities;
import me.Starry_Phantom.dfyItems.Core.TriggerCase;
import me.Starry_Phantom.dfyItems.Core.TriggerSlot;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class DfyAbility extends DfyStructure {
    private String displayName, lore, path, className;
    private boolean enabled;
    ArrayList<TriggerSlot> triggerSlots;
    ArrayList<TriggerCase> triggerCases;

    public DfyAbility(File loadFile, int index) {
        super(loadFile, index);
        importData();
    }

    public boolean isEnabled() {return enabled;}

    public static void reloadAbilitiesWithPath(String path) {
        for (Object o : FileManager.getAbilities().values().toArray()) {
            DfyAbility ability = (DfyAbility) o;
            if (ability.getPath().equals(path)) {
                FileManager.getAbilityHandler().recompile(ability);
            }
        }
    }

    public ArrayList<String> getLoreBlock() {
        ArrayList<String> loreBlock = new ArrayList<>();
        if (displayName != null) loreBlock.add("§r§6" + displayName);
        if (lore != null && !lore.isEmpty()) loreBlock.addAll(TextUtilities.wrapText(lore, "§r§7"));
        return loreBlock;
    }

    public boolean equals(DfyAbility ability) {
        if (!Objects.equals(STRUCTURE_ID, ability.getID())) {
            return false;
        }

        if (!Objects.equals(displayName, ability.getDisplayName())) {
            return false;
        }

        if (!Objects.equals(lore, ability.getLore())) {
            return false;
        }

        return true;
    }

    public boolean deepEquals(DfyAbility ability) {
        if (!Objects.equals(STRUCTURE_ID, ability.getID())) {
            return false;
        }

        if (!path.equals(ability.getPath())) {
            return false;
        }
        if (!Objects.equals(displayName, ability.getDisplayName())) {
            return false;
        }

        if (!Objects.equals(lore, ability.getLore())) {
            return false;
        }

        if (!Objects.equals(triggerCases, ability.getTriggerCases())) {
            return false;
        }

        if (!Objects.equals(triggerSlots, ability.getTriggerSlots())) {
            return false;
        }

        if (enabled != ability.isEnabled()) {
            return false;
        }
        return true;
    }

    public String getPath() {
        return path;
    }

    @Override
    protected boolean loadData() {
        for (String s : new String[]{"displayName", "lore"}) {
            initField(s, "");
        }
        initField("enabled", false);

        triggerCases = new ArrayList<>();
        if (!loadEnumArrayList("triggerCase", triggerCases, TriggerCase.class)) {
            PLUGIN.severe("Trigger case for " + STRUCTURE_ID + " is invalid!");
            return false;
        }

        triggerSlots = new ArrayList<>();
        if (!loadEnumArrayList("triggerSlot", triggerSlots, TriggerSlot.class)) {
            PLUGIN.severe("Trigger slot for " + STRUCTURE_ID + " is invalid!");
            return false;
        }

        try {
            if (data.get("path") instanceof String s) {
                path = FileManager.getScriptFolder().getCanonicalPath() + s;
            } else {
                path = FileManager.getScriptFolder().getCanonicalPath() + File.separator + TextUtilities.toClassCase(STRUCTURE_ID) ;
            }
            path = TextUtilities.correctPath(path);
            className = new File(path).getName().replace(".java", "");
        } catch (IOException e) {return false;}

        return true;
    }

    public static String[] getItemAbilities(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(PLUGIN, "abilities");
        String abilities = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (abilities == null) return null;
        return abilities.split(TextUtilities.makeRegexSafe(DELIMITER));
    }

    public ArrayList<TriggerSlot> getTriggerSlots() {
        return triggerSlots;
    }

    public ArrayList<TriggerCase> getTriggerCases() {
        return triggerCases;
    }

    public String getClassName() {
        return className;
    }
    public String getLore() {
        return lore;
    }

    public String getDisplayName() {
        return displayName;
    }
}
