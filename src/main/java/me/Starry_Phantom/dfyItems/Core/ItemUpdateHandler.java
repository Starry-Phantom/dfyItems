package me.Starry_Phantom.dfyItems.Core;

import me.Starry_Phantom.dfyItems.DfyItems;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemUpdateHandler implements Listener {
    private final DfyItems PLUGIN;

    public ItemUpdateHandler(DfyItems plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline()) this.cancel();
                updateHandItems(p);
            }
        }.runTaskTimer(PLUGIN, 200, 200);
    }

    private void updateHandItems(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack mainHand = inv.getItemInMainHand();
        if (itemNeedsUpdate(mainHand)) {
            ItemStack newItem = DfyItem.updateItem(mainHand);
            if (mainHand.equals(inv.getItemInMainHand())) inv.setItemInMainHand(newItem);
        }

        ItemStack offHand = inv.getItemInOffHand();
        if (itemNeedsUpdate(offHand)) {
            ItemStack newItem = DfyItem.updateItem(offHand);
            if (mainHand.equals(inv.getItemInOffHand())) inv.setItemInMainHand(newItem);
        }
    }

    public static boolean itemNeedsUpdate(ItemStack item) {
        if (!DfyItem.isValidItem(item)) return false;


        String id = DfyItem.getItemID(item);
        if (DfyItem.getEpochOf(item) != FileManager.getItemEpoch(id)) return true;
        return DfyItem.getGlobalEpochOf(item) != FileManager.getGlobalEpoch();
    }
}
