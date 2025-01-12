package biraw.online.b_s_BeePost;

import biraw.online.b_s_BeePost.Bee.BeeHolder;
import biraw.online.b_s_BeePost.Bee.BeeState;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Bee;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;

public final class B_s_BeePost extends JavaPlugin {

    private static B_s_BeePost instance;

    public static B_s_BeePost getInstance(){
        return instance;
    }
    public static NamespacedKey namespacedKey(String s){
        return new NamespacedKey(instance,s);
    }

    private final static Boolean DEBUG = false;

    public static void Debug(String message){
        if (DEBUG) instance.getLogger().warning("[DEBUG] "+message);
    }

    public static ArrayList<BeeHolder> ActiveBees = new ArrayList<>();

    public static BeeHolder getBeeHolderForEntity(Bee bee) {
        for (BeeHolder beeHolder : ActiveBees) {
            if (beeHolder.getEntity() == null) continue;
            if (beeHolder.getEntity().getUniqueId().equals(bee.getUniqueId())) {
                return beeHolder;
            }
        }
        Debug("Bee doesn't have any holder associated with it.");
        return null;
    }

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new BeeInteractListener(),this);
        new LanguageManager();

        LoadManager.StartLoader();
        new BukkitRunnable(){
            @Override
            public void run() {
                LoadManager.spawnBees();
            }
        }.runTaskTimer(instance, 400,100);

        // Print the motd
        this.getLogger().info(" ");
        this.getLogger().info("O=========================================================O");
        this.getLogger().info("      The B's BeePost plugin has loaded successfully!");
        this.getLogger().info("         This is B's BeePost for Minecraft 1.20.5+");
        this.getLogger().info("                       Author: BiRaw");
        this.getLogger().info("         Discord: https://discord.gg/XwFqu7uahX :>");
        this.getLogger().info("O=========================================================O");
        this.getLogger().info(" ");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("The B's BeePost plugin has been disabled!");
        for (BeeHolder beeHolder : new ArrayList<>(B_s_BeePost.ActiveBees))
        {
            if (    beeHolder.state != BeeState.ASCENDED &&
                    beeHolder.state != BeeState.ASCENDING &&
                    beeHolder.state != BeeState.DELIVERY ) continue;
            try {
                beeHolder.Save();
            } catch (IOException ignored){}
        }
    }
}
