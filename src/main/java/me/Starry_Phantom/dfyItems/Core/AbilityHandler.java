package me.Starry_Phantom.dfyItems.Core;

import me.Starry_Phantom.dfyItems.DfyItems;
import me.Starry_Phantom.dfyItems.InternalAbilities.EffectApplicator;
import me.Starry_Phantom.dfyItems.itemStructure.DfyAbility;
import me.Starry_Phantom.dfyItems.itemStructure.DfyItem;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AbilityHandler implements Listener {

    private final JavaCompiler javaCompiler;
    private String CLASSPATH;

    private Map<DfyAbility, URLClassLoader> abilityFiles;
    private static DfyItems PLUGIN;

    public static void setPlugin(DfyItems plugin) {
        PLUGIN = plugin;
    }

    public AbilityHandler(Map<String, DfyAbility> abilities) {
        this.abilityFiles = new HashMap<>();
        this.javaCompiler = ToolProvider.getSystemJavaCompiler();
        CLASSPATH = getAllJarPaths(System.getProperty("user.dir"))
                + File.pathSeparator + PLUGIN.getDataFolder()
                + File.separator + PLUGIN.getFileName();

        String[] keys = abilities.keySet().toArray(new String[0]);
        for (String s : keys) {
            DfyAbility ability = abilities.get(s);
            if (ability.getPath() == null) continue;
            abilityFiles.put(ability, compileFileFromAbility(ability));
        }
    }

    private URLClassLoader compileFileFromAbility(DfyAbility ability) {
        try {
            File scriptPath = new File(ability.getPath());
            String compilePath = FileManager.getCompiledFolder().getCanonicalPath() + File.separator + scriptPath.getName().replace(".java", "");
            javaCompiler.run(null, null, null,
                    //"-verbose",
                    "-d", compilePath,
                    "-classpath", CLASSPATH,
                    scriptPath.getCanonicalPath());


            File crazyPath = new File(compilePath);
            URL[] urls = {crazyPath.toURI().toURL()};
            return new URLClassLoader(urls, DfyItems.class.getClassLoader());
        } catch (IOException e) {
            return null;
        }
    }

    private String getAllJarPaths(String path) {
        ArrayList<String> jars = new ArrayList<>();
        File[] files = new File(path).listFiles();
        if (files == null) return "";
        try {
            for (File f : files) {
                if (f.isDirectory()) {
                    jars.add(getAllJarPaths(f.getCanonicalPath()));
                }
                if (f.getName().endsWith(".jar")) jars.add(f.getCanonicalPath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < jars.size(); i++) {
            s.append(jars.get(i));
            if (i < jars.size() -1) s.append(File.pathSeparator);
        }
        return s.toString();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = e.getPlayer();
        ArrayList<DfyAbility> abilities = getActiveAbilities(player, TriggerCase.INTERACT);
        runAbilities(abilities, e, PlayerInteractEvent.class);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            ArrayList<DfyAbility> abilities = getActiveAbilities(player, TriggerCase.DAMAGE_DEALT);
            runAbilities(abilities, e, EntityDamageByEntityEvent.class);
        }
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            ArrayList<DfyAbility> abilities = getActiveAbilities(player, TriggerCase.DAMAGE_TAKEN);
            runAbilities(abilities, e, EntityDamageEvent.class);
        }
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent e) {
        if (e.getPlayer() instanceof Player player) {
            ArrayList<DfyAbility> abilities = getActiveAbilities(player, TriggerCase.BREAK_BLOCK);
            runAbilities(abilities, e, BlockBreakEvent.class);
        }
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent e) {
        if (e.getPlayer() instanceof Player player) {
            ArrayList<DfyAbility> abilities = getActiveAbilities(player, TriggerCase.CROUCH);
            runAbilities(abilities, e, PlayerToggleSneakEvent.class);
        }
    }

//    @EventHandler
//    public void onEquip(PlayerEquipEvent e) {
//        if (e.getPlayer() instanceof Player player) {
//            ArrayList<DfyAbility> abilities = getActiveAbilities(player, TriggerCase.BREAK_BLOCK);
//            runAbilities(abilities, e, BlockBreakEvent.class);
//        }
//    }

    private <T extends Event> void runAbilities(ArrayList<DfyAbility> abilities, T e, Class<T> clazz) {
        for (DfyAbility ability : abilities ) {
            try {
                if (!ability.isEnabled()) {
                    Player p = (Player) e.getClass().getMethod("getPlayer").invoke(e);
                    p.sendMessage(Component.text("§c" + ability.getDisplayName() + "§r§c is currently disabled!"));
                    continue;
                }

                if (ability.getID().equals(EffectApplicator.STRUCTURE_ID)) {
                    EffectApplicator.trigger(e);
                    continue;
                }

                Class<?> abilityClass = abilityFiles.get(ability).loadClass(ability.getClassName());
                Method m = abilityClass.getMethod("trigger", clazz);
                m.invoke(null, e);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public boolean leftClickTrigger() {
        return false;
    }


    public static ArrayList<DfyAbility> getActiveAbilities(Player player, TriggerCase trigger) {
        Map<TriggerSlot, ItemStack> items = TriggerSlot.getAllSlotItems(player);
        TriggerSlot[] slots = TriggerSlot.values();
        ArrayList<DfyAbility> activeAbilities = new ArrayList<>();

        for (TriggerSlot slot : slots) {
            ItemStack item = items.get(slot);
            if (!DfyItem.isValidItem(item)) continue;
            if (ItemUpdateHandler.itemNeedsUpdate(item)) {
                ItemStack newItem = DfyItem.updateItem(item);
                TriggerSlot.setSlotItem(player, slot, newItem, item);
                item = newItem;

            }

            String[] abilities = DfyAbility.getItemEffectiveAbilities(item);
            if (abilities == null) continue;

            for (String s : abilities) {
                DfyAbility a = FileManager.getAbility(s);
                if (a == null) continue;
                if (a.getTriggerSlots().contains(slot) &&
                        a.getTriggerCases().contains(trigger)) activeAbilities.add(a);
            }
        }

        return activeAbilities;
    }

    public void replace(DfyAbility query, DfyAbility target) {
        URLClassLoader loader = abilityFiles.remove(query);
        if (loader == null) PLUGIN.severe("Attempted to replace an ability that doesn't exist! (" + query.getID() + ")");
        abilityFiles.put(target, loader);
    }

    public void recompile(DfyAbility target) {
        URLClassLoader loader = abilityFiles.remove(target);
        try {loader.close();
        } catch (IOException ignored) {}
        loader = null;
        System.gc();
        abilityFiles.put(target, compileFileFromAbility(target));

    }

    public void compile(DfyAbility ability) {
        abilityFiles.put(ability, compileFileFromAbility(ability));
    }
}
