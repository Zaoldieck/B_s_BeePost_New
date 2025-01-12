package biraw.online.b_s_BeePost.Bee;

import biraw.online.b_s_BeePost.B_s_BeePost;
import biraw.online.b_s_BeePost.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BeeHolder {

    private Bee entity; public Bee getEntity() {return entity;}

    public BeeState state;

    private OfflinePlayer sender; public OfflinePlayer getSender() {return sender;}
    private OfflinePlayer receiver; public OfflinePlayer getReceiver() {return receiver;}

    private ItemStack message; public ItemStack getMessage() {return message;}
    private ItemStack present; public ItemStack getPresent() {return present;}
    public void addPresent(ItemStack present){
        if (this.present==null) this.present = present;
    }

    public BeeHolder(Bee entity, OfflinePlayer sender, ItemStack message){

        this.entity = entity;

        this.sender = sender;
        this.message = message;

        this.state = BeeState.PRESENTLESS;

        //TODO: figure out the receiver

        B_s_BeePost.ActiveBees.add(this);
    }

    // Initialize a delivery bee
    public BeeHolder(File file){
        if (!file.exists())
        {
            B_s_BeePost.Debug("Trying to load bee, that doesn't exist.");
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Deserialize and load the data
        try {
            message = config.getItemStack("message");
            present = config.getItemStack("present");

            // Load sender and receiver UUIDs and convert them back to Player objects
            UUID senderUUID = UUID.fromString(config.getString("sender"));
            UUID receiverUUID = UUID.fromString(config.getString("receiver"));

            sender = Bukkit.getOfflinePlayer(senderUUID);
            receiver = Bukkit.getOfflinePlayer(receiverUUID);

            // Handle case where one of the players is non-existent (No idea how that might happen btw)
            if (sender == null) {
                B_s_BeePost.Debug("Sender does not exist.");
            }
            if (receiver == null) {
                B_s_BeePost.Debug("Receiver does not exist.");
            }
        } catch (Exception e) {
            B_s_BeePost.Debug("Error loading data from file: " + e);
        }

        state = BeeState.DELIVERY;
        B_s_BeePost.ActiveBees.add(this);
    }

    // Save the data of the bee to a file
    public void Save() throws IOException {

        state = BeeState.DELIVERY;

        // Get a random file, that doesn't already exist
        File saveFile;
        do {
            saveFile = new File("plugins/BeePost Messages/"+ UUID.randomUUID() +".yml");
        }
        while (!saveFile.createNewFile());

        // Open the file in yaml
        YamlConfiguration config = YamlConfiguration.loadConfiguration(saveFile);

        // Serialize the ItemStack and save it to the specified path
        config.set("message", message);
        config.set("present", present);
        config.set("sender", sender.getUniqueId().toString());
        config.set("receiver", receiver.getUniqueId().toString());

        // Save the file
        try {
            config.save(saveFile);
        } catch (IOException e) {
            B_s_BeePost.Debug("File saving error: " + e);
        }

        // Remove the bee
        Utilities.spawnBeeDisappearParticles(entity.getLocation());
        entity.remove();
    }

    public void Deliver(Player opener){
        if (opener.getUniqueId() != receiver.getUniqueId())
        {
            opener.sendMessage("§cThis message is not for you!");
            return;
        }

        state = BeeState.FINISHED;

        entity.getWorld().dropItem(entity.getLocation(),message);
        entity.getWorld().dropItem(entity.getLocation(),present);
    }

}
