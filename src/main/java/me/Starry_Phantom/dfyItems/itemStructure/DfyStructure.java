package me.Starry_Phantom.dfyItems.itemStructure;

import me.Starry_Phantom.dfyItems.DfyItems;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

public abstract class DfyStructure {
    protected static DfyItems PLUGIN;

    protected final File SOURCE_FILE;
    protected final int INDEX;
    protected String STRUCTURE_ID;
    protected Map<String, Object> data;
    public final static String DELIMITER = "|";
    public final static String SUB_DELIMITER = ":";

    protected boolean THROWN_LOAD_ERROR;

    public static void setPlugin(DfyItems plugin) {
        PLUGIN = plugin;
    }

    public DfyStructure(File loadFile, int index) {
        this.SOURCE_FILE = loadFile;
        this.INDEX = index;
        if (loadFile == null) return;
        if (!importData()) {
            PLUGIN.severe("Could not import data successfully for " + SOURCE_FILE.getName());
            THROWN_LOAD_ERROR = true;
            return;
        }
        if (!loadData()) {
            PLUGIN.severe("Could not load data successfully for " + SOURCE_FILE.getName());
            THROWN_LOAD_ERROR = true;
            return;
        }
        THROWN_LOAD_ERROR = false;
    }

    public static <T extends DfyStructure> void sortAlphabetical(ArrayList<T> structures) {
        DfyStructure[] structArray = structures.toArray(new DfyStructure[0]);
        mergerHelper(structArray, 0, structArray.length / 2 - 1, structArray.length / 2, structArray.length - 1);
        structures.clear();
        for (DfyStructure structure : structArray) structures.add((T) structure);
    }

    public static <T extends DfyStructure> void sortAlphabetical(T[] structArray) {
        mergerHelper(structArray, 0, structArray.length / 2 - 1, structArray.length / 2, structArray.length - 1);
    }

    private static void swap(DfyStructure[] arr, int index1, int index2) {
        DfyStructure swap = arr[index1];
        arr[index1] = arr[index2];
        arr[index2] = swap;
    }

    private static void mergerHelper(DfyStructure[] arr, int s1, int e1, int s2, int e2) {
        if (e2 - s1 == 1) {
            int compareResult = arr[s1].getID().compareTo(arr[s2].getID());
            if (compareResult > 0) swap(arr, s1, s2);
            return;
        }

        if (e2 - s1 <= 0) return;

        mergerHelper(arr, s1, (s1 + e1) / 2, (s1 + e1) / 2 + 1, e1);
        mergerHelper(arr, s2, (s2 + e2) / 2, (s2 + e2) / 2 + 1, e2);

        int l1p = s1;
        int l2p = s2;
        int tempIndex = 0;
        DfyStructure[] temp = new DfyStructure[e2-s1+1];
        while (l1p <= e1 && l2p <= e2) {
            int compareResult = arr[l1p].getID().compareTo(arr[l2p].getID());
            if (compareResult >= 0) {
                temp[tempIndex] = arr[l2p];
                l2p++;
            } else {
                temp[tempIndex] = arr[l1p];
                l1p++;
            }
            tempIndex++;
        }

        while (l1p <= e1) {
            temp[tempIndex] = arr[l1p];
            l1p++;
            tempIndex++;
        }

        while (l2p <= e2) {
            temp[tempIndex] = arr[l2p];
            l2p++;
            tempIndex++;
        }

        for (int i = s1; i <= e2; i++) {
            arr[i] = temp[i - s1];
        }
    }

    public static <T extends DfyStructure> boolean contains(T structure, ArrayList<T> structures) {
        for (T struct : structures) {
            if (structure.getID().equalsIgnoreCase(struct.getID())) return true;
        }
        return false;
    }

    public static <T extends DfyStructure> T removeFromList(T structure, ArrayList<T> structures) {
        for (int i = 0; i < structures.size(); i++) {
            if (structures.get(i).getID().equalsIgnoreCase(structure.getID())) return structures.remove(i);
        }
        return null;
    }

    public static <T extends DfyStructure> T findFromList(T target, ArrayList<T> query) {
        for (T struct : query) {
            if (target.getID().equalsIgnoreCase(struct.getID())) return struct;
        }
        return null;
    }

    protected abstract boolean loadData();

    protected boolean importData() {
        Yaml yaml = new Yaml();
        try (InputStream input = new FileInputStream(SOURCE_FILE)) {
            data = yaml.load(input);
            if (INDEX > data.size()) {
                PLUGIN.severe("SIZE: " + data.size() + " | INDEX: " + INDEX + " | BOOL: " + (INDEX + 1 < data.size()) );
                PLUGIN.severe("Malformed file (" + SOURCE_FILE.getName() + ")");
                return false;
            }
            STRUCTURE_ID = (String) data.keySet().toArray()[INDEX];

            data = (Map<String, Object>) data.get(STRUCTURE_ID);
        } catch (Exception e) {
            PLUGIN.severe("Failed to load item ??? from file " + SOURCE_FILE.getName());
            return false;
        }
        return true;
    }

    protected boolean initField(String field, String value) {
        try {
            Field f = this.getClass().getDeclaredField(field);
            f.setAccessible(true);
            if (!data.containsKey(field)) return false;
            if (data.get(field) instanceof String s) {
                f.set(this, s);
            } else if (data.get(field) == null) {
                f.set(this, null);
            } else {
                f.set(this, value);
                if (value.isEmpty()) f.set(this, null);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            if (e instanceof NoSuchFieldException) PLUGIN.warn("Tried to initialize invalid field (" + field + ")");
            if (e instanceof IllegalAccessException) PLUGIN.warn("Failed to access field while initializing (" + field + ")");
            return false;
        }

        return true;
    }

    protected <T extends Enum<T>> boolean initField(String field, T value, Class<T> clazz) {
        try {
            Field f = this.getClass().getDeclaredField(field);
            f.setAccessible(true);
            if (!data.containsKey(field)) return false;
            T thing;
            if (data.get(field) instanceof String s) {
                thing = Enum.valueOf(clazz, s);
                if (thing == null) {
                    f.set(this, value);
                    return false;
                }
                f.set(this, thing);
            } else if (data.get(field) == null) {
                f.set(this, value);
            } else {
                f.set(this, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            if (e instanceof NoSuchFieldException) PLUGIN.warn("Tried to initialize invalid field (" + field + ")");
            if (e instanceof IllegalAccessException) PLUGIN.warn("Failed to access field while initializing (" + field + ")");
            return false;
        }

        return true;
    }

    protected <T> boolean loadArrayList(String field, ArrayList<T> storage, Class<T> clazz) {
        if (!data.containsKey(field)) return false;
        if (data.get(field).getClass().isPrimitive()) {
            if (clazz.getSimpleName().toLowerCase().contains(data.get(field).getClass().getSimpleName())) storage.add((T) data.get(field));
        } else if (clazz.isInstance(data.get(field))) {
            storage.add((T) data.get(field));
        } else if (data.get(field) instanceof ArrayList<?> s) {
            int skipped = 0;
            for (Object thing : s) {
                if (!(clazz.isInstance(thing))) {
                    skipped++;
                    continue;
                }
                storage.add((T) thing);
            }
            if (skipped > 0) PLUGIN.warn("Skipped " + skipped + " of " + clazz.getSimpleName() + " when loading item " + STRUCTURE_ID);
        } else {
            return false;
        }
        return true;
    }

    protected <T extends Enum<T>> boolean loadEnumArrayList(String field, ArrayList<T> storage, Class<T> clazz) {
        if (!data.containsKey(field)) return false;
        if (data.get(field) instanceof String s) {
            storage.add(Enum.valueOf(clazz, s));
        } else if (data.get(field) instanceof ArrayList<?> s) {
            int skipped = 0;
            for (Object str : s) {
                if (!(str instanceof String)) {
                    skipped++;
                    continue;
                }
                storage.add(Enum.valueOf(clazz, (String) str));
            }
            if (skipped > 0) PLUGIN.warn("Skipped " + skipped + " values when loading " + STRUCTURE_ID + " in enum " + clazz.getName());
        } else {
            return false;
        }
        return true;
    }


    protected boolean initField(String field, boolean value) {
        try {
            Field f = this.getClass().getDeclaredField(field);
            f.setAccessible(true);
            if (!data.containsKey(field)) {
                PLUGIN.warn("Tried to load a field that doesn't exist! (This may be intentional)");
                return false;
            }
            if (data.get(field) == null) {
                f.set(this, value);
                return false;
            } else if (data.get(field) instanceof Boolean b) {
                f.set(this, b);
            } else {
                f.set(this, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            if (e instanceof NoSuchFieldException) PLUGIN.warn("Tried to initialize invalid field (" + field + ")");
            if (e instanceof IllegalAccessException) PLUGIN.warn("Failed to access field while initializing (" + field + ")");
            return false;
        }

        return true;
    }

    public String getID() {
        return this.STRUCTURE_ID;
    }

    public File getSourceFile() {
        return SOURCE_FILE;
    }

    public int getIndex() {
        return INDEX;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + STRUCTURE_ID;
    }

    protected boolean equalsID(DfyStructure dfyStructure) {
        return STRUCTURE_ID.equals(dfyStructure.getID());
    }
}
