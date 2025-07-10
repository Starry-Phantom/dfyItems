package me.Starry_Phantom.dfyItems.itemStructure;

import me.Starry_Phantom.dfyItems.Core.TextUtilities;

public class DfyEnchantment {
    private String name, ID;
    private int level;

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
    public String getID() {
        return ID;
    }

    public DfyEnchantment(String id, int level) {
        this.ID = id;
        this.name = TextUtilities.toReadableCase(id);
        this.level = level;
    }
}
