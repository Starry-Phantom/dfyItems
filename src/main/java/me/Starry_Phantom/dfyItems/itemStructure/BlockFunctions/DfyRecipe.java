package me.Starry_Phantom.dfyItems.itemStructure.BlockFunctions;

import me.Starry_Phantom.dfyItems.itemStructure.DfyStructure;

import java.io.File;
import java.util.*;

public class DfyRecipe extends DfyStructure {
    private boolean shapeless;
    private ArrayList<Object> recipe;
    private RecipeComponent[] recipe_9, recipe_4;

    public DfyRecipe(File loadFile, int index) {
        super(loadFile, index);
        createMatrices();
    }

    private void createMatrices() {
        recipe_9 = new RecipeComponent[9];
        int i = 0;
        for (i = 0; i < recipe.size(); i++) {
            Object o = recipe.get(i);
            if (o instanceof String s) {
                recipe_9[i] = new RecipeComponent(s, 1);
            }
            if (o instanceof Map<?,?> map) {
                String key = map.keySet().toArray(new String[0])[0];
                recipe_9[i] = new RecipeComponent(key, (int) map.get(key));
            }
        }
        if (!shapeless) Arrays.sort(recipe_9);

        if ((recipe.size() <= 5 && !shapeless) || (shapeless && recipe.size() <= 4)) {
            recipe_4 = new RecipeComponent[4];
            recipe_4[0] = recipe_9[0];
            recipe_4[1] = recipe_9[1];
            recipe_4[2] = recipe_9[3];
            recipe_4[3] = recipe_9[4];
        } else recipe_4 = null;
    }

    @Override
    protected boolean loadData() {
        recipe = new ArrayList<>();
        loadArrayList("recipe", recipe, Object.class);

        if (data.containsKey("shapeless")) {
            shapeless = (boolean) data.get("shapeless");
        } else shapeless = false;

        return true;
    }

    public int getRecipe4Hash() {
        if (recipe_4 == null) return -1;
        StringBuilder hashString = new StringBuilder();
        for (RecipeComponent r : recipe_4) {
            hashString.append(r);
        }
        return Objects.hash(hashString.toString());
    }

    public int getRecipe9Hash() {
        StringBuilder hashString = new StringBuilder();
        for (RecipeComponent r : recipe_9) {
            hashString.append(r);
        }
        return Objects.hash(hashString.toString());
    }

    public boolean has4Recipe() {
        return recipe_4 != null;
    }
}
