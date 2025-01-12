package biraw.online.b_s_BeePost;

import biraw.online.b_s_BeePost.Bee.BeeAI;
import biraw.online.b_s_BeePost.Bee.BeeHolder;
import biraw.online.b_s_BeePost.Bee.BeeState;
import org.bukkit.*;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

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
            HandlePostBee(B_s_BeePost.getBeeHolderForEntity(bee),event.getPlayer());
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
                sender.sendMessage(LanguageManager.getInvalidUsername());
                return;
            }

            // Check the environment
            if (sender.getLocation().getWorld().getEnvironment() != World.Environment.NORMAL)
            {
                Utilities.swapHands(sender);
                sender.sendMessage(LanguageManager.getOnlyOverworld());
                return;
            }

            // At this point we are sure they want to send a bee
            // Take one book away
            sender.getInventory().getItemInMainHand().subtract(1);

            Utilities.swapHands(sender);


            // Create bee holder, and set up AI
            BeeHolder beeHolder = new BeeHolder(bee,sender,receiver,book);
            BeeAI.BeeWaitingForPresent(beeHolder);
        }

        event.setCancelled(true);
    }

    private void HandlePostBee(BeeHolder beeHolder, Player clicker) {
        // If the bee is already getting delivered
        if (beeHolder.state == BeeState.DELIVERY)
        {
            beeHolder.Deliver(clicker);
        }
        // If the bee is waiting for present
        if (beeHolder.state == BeeState.PRESENTLESS)
        {
            ItemStack present = clicker.getInventory().getItemInMainHand().clone();
            clicker.getInventory().setItemInMainHand(null);
            beeHolder.addPresent(present);
            BeeAI.BeeAscending(beeHolder);
        }
    }
}
