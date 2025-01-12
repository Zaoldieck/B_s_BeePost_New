package biraw.online.b_s_BeePost;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import java.util.Random;


public class Utilities {

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
}
