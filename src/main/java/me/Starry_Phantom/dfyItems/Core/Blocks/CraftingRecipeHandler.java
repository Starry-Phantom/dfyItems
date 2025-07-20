package me.Starry_Phantom.dfyItems.Core.Blocks;

import me.Starry_Phantom.dfyItems.Core.FileManager;
import me.Starry_Phantom.dfyItems.itemStructure.BlockFunctions.DfyRecipe;
import me.Starry_Phantom.dfyItems.itemStructure.BlockFunctions.RecipeComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class CraftingRecipeHandler implements Listener {
    @EventHandler
    public void prepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();
        RecipeComponent[] recipe_matrix = RecipeComponent.convertToComponents(matrix);

        DfyRecipe recipe = FileManager.getRecipe(DfyRecipe.getHash(recipe_matrix, false));
        if (recipe == null) {
            RecipeComponent.sortAlphabetical(recipe_matrix);
            recipe = FileManager.getRecipe(DfyRecipe.getHash(recipe_matrix, true));
        }
        if (recipe == null) return;

        if (recipe.canBeCraftedFrom(recipe_matrix)) {
            if (recipe.transfersEnhancements()) inv.setResult(recipe.getResult(recipe_matrix));
            else inv.setResult(recipe.getPlainResult());
        }
    }

//    @EventHandler
//    public void craft(InventoryClickEvent event) {
//        CraftingInventory inv;
//        Player player;
//        if (event.getClickedInventory() instanceof CraftingInventory i) inv = i;
//        else return;
//
//        if (inv.getResult() == null) return;
//
//        if (event.getRawSlot() != 0) return;
//        event.setCancelled(true);
//
//        if (event.getViewers().getFirst() instanceof Player p) player = p;
//        else return;
//
//        ClickType click = event.getClick();
//        switch (click) {
//            case ClickType.LEFT, ClickType.RIGHT -> craftItems(player, CraftTarget.CURSOR, inv);
//            case ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> craftItems(player, CraftTarget.INVENTORY, inv);
//        }
//
//    }
//
//    private void craftItems(Player player, CraftTarget craftTarget, CraftingInventory inv) {
//        ItemStack cursorItem = player.getItemOnCursor();
//        if (craftTarget == CraftTarget.CURSOR && cursorItem.getType() != Material.AIR) {
//            ItemStack clone = inv.getResult().clone();
//            clone.setAmount(cursorItem.getAmount());
//            if (!cursorItem.equals(clone)) return;
//        }
//
//        int amount = -1;
//        ItemStack[] matrix = inv.getMatrix();
//        if (craftTarget == CraftTarget.INVENTORY) {
//            for (ItemStack item : matrix) {
//                if (item == null) continue;
//
//                if (amount == -1) amount = item.getAmount();
//                else amount = Math.min(amount, item.getAmount());
//            }
//        } else amount = 1;
//
//        ItemStack result = inv.getResult();
//        result.setAmount(amount);
//        System.out.println("A: " + result.getAmount());
//
//        if (cursorItem.getType() != Material.AIR && craftTarget == CraftTarget.CURSOR) {
//            int originalAmount = cursorItem.getAmount();
//            int maxStackSize = cursorItem.getMaxStackSize();
//            if (originalAmount == maxStackSize) return;
//            System.out.println("o: " + originalAmount + " m: " + maxStackSize + " a: " + amount);
//            if (originalAmount + amount > maxStackSize) amount = maxStackSize - originalAmount;
//            result.setAmount(originalAmount + amount);
//            System.out.println("o: " + originalAmount + " m: " + maxStackSize + " a: " + amount);
//            System.out.println("B: " + result.getAmount());
//        }
//        System.out.println("C: " + result.getAmount());
//
//        for (int i = 0; i < matrix.length; i++) {
//            ItemStack item = matrix[i];
//            if (item == null) continue;
//            // TODO: Custom amount handling
//            int newAmount = item.getAmount() - amount;
//            if (newAmount == 0) matrix[i] = null;
//            else item.setAmount(newAmount);
//        }
//
//        System.out.println("D: " + result.getAmount());
//
//        inv.setResult(null);
//        inv.setMatrix(matrix);
//
//        System.out.println("E: " + result.getAmount());
//
//        switch (craftTarget) {
//            case CraftTarget.INVENTORY -> player.getInventory().addItem(result);
//            case CraftTarget.CURSOR -> player.setItemOnCursor(result);
//        }
//
//        System.out.println("F: " + result.getAmount());
//
//    }
}
