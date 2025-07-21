package me.Starry_Phantom.dfyItems.Commands;

import me.Starry_Phantom.dfyItems.Core.FileManager;
import me.Starry_Phantom.dfyItems.Core.StructureLoader;
import me.Starry_Phantom.dfyItems.Core.TextUtilities;
import me.Starry_Phantom.dfyItems.DfyItems;
import me.Starry_Phantom.dfyItems.itemStructure.BlockFunctions.DfyRecipe;
import me.Starry_Phantom.dfyItems.itemStructure.DfyAbility;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import me.Starry_Phantom.dfyItems.itemStructure.DfyStructure;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreCommands implements CommandExecutor, TabCompleter {
    private final DfyItems PLUGIN;

    public CoreCommands(DfyItems plugin) {
        this.PLUGIN = plugin;
    }

    /*
        dfi reload <path>
        dfi reload items
        dfi reload abilities
        dfi reload config
        dfi reload all

     */

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            if (commandSender instanceof Player player && player.hasPermission("dfyitems.get.gui")) return player.performCommand("/get");
            else return sendHelp(commandSender);
        }

        if (!commandSender.hasPermission("dfyitems.manage")) {
            commandSender.sendMessage(Component.text(PLUGIN.getPermissionMessage()));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            return sendHelp(commandSender);
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (args.length == 1) {
                return false;
            }

            String path = args[1];
            if (path.matches("(epochs)|(all)")) {
                if (path.matches("epochs")) {
                    FileManager.loadEpochs();
                } else {
                    FileManager.loadEpochs();
                    reloadScript("/scripts", commandSender);
                    reloadFile("/abilities", commandSender, DfyAbility.class);
                    reloadFile("/items", commandSender, DfyItem.class);

                    reloadFile("/recipes", commandSender, DfyRecipe.class);
                }
                return true;
            }

            path = TextUtilities.correctPath(path);
            if (!path.startsWith(File.separator)) path = File.separator + path;

            String separator = TextUtilities.makeRegexSafe(File.separator);

            return switch (path.split(separator)[1]) {
                case "items" -> reloadFile(path, commandSender, DfyItem.class);
                case "abilities" -> reloadFile(path, commandSender, DfyAbility.class);
                case "scripts" -> reloadScript(path, commandSender);
                case "recipes" -> reloadFile(path, commandSender, DfyRecipe.class);
                default -> false;
            };

        }

        return true;
    }

    private boolean reloadScript(String path, CommandSender sender) {
        if (sender instanceof Player) sender.sendMessage(Component.text(PLUGIN.getPrefix() + "Reloading §6'" + path + "'§e!" ));
        PLUGIN.log("Reloading '" + path + "'!");
        long time = System.nanoTime();

        try {
            File reloadFile = new File(PLUGIN.getDataFolder().getCanonicalPath() + path);
            if (reloadFile.isDirectory()) {
                reloadScriptsIn(reloadFile);
            }
            else DfyAbility.reloadAbilitiesWithPath(PLUGIN.getDataFolder().getCanonicalPath() + path);
        } catch (IOException e) {
            if (sender instanceof Player) sender.sendMessage(Component.text(PLUGIN.getPrefix() + "An error occurred while reloading §6'" + path + "'§e!"));
            PLUGIN.log("An error occurred while reloading '" + path + "'!");
            return false;
        }

        double timeToReload = (System.nanoTime() - time) / 1000000d;
        if (sender instanceof Player) sender.sendMessage(Component.text(PLUGIN.getPrefix() + "Reloading §6'" + path + "'§e in " + timeToReload + "ms!" ));
        PLUGIN.log("Reloaded '" + path + "' in " + timeToReload + "ms!");
        return true;
    }

    private void reloadScriptsIn(File reloadFile) {
        File[] files = reloadFile.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) reloadScriptsIn(f);
            else {
                try {
                    DfyAbility.reloadAbilitiesWithPath(f.getCanonicalPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private <T extends DfyStructure> boolean reloadFile(String path, CommandSender sender, Class<T> clazz) {
        if (sender instanceof Player) sender.sendMessage(Component.text(PLUGIN.getPrefix() + "Reloading §6'" + path + "'§e!" ));
        PLUGIN.log("Reloading '" + path + "'!");
        long time = System.nanoTime();

        Map<String, T> storage = new HashMap<>();
        File sourceFile = new File(PLUGIN.getDataFolder(), path);
        if (!sourceFile.exists()) {
            sender.sendMessage(PLUGIN.getErrorPrefix() + "§cCould not find file '" + path + "'!");
            return true;
        }
        if (!loadFromDirectory(sourceFile, storage, clazz)) {
            sender.sendMessage(PLUGIN.getErrorPrefix() + "§cAt least one error occurred while reloading files in '" + path + "'!");
        } else if (!new StructureLoader<>(clazz).loadAllStructuresFrom(sourceFile, storage)) {
            sender.sendMessage(PLUGIN.getErrorPrefix() + "§cAn error occurred while reloading '" + path + "'!");
            return true;
        }

        String[] keys = storage.keySet().toArray(new String[0]);
        FileManager.reloadStorage(keys, storage, clazz);

        double timeToReload = (System.nanoTime() - time) / 1000000d;
        if (sender instanceof Player) sender.sendMessage(Component.text(PLUGIN.getPrefix() + "Reloading §6'" + path + "'§e in " + timeToReload + "ms!" ));
        PLUGIN.log("Reloaded '" + path + "' in " + timeToReload + "ms!");
        return true;
    }

    private <T extends DfyStructure> boolean loadFromDirectory(File sourceFile, Map<String, T> storage, Class<T> clazz) {
        if (!sourceFile.isDirectory()) return true;
        File[] files = sourceFile.listFiles();
        if (files == null) return true;

        boolean anyFailed = false;
        for (File file : files) {
            if (file.isDirectory()) {
                if (anyFailed) loadFromDirectory(file, storage, clazz);
                else anyFailed = !loadFromDirectory(file, storage, clazz);
                continue;
            }
            if (anyFailed) new StructureLoader<>(clazz).loadAllStructuresFrom(file, storage);
            else anyFailed = !new StructureLoader<>(clazz).loadAllStructuresFrom(file, storage);
        }
        return !anyFailed;
    }

    private boolean sendHelp(CommandSender commandSender) {
        //TODO: IMPLEMENT
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!commandSender.hasPermission("dfyitems.manage")) return List.of();
        if (args.length == 1) return List.of("help", "reload");
        if (args.length == 2) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add("all");
            strings.add("epochs");
            strings.addAll(FileManager.getAllFilePaths());
            return strings;
        }
        return List.of();
    }
}
