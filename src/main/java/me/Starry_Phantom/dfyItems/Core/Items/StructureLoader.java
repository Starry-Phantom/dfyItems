package me.Starry_Phantom.dfyItems.Core.Items;

import me.Starry_Phantom.dfyItems.DfyItems;
import me.Starry_Phantom.dfyItems.itemStructure.DfyStructure;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

public class StructureLoader<T> {
    private static DfyItems PLUGIN;
    private final Class<T> clazz;
    private final String name;

    public static void setPlugin(DfyItems plugin) {
        PLUGIN = plugin;
    }

    public StructureLoader(Class<T> clazz) {
        this.clazz = clazz;
        this.name = clazz.getName().split("\\.")[clazz.getName().split("\\.").length-1];
        if (!DfyStructure.class.isAssignableFrom(clazz)) {
            PLUGIN.severe("Invalid loading class " + name);
            throw new IllegalArgumentException("Invalid loading class " + clazz.getName());
        }
    }

    public boolean load(File sourceFolder, Map<String, T> storage) {
        if (!sourceFolder.isDirectory()) {
            PLUGIN.warn("Cannot load " + name + " from " + sourceFolder.getName() + "! It is not a directory!");
            return false;
        }

        File[] items = sourceFolder.listFiles();
        if (items == null) {
            PLUGIN.warn("Cannot load " + name + " from " + sourceFolder.getName() + "! File.listFiles() returned null!");
            return false;
        }

        if (items.length == 0) {
            PLUGIN.log("No " + name + " found in directory " + sourceFolder.getName());
            return true;
        }

        int counter = 0;
        boolean error = false;
        for (File f : items) {
            if (f.isDirectory()) {
                boolean loadResult = load(f, storage);
                if (!loadResult) return false;
            }
            if (!f.getName().contains(".yml")) continue;
            counter++;

            if (!loadAllStructuresFrom(f, storage)) error = true;

        }

        PLUGIN.log("Loaded " + counter + " " + name + " from directory " + sourceFolder.getName());

        return !error;
    }

    public boolean loadAllStructuresFrom(File f, Map<String, T> storage) {
        Yaml yaml = new Yaml();
        try (InputStream input = new FileInputStream(f)) {
            Map<String, Object> data = yaml.load(input);
            for (int i = 0; i < data.size(); i++) {
                T thing = clazz.getDeclaredConstructor(File.class, int.class).newInstance(f, i);
                String ID;
                try {
                    Method m = thing.getClass().getMethod("getID");
                    ID = (String) m.invoke(thing);
                } catch (NoSuchMethodException e) {
                    PLUGIN.severe("Could not retrieve ID reflectively from object of class " + clazz.getName());
                    continue;
                }
                storage.put(ID, thing);
            }
        } catch (Exception e) {
            PLUGIN.severe("Failed to load " + name + " from file " + f.getName());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
