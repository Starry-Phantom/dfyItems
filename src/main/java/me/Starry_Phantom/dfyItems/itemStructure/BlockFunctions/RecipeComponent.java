package me.Starry_Phantom.dfyItems.itemStructure.BlockFunctions;

import me.Starry_Phantom.dfyItems.Core.FileManager;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RecipeComponent implements Comparable {
    private String id;
    private int amount;
    private ItemStack item;

    public RecipeComponent(String id, int amount) {
        this.id = id;
        this.amount = amount;
        item = null;
    }

    public RecipeComponent(ItemStack item) {
        this.id = DfyItem.getItemID(item);
        this.amount = item.getAmount();
        item = null;
    }

    public String getID() {
        return id;
    }

    public int getAmount() {
        if (id == null) return -1;
        return amount;
    }

    public ItemStack getItem() {
        if (id == null) return null;
        if (item == null) return FileManager.getItem(id).getItem();
        return item;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof RecipeComponent r) {
            return id.compareTo(r.getID());
        }
        return 0;
    }
}
