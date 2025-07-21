package me.Starry_Phantom.dfyItems.itemStructure.BlockFunctions;

import me.Starry_Phantom.dfyItems.Core.FileManager;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import me.Starry_Phantom.dfyItems.itemStructure.DfyStructure;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class DfyRecipe extends DfyStructure {
    private boolean shapeless;
    private ArrayList<Object> recipe;
    private RecipeComponent[] recipe_9, recipe_4;
    private String result;
    private int transferEnhancements, resultAmount;

    public DfyRecipe(File loadFile, int index) {
        super(loadFile, index);
        createMatrices();
    }

    public static int getHash(RecipeComponent[] recipeMatrix, boolean shapeless) {
        StringBuilder hashString = new StringBuilder();
        hashString.append(shapeless);
        for (RecipeComponent r : recipeMatrix) {
            hashString.append(r);
        }
        return Objects.hash(hashString.toString());
    }

    public static ItemStack[] toItemStackMatrix(RecipeComponent[] components) {
        ItemStack[] items = new ItemStack[components.length];
        for (int i = 0; i < components.length; i++) {
            items[i] = components[i].getItem();
            if (items[i] != null) items[i].setAmount(components[i].getAmount());
        }
        return items;
    }

    public static DfyRecipe getRecipe(RecipeComponent[] matrix) {
        DfyRecipe recipe = FileManager.getRecipe(DfyRecipe.getHash(matrix, false));
        if (recipe == null) {
            RecipeComponent[] sortedMatrix = new RecipeComponent[matrix.length];
            System.arraycopy(matrix, 0, sortedMatrix, 0, matrix.length);
            RecipeComponent.sortAlphabetical(sortedMatrix);
            recipe = FileManager.getRecipe(DfyRecipe.getHash(sortedMatrix, true));
        }
        return recipe;
    }

    private void createMatrices() {
        recipe_9 = new RecipeComponent[9];
        int i = 0;
        for (i = 0; i < recipe.size(); i++) {
            Object o = recipe.get(i);
            if (o == null) {
                recipe_9[i] = new RecipeComponent(null);
            }
            if (o instanceof String s) {
                recipe_9[i] = new RecipeComponent(s, 1);
            }
            if (o instanceof Map<?,?> map) {
                String key = map.keySet().toArray(new String[0])[0];
                recipe_9[i] = new RecipeComponent(key, (int) map.get(key));
            }
        }
        for (i = i; i < 9; i++) recipe_9[i] = new RecipeComponent(null);
        if (shapeless) RecipeComponent.sortAlphabetical(recipe_9);

        if ((recipe.size() <= 5 && !shapeless) || (shapeless && recipe.size() <= 4)) {
            recipe_4 = new RecipeComponent[4];
            int offSet = 0;
            for (int j = 0; j < 4; j++) {
                if (j > 1 && !shapeless) offSet = 1;
                recipe_4[j] = recipe_9[j + offSet];
            }
        } else recipe_4 = null;
    }

    @Override
    protected boolean loadData() {
        recipe = (ArrayList<Object>) data.get("recipe");

        if (data.containsKey("shapeless")) {
            shapeless = (boolean) data.get("shapeless");
        } else shapeless = false;

        initField("result", "");

        if (data.containsKey("transfer_enhancements")) transferEnhancements = (int) data.get("transfer_enhancements");
        else transferEnhancements = -1;

        if (data.containsKey("result_amount")) resultAmount = (int) data.get("result_amount");
        else resultAmount = 1;

        return true;
    }

    public int getRecipe4Hash() {
        if (recipe_4 == null) return -1;
        StringBuilder hashString = new StringBuilder();
        hashString.append(shapeless);
        for (RecipeComponent r : recipe_4) {
            hashString.append(r);
        }
        return Objects.hash(hashString.toString());
    }

    public int getRecipe9Hash() {
        StringBuilder hashString = new StringBuilder();
        hashString.append(shapeless);
        for (RecipeComponent r : recipe_9) {
            hashString.append(r);
        }
        return Objects.hash(hashString.toString());
    }

    public boolean has4Recipe() {
        return recipe_4 != null;
    }

    public ItemStack getPlainResult() {
        ItemStack retVal = FileManager.getItem(result).getItem();
        retVal.setAmount(resultAmount);
        return retVal;
    }

    public ItemStack getResult(RecipeComponent[] recipeMatrix) {
        if (transferEnhancements == -1) return getPlainResult();
        RecipeComponent transferItem = null;
        if (!shapeless) transferItem = recipeMatrix[transferEnhancements];
        else {
            Object o = recipe.get(transferEnhancements);
            String query;
            if (o instanceof String s) query = s;
            else if (o instanceof Map<?,?> map) query = map.keySet().toArray(new String[0])[0];
            else return getPlainResult();

            for (RecipeComponent comp : recipeMatrix) {
                if (comp.getItem() == null) continue;
                if (Objects.equals(comp.getID(), query)) transferItem = comp;
            }
            if (transferItem == null) return getPlainResult();
        }

        ItemStack retVal = DfyItem.transmuteItem(transferItem.getItem(), result);
        retVal.setAmount(resultAmount);
        return retVal;
    }

    public boolean transfersEnhancements() {
        return transferEnhancements != -1;
    }

    public boolean canBeCraftedFrom(RecipeComponent[] comparison) {
        RecipeComponent[] recipeMatrix;
        if (comparison.length == 4) {
            if (recipe_4 == null) return false;
            recipeMatrix = recipe_4;
        } else recipeMatrix = recipe_9;

        if (shapeless) {
            RecipeComponent[] sortedMatrix = new RecipeComponent[comparison.length];
            System.arraycopy(comparison, 0, sortedMatrix, 0, comparison.length);
            RecipeComponent.sortAlphabetical(sortedMatrix);
            comparison = sortedMatrix;
        }

        for (int i = 0; i < comparison.length; i++) {
            if (comparison[i].getAmount() < recipeMatrix[i].getAmount()) return false;
            ItemStack compItem = comparison[i].getItem();
            ItemStack thresholdItem = recipeMatrix[i].getItem();

            if (compItem == null && thresholdItem == null) continue;;
            if ((compItem == null && thresholdItem != null) || (compItem != null && thresholdItem == null)) return false;
            if (!DfyItem.getItemID(compItem).equals(DfyItem.getItemID(thresholdItem))) return false;
        }

        return true;
    }

    public int getMaxCraftable(RecipeComponent[] comparison) {
        RecipeComponent[] base;
        if (comparison.length == 4) base = recipe_4;
        else base = recipe_9;

        int amount = 1;
        for (int i = 0; i < comparison.length; i++) {
            if (comparison[i].getID() == null) continue;

            amount = Math.min(amount, comparison[i].getAmount() / base[i].getAmount());
        }
        return amount;
    }

    public void craft(RecipeComponent[] input, int amount) {
        RecipeComponent[] base;
        if (input.length == 4) base = recipe_4;
        else base = recipe_9;

        if (shapeless) {
            RecipeComponent[] sortedMatrix = new RecipeComponent[input.length];
            System.arraycopy(input, 0, sortedMatrix, 0, input.length);
            RecipeComponent.sortAlphabetical(sortedMatrix);
            input = sortedMatrix;
        }

        for (int i = 0; i < input.length; i++) {
            RecipeComponent item = input[i];
            if (item.getID() == null) continue;;

            int newAmount = item.getAmount();
            newAmount = newAmount - (base[i].getAmount() * amount);
            item.setAmount(newAmount);
        }

    }

    public int getResultAmount() {
        return resultAmount;
    }
}
