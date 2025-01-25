package biraw.online.b_s_BeePost;


import biraw.online.b_s_BeePost.Bee.BeeAI;
import biraw.online.b_s_BeePost.Bee.BeeHolder;
import biraw.online.b_s_BeePost.Bee.BeeState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.iseal.sealLib.Systems.I18N.I18N;
import org.bukkit.*;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LoadManager {


    public static void StartLoader() {
        String directoryPath = "plugins/BeePost messages/";
        File directory = new File(directoryPath);

        // Ensure the directory exists
        if (!directory.exists() || !directory.isDirectory()) {
            directory.mkdir();
            return;
        }

        // Get all files in the directory
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : new ArrayList<>(List.of(files))) {
            BeeHolder newBeeHolder = new BeeHolder(file);
            B_s_BeePost.ActiveBees.add(newBeeHolder);
            file.delete();
        }
    }

    public static void spawnBees() {
        if (B_s_BeePost.ActiveBees == null || B_s_BeePost.ActiveBees.isEmpty()) return;

        for (BeeHolder beeHolder : new ArrayList<>(B_s_BeePost.ActiveBees))
        {
            if (beeHolder.state == BeeState.ASCENDED && beeHolder.getReceiver().isOnline())
            {

                Player receiver = (Player) beeHolder.getReceiver();
                // DON'T LOOK AT THIS
                Location beeSpawnpoint = receiver.getWorld().getHighestBlockAt(receiver.getLocation()
                                .add(   new Random().nextInt(51) - 25,
                                        0,
                                        new Random().nextInt(51) - 25))
                        .getLocation().add(0,20f,0);
                beeHolder.setEntity((Bee) receiver.getWorld().spawnEntity(beeSpawnpoint, EntityType.BEE));
                beeHolder.state = BeeState.DELIVERY;
                BeeAI.BeeDeliver(beeHolder);
                receiver.sendMessage(I18N.translate("POST_BEE_APPROACHING"));
            }
            if (beeHolder.state == BeeState.FINISHED) B_s_BeePost.ActiveBees.remove(beeHolder);
        }
    }
}
