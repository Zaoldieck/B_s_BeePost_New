package biraw.online.b_s_BeePost;

import com.google.gson.*;
import org.bukkit.*;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Random;


public class Utilities {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void spawnBeeDisappearParticles(Location center){
        Random random = new Random();

        // Spawn 50 random particles
        for (int i = 0; i < 50; i++) {
            // Generate random spherical coordinates
            double angle = random.nextDouble() * Math.PI * 2;
            double inclination = random.nextDouble() * Math.PI;

            // Calculate x, y, z positions in spherical coordinates
            double x = Math.sin(inclination) * Math.cos(angle);
            double y = Math.cos(inclination);
            double z = Math.sin(inclination) * Math.sin(angle);
            Vector particleOffset = new Vector(x, y, z).multiply(1.0);

            // Get the final position
            Location particleLocation = center.clone().add(particleOffset);

            // Spawn the particle
            center.getWorld().spawnParticle(Particle.WHITE_SMOKE, particleLocation, 1, 0, 0, 0, 0.1);
        }
    }

    public static void spawnParticleForBee(Location location,Color color){
        Particle.DustOptions option = new Particle.DustOptions(color, 2f);
        location.getWorld().spawnParticle(Particle.DUST, location.add(0.0,1.0,0.0), 5, option);
    }

    public static void swapHands(Player player) { // This is to prevent the player from opening the book, if they have multiple in hand
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        // Swap to offhand
        player.getInventory().setItemInMainHand(offHandItem);
        player.getInventory().setItemInOffHand(mainHandItem);

        // Schedule a task to swap back in 1 tick
        Bukkit.getScheduler().runTaskLater(B_s_BeePost.getInstance(), () -> {
            player.getInventory().setItemInMainHand(mainHandItem);
            player.getInventory().setItemInOffHand(offHandItem);
        }, 1L);
    }

    public static Vector getRandomBeeDirection() {
        Random random = new Random();

        // Generate random horizontal direction
        double angle = random.nextDouble() * Math.PI * 2;
        double x = Math.sin(angle);
        double z = Math.cos(angle);

        Vector randomVector = new Vector(x, 1.0, z);

        randomVector.normalize();

        return randomVector;
    }

    public static void dropDelivery(Bee bee) {
        // Get the bee's current location
        Location location = bee.getLocation();

        // Retrieve the items from the bee's persistent data container
        PersistentDataContainer beeData = bee.getPersistentDataContainer();
        String message = beeData.get(B_s_BeePost.namespacedKey("message"), PersistentDataType.STRING);
        String present = beeData.get(B_s_BeePost.namespacedKey("present"), PersistentDataType.STRING);

        // Deserialize the items
        ItemStack messageItem = deserializeItemStack(message);
        ItemStack presentItem = deserializeItemStack(present);

        // Drop the items at the bee's location
        if (messageItem != null) {
            bee.getWorld().dropItem(location, messageItem);
        }
        if (presentItem != null) {
            bee.getWorld().dropItem(location, presentItem);
        }
    }

    // Deserialize the item stack from a JSON string
    private static ItemStack deserializeItemStack(String itemData) {
        if (itemData == null || itemData.isEmpty()) {
            return null; // Return null if no item data exists
        }

        // You can use GSON or your preferred method to convert the string back into ItemStack data
        // Example for GSON deserialization
        try {
            // Deserialize into a map or a specific item structure
            Map<String, Object> data = GSON.fromJson(itemData, Map.class);

            // Extract the necessary fields like type and meta
            String type = (String) data.get("type");
            // Assuming you store ItemStack meta information in a similar format, extract it here
            // Example for ItemStack creation (needs further refinement based on your data format)
            ItemStack itemStack = new ItemStack(Material.valueOf(type)); // You may need to handle item meta too

            // Set meta if available, deserialize accordingly
            // Here you'd handle any additional item meta and apply it to the ItemStack

            return itemStack;
        } catch (Exception e) {
            e.printStackTrace();  // Log the error if deserialization fails
            return null;
        }
    }

}
