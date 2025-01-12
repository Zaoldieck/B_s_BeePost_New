package biraw.online.b_s_BeePost.Bee;

import biraw.online.b_s_BeePost.B_s_BeePost;
import biraw.online.b_s_BeePost.Utilities;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.Random;

public class BeeAI {

    public void BeeWaitingForPresent(BeeHolder beeHolder)
    {
        Bee bee = beeHolder.getEntity();
        OfflinePlayer offlineSender = beeHolder.getSender();


        new BukkitRunnable() {
            @Override
            public void run() {

                // If the player is not online, it finishes
                if (!offlineSender.isOnline())
                {
                    this.cancel();
                    beeHolder.state = BeeState.FINISHED;
                }

                Player sender = (Player) offlineSender;

                // If something happens to the bee or the owner, it finishes
                if (sender.getWorld() != bee.getWorld() || bee.isDead())
                {
                    this.cancel();
                    beeHolder.state = BeeState.FINISHED;
                }

                // If the bee got the present, go to the next phase
                if (beeHolder.state != BeeState.PRESENTLESS ) this.cancel();


                bee.setAnger(0);
                bee.setHive(null);
                bee.setFlower(null);

                // Make the bee move to the player
                if(bee.getLocation().distance(sender.getLocation()) > 4) bee.getPathfinder().moveTo(sender);

                // Spawn state indicator particles
                if (beeHolder.getReceiver().isOnline())    Utilities.spawnParticleForBee(bee.getLocation(), Color.GREEN);
                else                                       Utilities.spawnParticleForBee(bee.getLocation(), Color.GRAY);
            }
        }.runTaskTimer(B_s_BeePost.getInstance(), 0L, 10L);
    }

    public void BeeAscending(BeeHolder beeHolder){
        Bee bee = beeHolder.getEntity();
        OfflinePlayer offlineSender = beeHolder.getSender();

        new BukkitRunnable() {

            // Get a random direction for the bee
            Random random = new Random();
            final Vector beeDirection = new Vector(random.nextBoolean() ? -5 : 5,5,random.nextBoolean() ? -5 : 5);

            @Override
            public void run() {

                // If the player logs off or state is not ascending, go to next phase
                if (!offlineSender.isOnline() || beeHolder.state != BeeState.ASCENDING)
                {
                    this.cancel();
                    try {
                        beeHolder.Save();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                Player sender = (Player) offlineSender; // Just mold the receiver to a Player for easier usage

                // Save bee if the conditions are right
                if (bee.getLocation().distance(sender.getLocation())>=32)
                {
                    sender.sendMessage("§6Message was sent to: §a"+ beeHolder.getReceiver().getName());
                    this.cancel();
                    try {
                        beeHolder.Save();
                    } catch (IOException e) {throw new RuntimeException(e);}
                }

                bee.setAnger(0);
                bee.setHive(null);
                bee.setFlower(null);
                bee.setInvulnerable(true);

                // Make the bee fly in one direction
                bee.getPathfinder().moveTo(bee.getLocation().add(beeDirection));
                Utilities.spawnParticleForBee(bee.getLocation(), Color.AQUA);
            }
        }.runTaskTimer(B_s_BeePost.getInstance(), 0L, 10L);
    }

    public void BeeDeliver(BeeHolder beeHolder){
        Bee bee = beeHolder.getEntity();
        OfflinePlayer offlineReceiver = beeHolder.getReceiver();

        new BukkitRunnable() {
            @Override
            public void run() {
                // If the bee got the present delivered, it gets back to being a mindless little honey farmer :)
                if (beeHolder.state != BeeState.DELIVERY) this.cancel();
                // If the receiver logs off, the bee gets saved
                if (!offlineReceiver.isOnline())
                {
                    try {
                        beeHolder.Save();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    this.cancel();
                }

                Player receiver = (Player) offlineReceiver; // Just mold the receiver to a Player for easier usage

                // If the bee gets too far from the owner, it disappears
                if (bee.getLocation().distance(receiver.getLocation())>=80)
                {
                    receiver.sendMessage("§4You went too far, and the post be disappeared...");
                    this.cancel();
                    try {
                        beeHolder.Save();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                bee.setAnger(0);
                bee.setHive(null);
                bee.setFlower(null);

                // Get to the receiver constantly
                if(bee.getLocation().distance(receiver.getLocation()) > 4) bee.getPathfinder().moveTo(receiver);

                // Spawn state indicator particles
                Utilities.spawnParticleForBee(bee.getLocation(), Color.ORANGE);
            }
        }.runTaskTimer(B_s_BeePost.getInstance(), 0L, 10L);
    }
}
