package me.Starry_Phantom.dfyItems;

import me.Starry_Phantom.dfyItems.Commands.CoreCommands;
import me.Starry_Phantom.dfyItems.Commands.GetCommand;
import me.Starry_Phantom.dfyItems.Core.AbilityHandler;
import me.Starry_Phantom.dfyItems.Core.StructureLoader;
import me.Starry_Phantom.dfyItems.Core.TextUtilities;
import me.Starry_Phantom.dfyItems.itemStructure.DfyAbility;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import me.Starry_Phantom.dfyItems.itemStructure.DfyStructure;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class DfyItems extends JavaPlugin {

    private static final String SYSTEM_PREFIX = "[dfyItems]";
    private static final String PREFIX = "§8[§6dfy§bItems§8]§e ";
    private static final String PERMISSION_PREFIX = "§c[dfy] ";
    private static final String ERROR_PREFIX = "§c[dfyItems] ";
    private static final String COMPILED_FOLDER_NAMESPACE = "compiled (DO NOT EDIT) (FOR PLUGIN USE ONLY)";

    private File itemsFolder;
    private File scriptFolder;
    private File abilityFolder;
    private File compiledFoler;

    private Map<String, DfyItem> ITEMS;
    private Map<String, DfyAbility> ABILITIES;
    private AbilityHandler ABILITY_HANDLER;

    @Override
    public void onEnable() {
        // Plugin startup logic
        ITEMS = new HashMap<>();
        ABILITIES = new HashMap<>();
        AbilityHandler.setPlugin(this);
        TextUtilities.setPlugin(this);
        StructureLoader.setPlugin(this);
        DfyStructure.setPlugin(this);

        boolean initResult = checkFileStructure();
        if (!initResult) this.severe("Failed to initialize file structure!");

        initResult = loadConfig();
        if (initResult) this.log("Loaded settings from config!");

        initResult = new StructureLoader<>(DfyAbility.class).load(abilityFolder, ABILITIES);
        if (!initResult) this.severe("Could not load abilities for some reason!!");

        initResult = new StructureLoader<>(DfyItem.class).load(itemsFolder, ITEMS);
        if (!initResult) this.severe("Could not load items for some reason!!");


        commandRegistration();
        ABILITY_HANDLER = new AbilityHandler(ABILITIES);
        Bukkit.getPluginManager().registerEvents(ABILITY_HANDLER, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try (Stream<Path> stream = Files.walk(compiledFoler.getCanonicalFile().toPath())) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException ignored) {}

        compiledFoler.delete();
    }

    private void commandRegistration() {
        this.getCommand("get").setExecutor(new GetCommand(this));
        this.getCommand("dfyitems").setExecutor(new CoreCommands(this));

    }


    private boolean loadConfig() {
        itemsFolder = new File(getDataFolder(), "items");
        scriptFolder = new File(getDataFolder(), "scripts");
        abilityFolder = new File(getDataFolder(), "abilities");
        compiledFoler = new File(getDataFolder(), COMPILED_FOLDER_NAMESPACE);
        return true;
    }


    private boolean checkFileStructure() {

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File itemFolder = checkForFolder("items");
        File scriptFolder = checkForFolder("scripts");
        File abilityFolder = checkForFolder("abilities");
        File compileFolder = checkForFolder(COMPILED_FOLDER_NAMESPACE);

        File config = new File(dataFolder, "config.yml");
        if (!config.exists()) {
            try {
                config.createNewFile();
                FileConfiguration emptyConfig = getConfig();

                emptyConfig.set("wrap-length", 32);
                emptyConfig.save(config);
            } catch (IOException e) {
                return false;
            }
        }
        return true;

    }

    private File checkForFolder(String query) {
        File ret = new File(getDataFolder(), query);
        if (!ret.exists()) {
            ret.mkdir();
        }

        return null;
    }

    public File getItemsFolder() {
        return itemsFolder;
    }

    public File getScriptFolder() {
        return scriptFolder;
    }

    public File getAbilityFolder() {
        return abilityFolder;
    }

    public File getCompiledFolder() {
        return compiledFoler;
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

    public DfyItem getItem(String id) {
        return ITEMS.get(id);
    }

    public DfyAbility getAbility(String id) {
        return ABILITIES.get(id);
    }

    public Map<String, DfyAbility> getAbilities() {
        return ABILITIES;
    }

    public <T extends DfyStructure> void reloadStorage(String[] keys, Map<String, T> storage, Class<T> clazz) {
        if (storage.isEmpty()) return;

        if (clazz == DfyItem.class) for (String key : keys) {
            if (ITEMS.containsKey(key)) ITEMS.replace(key, (DfyItem) storage.get(key));
            else ITEMS.put(key, (DfyItem) storage.get(key));
        }
        if (clazz == DfyAbility.class) { for (String key : keys) {
            DfyAbility ability = (DfyAbility) storage.get(key);
            if (ABILITIES.containsKey(key)) {
                DfyAbility oldAbility = ABILITIES.get(key);
                ABILITY_HANDLER.replace(oldAbility, ability);
                ABILITIES.replace(key, ability);
                if (!oldAbility.getPath().equals(ability.getPath())) {
                    ABILITY_HANDLER.recompile(ability);
                }
            } else {
                ABILITIES.put(key, ability);
                ABILITY_HANDLER.compile(ability);
            }
        }
            rebuildItems("ability", keys);
        }
    }

    private void rebuildItems(String target, String[] keys) {
        for (Object o : ITEMS.values().toArray()) {
            DfyItem i = (DfyItem) o;
            boolean found = false;
            for (String s : keys) {
                if (i.getAbilities().contains(s)) {
                    found = true;
                    break;
                }
            }
            if (found) ITEMS.replace(i.getID(), new DfyItem(i.getSourceFile(), i.getIndex()));

        }

    }

    public AbilityHandler getAbilityHandler() {
        return ABILITY_HANDLER;
    }

    public String getPermissionPrefix() {
        return PERMISSION_PREFIX;
    }

    public String getErrorPrefix() {
        return ERROR_PREFIX;
    }
}
