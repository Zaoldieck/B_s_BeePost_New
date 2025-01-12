package biraw.online.b_s_BeePost;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void StartLoader() {
        String directoryPath = "plugins/BeePost messages/";
        File directory = new File(directoryPath);

        // Ensure the directory exists
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory does not exist: " + directoryPath);
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                // Get all files in the directory
                File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
                if (files == null || files.length == 0) {
                    return;
                }

                // Select a random file
                int randomIndex = ThreadLocalRandom.current().nextInt(files.length);
                File randomFile = files[randomIndex];

                // Read data from the selected file
                try (Reader reader = new FileReader(randomFile)) {
                    // Deserialize the JSON into a Map<String, String>
                    Map<String, String> data = GSON.fromJson(reader, Map.class);

                    // Extract the relevant fields
                    String message = data.get("message");
                    String present = data.get("present");
                    String address = data.get("address");

                    // Pass the raw strings directly to the spawnBee method
                    spawnBee(message, present, address);

                    // Delete the file after processing
                    reader.close();
                    randomFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();  // Log the exception
                }
            }
        }.runTaskTimer(B_s_BeePost.getInstance(), 0L, 20L);
    }

    private static void spawnBee(String message, String present, String address) {
        OfflinePlayer offlineReceiver = Bukkit.getOfflinePlayer(UUID.fromString(address));
        if (!offlineReceiver.isOnline()) return;
        Player receiver = (Player) offlineReceiver;
        if (receiver.getWorld().getEnvironment() != World.Environment.NORMAL) return;

        // DON'T LOOK AT THIS
        Location beeSpawnpoint = receiver.getWorld().getHighestBlockAt(receiver.getLocation()
                .add(   new Random().nextInt(51) - 25,
                        0,
                        new Random().nextInt(51) - 25))
                .getLocation().add(0,20f,0);

        Bee bee = (Bee) receiver.getWorld().spawnEntity(beeSpawnpoint, EntityType.BEE);

        receiver.sendMessage("§6A post bee is approaching you!");

        bee.getPersistentDataContainer().set(B_s_BeePost.namespacedKey("message"), PersistentDataType.STRING,message);
        bee.getPersistentDataContainer().set(B_s_BeePost.namespacedKey("present"), PersistentDataType.STRING,present);
        bee.getPersistentDataContainer().set(B_s_BeePost.namespacedKey("address"), PersistentDataType.STRING,address);
        bee.getPersistentDataContainer().set(B_s_BeePost.namespacedKey("delivered"), PersistentDataType.BOOLEAN,true);

        new BukkitRunnable() {
            @Override
            public void run() {
                // If the bee got the present delivered, it gets back to being a mindless little honey farmer :)
                if (!bee.getPersistentDataContainer().has(B_s_BeePost.namespacedKey("delivered"))) this.cancel();

                if (bee.getLocation().distance(receiver.getLocation())>=50)
                {
                    receiver.sendMessage("§4You went too far, and the post be disappeared...");
                    this.cancel();
                    SaveManager.SaveDelivery(bee);
                    Utilities.spawnBeeDisappearParticles(bee.getLocation());
                    bee.remove();
                }

                bee.setAnger(0);
                bee.setHive(null);
                bee.setFlower(null);

                if(bee.getLocation().distance(receiver.getLocation()) > 4) bee.getPathfinder().moveTo(receiver);

                Utilities.spawnParticleForBee(bee.getLocation(), Color.ORANGE);

            }
        }.runTaskTimer(B_s_BeePost.getInstance(), 0L, 10L);
    }
}
