package me.Starry_Phantom.dfyItems.itemStructure.BlockFunctions;

import me.Starry_Phantom.dfyItems.Core.FileManager;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import me.Starry_Phantom.dfyItems.itemStructure.DfyStructure;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class RecipeComponent {
    private String id;
    private int amount;
    private ItemStack item;

    public RecipeComponent(String id, int amount) {
        this.id = id;
        this.amount = amount;
        item = null;
    }

    public RecipeComponent(ItemStack item) {
        this.item = item;
        if (item == null) {
            id = null;
            amount = -1;
        } else {
            this.id = DfyItem.getItemID(item);
            this.amount = item.getAmount();
        }
    }

    public static void sortAlphabetical(RecipeComponent[] recipe) {
        int counter = 0;
        for (RecipeComponent recipeComponent : recipe) {
            if (recipeComponent.getID() == null) counter++;
        }
        RecipeComponent[] sort = new RecipeComponent[recipe.length - counter];
        counter = 0;
        for (int i = 0; i < recipe.length; i++) {
            if (recipe[i].getID() == null) {
                counter++;
                continue;
            }
            sort[i - counter] = recipe[i];
        }
        mergerHelper(sort, 0, sort.length / 2 - 1, sort.length / 2, sort.length - 1);
        for (int i = 0; i < recipe.length; i++) {
            if (i < sort.length) recipe[i] = sort[i];
            else recipe[i] = new RecipeComponent(null);
        }

    }

    private static void swap(RecipeComponent[] arr, int index1, int index2) {
        RecipeComponent swap = arr[index1];
        arr[index1] = arr[index2];
        arr[index2] = swap;
    }

    private static void mergerHelper(RecipeComponent[] arr, int s1, int e1, int s2, int e2) {
        if (e2 - s1 == 1) {
            int compareResult;
            if (arr[s1] == null) compareResult = 100;
            else if (arr[s2] == null) compareResult = -100;
            else compareResult = arr[s1].getID().compareTo(arr[s2].getID());
            if (compareResult > 0) swap(arr, s1, s2);
            return;
        }

        if (e2 - s1 <= 0) return;

        mergerHelper(arr, s1, (s1 + e1) / 2, (s1 + e1) / 2 + 1, e1);
        mergerHelper(arr, s2, (s2 + e2) / 2, (s2 + e2) / 2 + 1, e2);

        int l1p = s1;
        int l2p = s2;
        int tempIndex = 0;
        RecipeComponent[] temp = new RecipeComponent[e2-s1+1];
        while (l1p <= e1 && l2p <= e2) {
            int compareResult = 0;
            if (arr[s1] == null) compareResult = 100;
            else if (arr[s2] == null) compareResult = -100;
            else compareResult = arr[s1].getID().compareTo(arr[s2].getID());
            if (compareResult >= 0) {
                temp[tempIndex] = arr[l2p];
                l2p++;
            } else {
                temp[tempIndex] = arr[l1p];
                l1p++;
            }
            tempIndex++;
        }

        while (l1p <= e1) {
            temp[tempIndex] = arr[l1p];
            l1p++;
            tempIndex++;
        }

        while (l2p <= e2) {
            temp[tempIndex] = arr[l2p];
            l2p++;
            tempIndex++;
        }

        for (int i = s1; i <= e2; i++) {
            arr[i] = temp[i - s1];
        }
    }

    public static RecipeComponent[] convertToComponents(ItemStack[] matrix) {
        RecipeComponent[] retVal = new RecipeComponent[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            retVal[i] = new RecipeComponent(matrix[i]);
        }
        return retVal;
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

}
