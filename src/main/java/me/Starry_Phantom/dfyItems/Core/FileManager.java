package me.Starry_Phantom.dfyItems.Core;

import me.Starry_Phantom.dfyItems.DfyItems;
import me.Starry_Phantom.dfyItems.InternalAbilities.EffectApplicator;
import me.Starry_Phantom.dfyItems.itemStructure.DfyAbility;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import me.Starry_Phantom.dfyItems.itemStructure.DfyStructure;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class FileManager {
    private static DfyItems PLUGIN;
    private static Map<String, DfyItem> ITEMS;
    private static Map<String, DfyAbility> ABILITIES;
    private static AbilityHandler ABILITY_HANDLER;
    private static final String COMPILED_FOLDER_NAMESPACE = ".compiled";

    private static File itemsFolder;
    private static File scriptFolder;
    private static File abilityFolder;
    private static File compiledFoler;

    private static File epochFile, backupEpochFile;
    private static Map<String, Integer> EPOCHS;

    public static void setPlugin(DfyItems plugin) {PLUGIN = plugin;}

    public static boolean initialize() {
        ITEMS = new HashMap<>();
        ABILITIES = new HashMap<>();

        boolean initResult = loadFiles();
        if (!initResult) PLUGIN.severe("Failed to initialize file structure!");

        initResult = FileManager.loadConfig();
        if (initResult) PLUGIN.log("Loaded settings from config!");

        loadDfyStructures();
        loadEpochs();

        establishAutoSave();

        return true;
    }

    private static void loadEpochs() {
        Yaml yaml = new Yaml();
        try (InputStream input = new FileInputStream(epochFile)) {
            Map<String, Integer> temp = yaml.load(input);
            if (temp == null) EPOCHS = new ConcurrentHashMap<>();
            else {
                EPOCHS = new ConcurrentHashMap<>(temp);
            }
            if (!EPOCHS.containsKey("GLOBAL")) EPOCHS.put("GLOBAL", 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getGlobalEpoch() {
        return EPOCHS.get("GLOBAL");
    }

    public static int getItemEpoch(String id) {
        if (EPOCHS.containsKey(id)) return EPOCHS.get(id);
        EPOCHS.put(id, 0);
        return 0;
    }

    private static void establishAutoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileManager.saveEpochs(epochFile);
            }
        }.runTaskTimerAsynchronously(PLUGIN, 20 * 60 * 5, 20 * 60 * 10);

        new BukkitRunnable() {
            @Override
            public void run() {
                FileManager.saveEpochs(backupEpochFile);
            }
        }.runTaskTimerAsynchronously(PLUGIN, 20 * 60 * 10, 20 * 60 * 10);
    }

    public static void finalSaveEpochs(File file) {
        boolean initResult;
        Map<String, DfyAbility> abilities = new HashMap<>();
        initResult = new StructureLoader<>(DfyAbility.class).load(abilityFolder, abilities);
        if (!initResult) PLUGIN.severe("There may have been an error while saving epochs! This could cause desync issues!");
        ArrayList<String> abilReloads = new ArrayList<>();
        for (String key : ABILITIES.keySet()) {
            if (!abilities.containsKey(key)) continue;
            if (!ABILITIES.get(key).equals(abilities.get(key))) {
                abilReloads.add(key);
            }
        }
        rebuildItems("ability", abilReloads.toArray(new String[0]), false);

        Map<String, DfyItem> items = new HashMap<>();
        initResult = new StructureLoader<>(DfyItem.class).load(itemsFolder, items);
        if (!initResult) PLUGIN.severe("There may have been an error while saving epochs! This could cause desync issues!");
        for (String key : ITEMS.keySet()) {
            if (!items.containsKey(key)) continue;
            if (!ITEMS.get(key).deepEquals(items.get(key))) {
                increaseEpoch(key);
            }
        }

        saveEpochs(file);

    }

    public static void saveEpochs(File file) {
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            FileWriter writer = new FileWriter(file);
            yaml.dump(EPOCHS, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void increaseEpoch(String id) {
        if (EPOCHS.containsKey(id)) EPOCHS.replace(id, EPOCHS.get(id) + 1);
        else EPOCHS.put(id, 0);
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

        epochFile = makeFileIfMissing("item_epochs.yml");
        backupEpochFile = makeFileIfMissing("item_epochs_backup.yml");

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

    private static File makeFileIfMissing(String path) {
        File file = new File(PLUGIN.getDataFolder(), path);
        if (!file.exists()) {
            try {
                file.createNewFile();
                return file;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
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
        ABILITIES.put("applicator", EffectApplicator.getAbilityObject());

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
            increaseEpoch(key);
            if (ITEMS.containsKey(key)) ITEMS.replace(key, (DfyItem) storage.get(key));
            else ITEMS.put(key, (DfyItem) storage.get(key));
        }
        if (clazz == DfyAbility.class) { for (String key : keys) {
            ArrayList<String> replaceKeys = new ArrayList<>();
            DfyAbility ability = (DfyAbility) storage.get(key);

            if (ABILITIES.containsKey(key)) {
                DfyAbility oldAbility = ABILITIES.get(key);

                if (!oldAbility.equals(ability)) replaceKeys.add(key);

                ABILITY_HANDLER.replace(oldAbility, ability);
                ABILITIES.replace(key, ability);
                if (!oldAbility.getPath().equals(ability.getPath())) {
                    ABILITY_HANDLER.recompile(ability);
                }
            } else {
                ABILITIES.put(key, ability);
                replaceKeys.add(key);
                ABILITY_HANDLER.compile(ability);
            }

            rebuildItems("ability", replaceKeys.toArray(new String[0]), true);
        }}
    }

    private static void rebuildItems(String target, String[] keys, boolean loadData) {
        for (Object o : ITEMS.values().toArray()) {
            DfyItem i = (DfyItem) o;
            boolean found = false;
            for (String s : keys) {
                if (i.getAbilities().contains(s)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                increaseEpoch(i.getID());
                if (loadData) ITEMS.replace(i.getID(), new DfyItem(i.getSourceFile(), i.getIndex()));
            }

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

    public static ArrayList<DfyAbility> getAbilities(ArrayList<String> ids) {
        ArrayList<DfyAbility> retVal = new ArrayList<>();
        for (String id : ids) {
            retVal.add(getAbility(id));
        }
        return retVal;
    }

    public static ArrayList<DfyAbility> getAbilities(String[] ids) {
        ArrayList<DfyAbility> retVal = new ArrayList<>();
        for (String id : ids) {
            retVal.add(getAbility(id));
        }
        return retVal;
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

    public static File getEpochFile() {
        return epochFile;
    }
}
