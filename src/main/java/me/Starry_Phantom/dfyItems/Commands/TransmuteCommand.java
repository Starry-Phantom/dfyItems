package me.Starry_Phantom.dfyItems.Commands;

import me.Starry_Phantom.dfyItems.Core.FileManager;
import me.Starry_Phantom.dfyItems.DfyItems;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TransmuteCommand implements CommandExecutor {
    private final DfyItems PLUGIN;

    public TransmuteCommand(DfyItems plugin) {this.PLUGIN = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("dfyitems.transmute")) {
            sender.sendMessage(PLUGIN.getPermissionMessage());
            return true;
        }
        Player player;
        if (sender instanceof Player p) player = p;
        else {
            sender.sendMessage("You must be a player to use this command!");
            return true;
        }

        if (args.length != 1) return false;

        ItemStack item = player.getInventory().getItem(EquipmentSlot.HAND);
        if (!DfyItem.isValidItem(item)) {
            sender.sendMessage(PLUGIN.getErrorPrefix() + "You can only transmute dfyItems!");
            return true;
        }
        String id = DfyItem.getItemID(item);

        DfyItem target = FileManager.getItem(args[0].toUpperCase());
        if (target == null) {
            sender.sendMessage(PLUGIN.getErrorPrefix() + "Invalid item ID!");
            return true;
        }

        ItemStack transItem = DfyItem.transmuteItem(item, target);
        player.getInventory().setItemInMainHand(transItem);
        player.sendMessage(PLUGIN.getPrefix() + "Transmuted §6'" + id + "'§e to §6'" + target.getID() + "'");
        return true;
    }
}
