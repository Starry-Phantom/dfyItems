package me.Starry_Phantom.dfyItems.Core;

import me.Starry_Phantom.dfyItems.DfyItems;
import me.Starry_Phantom.dfyItems.itemStructure.DfyAbility;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import me.Starry_Phantom.dfyItems.itemStructure.DfyStructure;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FileManager {
    private static DfyItems PLUGIN;
    private static Map<String, DfyItem> ITEMS;
    private static Map<String, DfyAbility> ABILITIES;
    private static AbilityHandler ABILITY_HANDLER;
    private static final String COMPILED_FOLDER_NAMESPACE = "compiled (DO NOT EDIT) (FOR PLUGIN USE ONLY)";

    private static File itemsFolder;
    private static File scriptFolder;
    private static File abilityFolder;
    private static File compiledFoler;

    public static void setPlugin(DfyItems plugin) {PLUGIN = plugin;}

    public static boolean initialize() {
        ITEMS = new HashMap<>();
        ABILITIES = new HashMap<>();

        boolean initResult = loadFiles();
        if (!initResult) PLUGIN.severe("Failed to initialize file structure!");

        initResult = FileManager.loadConfig();
        if (initResult) PLUGIN.log("Loaded settings from config!");

        loadDfyStructures();

        return true;
    }

    private static boolean loadFiles() {
        File dataFolder = PLUGIN.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File itemFolder = makeFolderIfMissing("items");
        File scriptFolder = makeFolderIfMissing("scripts");
        File abilityFolder = makeFolderIfMissing("abilities");
        File compileFolder = makeFolderIfMissing(COMPILED_FOLDER_NAMESPACE);

        File config = new File(dataFolder, "config.yml");
        if (!config.exists()) {
            try {
                config.createNewFile();
                FileConfiguration emptyConfig = PLUGIN.getConfig();

                emptyConfig.set("wrap-length", 32);
                emptyConfig.save(config);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    private static File makeFolderIfMissing(String query) {
        File ret = new File(PLUGIN.getDataFolder(), query);
        if (!ret.exists()) {
            ret.mkdir();
        }

        return null;
    }

    private static boolean loadConfig() {
        File dataFolder = PLUGIN.getDataFolder();
        itemsFolder = new File(dataFolder, "items");
        scriptFolder = new File(dataFolder, "scripts");
        abilityFolder = new File(dataFolder, "abilities");
        compiledFoler = new File(dataFolder, COMPILED_FOLDER_NAMESPACE);
        return true;
    }

    private static void loadDfyStructures() {
        boolean initResult;
        initResult = new StructureLoader<>(DfyAbility.class).load(abilityFolder, ABILITIES);
        if (!initResult) PLUGIN.severe("Could not load abilities for some reason!!");

        initResult = new StructureLoader<>(DfyItem.class).load(itemsFolder, ITEMS);
        if (!initResult) PLUGIN.severe("Could not load items for some reason!!");
    }

    public static AbilityHandler createAbilityHandler() {
        ABILITY_HANDLER = new AbilityHandler(ABILITIES);
        return ABILITY_HANDLER;
    }

    public static <T extends DfyStructure> void reloadStorage(String[] keys, Map<String, T> storage, Class<T> clazz) {
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

    private static void rebuildItems(String target, String[] keys) {
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

    public static DfyItem getItem(String id) {
        return ITEMS.get(id);
    }

    public static Map<String, DfyItem> getItems() {
        return ITEMS;
    }

    public static DfyAbility getAbility(String id) {
        return ABILITIES.get(id);
    }

    public static Map<String, DfyAbility> getAbilities() {
        return ABILITIES;
    }

    public static File getItemsFolder() {
        return itemsFolder;
    }

    public static File getScriptFolder() {
        return scriptFolder;
    }

    public static File getAbilityFolder() {
        return abilityFolder;
    }

    public static File getCompiledFolder() {
        return compiledFoler;
    }

    public static AbilityHandler getAbilityHandler() {
        return ABILITY_HANDLER;
    }


    public static void deleteCompiledFolder() {
        try (Stream<Path> stream = Files.walk(compiledFoler.getCanonicalFile().toPath())) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException ignored) {}

        compiledFoler.delete();
    }
}
