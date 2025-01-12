package biraw.online.b_s_BeePost;

import biraw.online.b_s_BeePost.Bee.BeeHolder;
import org.bukkit.*;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class BeeInteractListener implements Listener {



    @EventHandler
    private void BeeInteract(PlayerInteractEntityEvent event){
        // Check if we are working with a bee
        if (event.getRightClicked().getType() != EntityType.BEE) return;
        Bee bee = (Bee) event.getRightClicked();
        // Check if bee is registered in the plugin
        if (B_s_BeePost.getBeeHolderForEntity(bee) != null)
        {
            // If yes, handle it in this function
            HandlePostBee(B_s_BeePost.getBeeHolderForEntity(bee));
        }
        else
        {
            // If not, then register it

            Player sender = event.getPlayer(); // Get sender

            // Check if the player has a written book in hand
            ItemStack book = sender.getInventory().getItemInMainHand().clone(); // Get the book
            book.setAmount(1);
            if (book.getType() != Material.WRITTEN_BOOK) return;

            // Get the books meta
            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            if (bookMeta.getTitle() == null) return;

            // Get the receiver
            OfflinePlayer receiver = Bukkit.getOfflinePlayerIfCached(bookMeta.getTitle());
            if (receiver == null || !receiver.hasPlayedBefore())
            {
                Utilities.swapHands(sender); // Cancel the book opening
                sender.sendMessage("§4This username is not valid!");
                return;
            }

            // At this point we are sure they want to send a bee
            // Take one book away
            sender.getInventory().getItemInMainHand().subtract(1);

            B_s_BeePost.ActiveBees.add(
                    new BeeHolder(bee,event.getPlayer(),)
            )
        }

        event.setCancelled(true);
    }

    private void HandlePostBee(BeeHolder beeHolder) {
        // If the bee is already getting delivered
        if (bee.getPersistentDataContainer().has(B_s_BeePost.namespacedKey("delivered")))
        {
            Deliver(bee, player);
        }
        // If player intends to put a message to a bee...
        else if (player.getInventory().getItemInMainHand().getType() == Material.WRITTEN_BOOK &&
            !bee.getPersistentDataContainer().has(B_s_BeePost.namespacedKey("message")))
        {
            AddMessageToBee(player,bee); //...handle it.
        }
        // If the bee already has a message, and player intend to give it
        else if (bee.getPersistentDataContainer().has(B_s_BeePost.namespacedKey("message")) &&
                !bee.getPersistentDataContainer().has(B_s_BeePost.namespacedKey("present")))
        {
            AddPresentToBee(player, bee);
        }
    }

    private void Deliver(Bee bee, Player receiver){
        //String address = bee.getPersistentDataContainer().get(B_s_BeePost.namespacedKey("address"), PersistentDataType.STRING);
        Utilities.dropDelivery(bee);
        bee.getPersistentDataContainer().remove(B_s_BeePost.namespacedKey("message"));
        bee.getPersistentDataContainer().remove(B_s_BeePost.namespacedKey("address"));
        bee.getPersistentDataContainer().remove(B_s_BeePost.namespacedKey("present"));
        bee.getPersistentDataContainer().remove(B_s_BeePost.namespacedKey("delivered"));
    }

    private void AddMessageToBee(Player sender, Bee bee){
        ItemStack book = sender.getInventory().getItemInMainHand().clone(); // Get the book
        book.setAmount(1);
        sender.getInventory().getItemInMainHand().subtract(1); // Take one book away

        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if (bookMeta.getTitle() == null) return;

        // Get the receiver
        OfflinePlayer receiver = Bukkit.getOfflinePlayerIfCached(bookMeta.getTitle());
        if (receiver == null || !receiver.hasPlayedBefore())
        {
            Utilities.swapHands(sender); // Cancel the book opening
            sender.sendMessage("§4This username is not valid!");
            return;
        }

        if (bee.getLocation().getWorld().getEnvironment() != World.Environment.NORMAL)
        {
            Utilities.swapHands(sender); // Cancel the book opening
            sender.sendMessage("§4You can only sand messages from the overworld!");
            return;
        }

        Utilities.swapHands(sender); // Cancel the book opening

        // Give the receiver address to the bee
        bee.getPersistentDataContainer().set(B_s_BeePost.namespacedKey("address"), PersistentDataType.STRING,receiver.getUniqueId().toString());

        // Give the message to the bee
        String message = book.serialize().toString();
        bee.getPersistentDataContainer().set(B_s_BeePost.namespacedKey("message"), PersistentDataType.STRING,message);

        new BukkitRunnable() {
            @Override
            public void run() {
                // If the bee got the present go to the next phase
                if (bee.getPersistentDataContainer().has(B_s_BeePost.namespacedKey("present"))) this.cancel();

                // Handel if something happens while the present is not yet received by the bee
                if ((!sender.isOnline() || sender.getWorld() != bee.getWorld() || bee.isDead()) &&
                        !bee.getPersistentDataContainer().has(B_s_BeePost.namespacedKey("present")))
                {
                    if (!bee.isDead())
                    {
                        bee.getPersistentDataContainer().remove(B_s_BeePost.namespacedKey("message"));
                        bee.getPersistentDataContainer().remove(B_s_BeePost.namespacedKey("address"));
                    }
                    this.cancel();
                }

                bee.setAnger(0);
                bee.setHive(null);
                bee.setFlower(null);

                if(bee.getLocation().distance(sender.getLocation()) > 4) bee.getPathfinder().moveTo(sender);

                if (receiver.isOnline())    Utilities.spawnParticleForBee(bee.getLocation(), Color.GREEN);
                else                        Utilities.spawnParticleForBee(bee.getLocation(), Color.GRAY);

            }
        }.runTaskTimer(B_s_BeePost.getInstance(), 0L, 10L);

    }

    private void  AddPresentToBee(Player sender, Bee bee){

        String present = sender.getInventory().getItemInMainHand().serialize().toString();
        sender.getInventory().getItemInMainHand().setAmount(0);
        bee.getPersistentDataContainer().set(B_s_BeePost.namespacedKey("present"), PersistentDataType.STRING,present);

        new BukkitRunnable() {

            Random random = new Random();
            final Vector beeDirection = new Vector(random.nextBoolean() ? -5 : 5,5,random.nextBoolean() ? -5 : 5);

            @Override
            public void run() {
                if (bee.getLocation().distance(sender.getLocation())>=32)
                {
                    sender.sendMessage("pajkdfsopas");
                    this.cancel();
                    SaveManager.SaveDelivery(bee);
                    Utilities.spawnBeeDisappearParticles(bee.getLocation());
                    bee.remove();
                }

                bee.setAnger(0);
                bee.setHive(null);
                bee.setFlower(null);

                bee.getPathfinder().moveTo(bee.getLocation().add(beeDirection));
                Utilities.spawnParticleForBee(bee.getLocation(), Color.AQUA);
            }
        }.runTaskTimer(B_s_BeePost.getInstance(), 0L, 10L);
    }

}
