package me.Starry_Phantom.dfyItems.Commands;

import me.Starry_Phantom.dfyItems.Core.FileManager;
import me.Starry_Phantom.dfyItems.DfyItems;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GetCommand implements CommandExecutor, TabCompleter {
    private final DfyItems PLUGIN;

    public GetCommand(DfyItems plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!commandSender.hasPermission("dfyitems.get.access")) {
            commandSender.sendMessage(Component.text(PLUGIN.getPermissionMessage()));
            return true;
        }

        if (args.length > 3) return false;
        if (args.length > 2 && !commandSender.hasPermission("dfyitems.get.others")) {
            commandSender.sendMessage(Component.text(PLUGIN.getPermissionMessage()));
            return true;
        }
        if (args.length == 0) if (commandSender instanceof Player player) return itemSelectGUI(player);
        else {
            commandSender.sendMessage(Component.text("You are not a player! Run this as a player or specify one as target!"));
            return false;
        }

        Player player;
        int amount = -1;
        boolean senderSame = true;

        if (args.length > 1) {
            player = Bukkit.getPlayer(args[1]);
            try {
                amount = Math.clamp(Integer.parseInt(args[1]), 0, 64);
            } catch (NumberFormatException ignored) {}

            if (!commandSender.hasPermission("dfyitems.get.others")) {
                if (amount == -1) {
                    commandSender.sendMessage(Component.text(PLUGIN.getPermissionMessage() + "You do not have permission give others items directly!"));
                    return true;
                }
                if (commandSender instanceof Player p) {
                    player = p;
                } else {
                    commandSender.sendMessage(Component.text("You are not a player! Run this as a player or specify one as target!"));
                    return false;
                }
            }

            if (amount != -1 && player != null && args.length != 3) {
                commandSender.sendMessage(Component.text(PLUGIN.getPrefix() + "Warning: Amount is also detected to be a player! The plugin will presume that this is a player. If this is not the intended case, please specify both."));
            }

            if (player == null) {
                if (amount == -1) {
                    commandSender.sendMessage(Component.text(PLUGIN.getPrefix() + "Invalid player!"));
                    return true;
                } else if (commandSender instanceof Player p) {
                    player = p;
                } else {
                    commandSender.sendMessage(Component.text("You are not a player! Run this as a player or specify one as target!"));
                    return false;
                }
            } else {
                senderSame = false;
            }

        } else if (commandSender instanceof Player p) {
            player = p;
        } else {
            commandSender.sendMessage(Component.text("You are not a player! Run this as a player or specify one as target!"));
            return false;
        }

        if (args.length == 3) {
            try {
                amount = Math.clamp(Integer.parseInt(args[2]), 0, 64);
            } catch (NumberFormatException ignored) {}
        }

        if (amount == -1) amount = 1;

        String id = args[0];
        id = id.toUpperCase();
        DfyItem item = FileManager.getItem(id);
        if (item == null) {
            commandSender.sendMessage(Component.text(PLUGIN.getPrefix() + "No item found with ID §6'" + id + "'§e!"));
            return true;
        }
        if (!senderSame) {
            if (amount == 1) commandSender.sendMessage(Component.text(PLUGIN.getPrefix() + "Gave " + player.getName() + " §6'" + id + "'§e!"));
            else commandSender.sendMessage(Component.text(PLUGIN.getPrefix() + "Gave " + player.getName() + " " + amount + "x of " + " §6'" + id + "'§e!"));
        }
        if (amount == 1) player.sendMessage(Component.text(PLUGIN.getPrefix() + "You received §6'" + id + "'§e!"));
        else player.sendMessage(Component.text(PLUGIN.getPrefix() + "You received " + amount + "x of §6'" + id + "'§e!"));
        ItemStack itemStack = item.getItem();
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
        return true;
    }

    private boolean itemSelectGUI(Player player) {
        if (!player.hasPermission("dfyitems.get.gui")) {
            player.sendMessage(Component.text(PLUGIN.getPermissionPrefix() + "You do not have permission to use the item GUI!"));
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!commandSender.hasPermission("dfyitems.get.access")) return List.of();
        ArrayList<String> strings = new ArrayList<>();
        if (args.length == 1) strings.addAll(List.of(FileManager.getItems().keySet().toArray(new String[0])));
        else if (args.length <= 3) {
            if (args.length == 3) {
                try {
                    int amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {return strings;}
            }
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            for (Player p : players) strings.add(p.getName());
        }

        return strings;
    }
}
