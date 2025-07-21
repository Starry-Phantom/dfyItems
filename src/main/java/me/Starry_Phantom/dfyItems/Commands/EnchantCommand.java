package me.Starry_Phantom.dfyItems.Commands;

import me.Starry_Phantom.dfyItems.DfyItems;
import me.Starry_Phantom.dfyItems.itemStructure.DfyEnchantment;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnchantCommand implements CommandExecutor, TabCompleter {
    private final DfyItems PLUGIN;

    public EnchantCommand(DfyItems plugin) {this.PLUGIN = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!commandSender.hasPermission("dfyitems.enchant")) {
            commandSender.sendMessage(PLUGIN.getPermissionMessage());
            return true;
        }
        if (args.length == 0 || args.length > 3) return false;
        Player player;
        if (commandSender instanceof Player p) player = p;
        else {
            commandSender.sendMessage("You must be a player to use this command!");
            return true;
        }

        ItemStack item = player.getInventory().getItem(EquipmentSlot.HAND);
        if (!DfyItem.isValidItem(item)) {
            commandSender.sendMessage(PLUGIN.getErrorPrefix() + "You can only enchant dfyItems! (Use /minecraft:enchant for default enchantments)");
            return true;
        }

        boolean hasSpecification = switch (args[0].toLowerCase()) {
            case "remove", "add" -> true;
            default -> args.length > 2;
        };

        String enchant;
        if (hasSpecification) enchant = args[1].toUpperCase();
        else enchant = args[0].toUpperCase();

        int level = -1;
        try {
            if (args.length == 2 && !hasSpecification) level = Integer.parseInt(args[1]);
            if (args.length == 3) level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {return false;}

        if (args[0].equalsIgnoreCase("remove")) {
            player.sendMessage(PLUGIN.getPrefix() + "Removed enchantment §6'" + enchant + ":" + level + "'");
            if (DfyEnchantment.removeEnchantments(item, new ArrayList<>(List.of(new DfyEnchantment(enchant, level, false))))) {
                player.sendMessage(PLUGIN.getPrefix() + "A default enchantment was removed. This change will be erased upon transmuting the item.");
            }
        } else {
            if (level == -1) level = 1;
            DfyEnchantment.applyEnchantments(item, new ArrayList<>(List.of(new DfyEnchantment(enchant, level, false))));
            player.sendMessage(PLUGIN.getPrefix() + "Applied enchantment §6'" + enchant + ":" + level + "'");
        }

        player.getInventory().setItemInMainHand(item);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!commandSender.hasPermission("dfyitems.enchant")) return List.of();
        if (strings.length == 1) return List.of("enchant");
        if (strings.length == 2) return List.of("level");
        return List.of();
    }
}
