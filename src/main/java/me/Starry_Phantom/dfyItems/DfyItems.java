package me.Starry_Phantom.dfyItems;

import me.Starry_Phantom.dfyItems.Commands.*;
import me.Starry_Phantom.dfyItems.Core.*;
import me.Starry_Phantom.dfyItems.Core.Blocks.CraftingRecipeHandler;
import me.Starry_Phantom.dfyItems.itemStructure.DfyAbility;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import me.Starry_Phantom.dfyItems.itemStructure.DfyStructure;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

public final class DfyItems extends JavaPlugin {

    public static DfyItems PLUGIN;

    private static final String SYSTEM_PREFIX = "[dfyItems]";
    private static final String PREFIX = "§8[§6dfy§bItems§8]§e ";
    private static final String PERMISSION_PREFIX = "§c[dfy] ";
    private static final String ERROR_PREFIX = "§c[dfyItems] ";

    @Override
    public void onEnable() {
        // Plugin startup logic
        PLUGIN = this;
        passPluginToClasses();

        FileManager.initialize();

        registerCommands();
        registerEventListeners();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        FileManager.deleteCompiledFolder();
        FileManager.finalSaveEpochs(FileManager.getEpochFile());
    }

    private void passPluginToClasses() {
        AbilityHandler.setPlugin(this);
        TextUtilities.setPlugin(this);
        StructureLoader.setPlugin(this);
        DfyStructure.setPlugin(this);
        FileManager.setPlugin(this);
    }

    private void registerCommands() {
        this.getCommand("get").setExecutor(new GetCommand(this));
        CoreCommands c = new CoreCommands(this);
        PluginCommand dfyitems = this.getCommand("dfyitems");
        dfyitems.setExecutor(c);
        dfyitems.setTabCompleter(c);

        this.getCommand("transmute").setExecutor(new TransmuteCommand(this));
        this.getCommand("enchant").setExecutor(new EnchantCommand(this));
    }

    private void registerEventListeners() {
        AbilityHandler abilityHandler = FileManager.createAbilityHandler();
        Bukkit.getPluginManager().registerEvents(abilityHandler, this);
        Bukkit.getPluginManager().registerEvents(new ItemUpdateHandler(this), this);
        Bukkit.getPluginManager().registerEvents(new CraftingRecipeHandler(), this);
    }


    public String getPrefix() {
        return PREFIX;
    }

    public String getFileName() {
        return super.getFile().getName();
    }

    public void warn(String s) {
        getLogger().warning(SYSTEM_PREFIX + " " + s);
    }

    public void log(String s) {
        getLogger().info(SYSTEM_PREFIX + " " + s);
    }

    public void severe(String s) {
        getLogger().severe(SYSTEM_PREFIX + " " + s);
    }

    public String getPermissionPrefix() {
        return PERMISSION_PREFIX;
    }
    public String getPermissionMessage() {return PERMISSION_PREFIX + " You do not have permission to run this command.";}

    public String getErrorPrefix() {
        return ERROR_PREFIX;
    }
}
