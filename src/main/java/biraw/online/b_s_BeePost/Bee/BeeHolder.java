package biraw.online.b_s_BeePost.Bee;

import biraw.online.b_s_BeePost.B_s_BeePost;
import biraw.online.b_s_BeePost.LanguageManager;
import biraw.online.b_s_BeePost.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class BeeHolder {

    private Bee entity; public Bee getEntity() {return entity;}
                        public void setEntity(Bee entity) {this.entity = entity;}

    public BeeState state;

    private UUID sender; public OfflinePlayer getSender() {return Bukkit.getOfflinePlayer(sender);}
    private UUID receiver; public OfflinePlayer getReceiver() {return Bukkit.getOfflinePlayer(receiver);}

    private ItemStack message; public ItemStack getMessage() {return message;}
    private ItemStack present; public ItemStack getPresent() {return present;}
    public void addPresent(ItemStack present){
        if (this.present==null)
        {
            this.present = present;
            state = BeeState.ASCENDING;
        }
    }

    public BeeHolder(Bee entity, OfflinePlayer sender, OfflinePlayer receiver, ItemStack message){

        this.entity = entity;

        this.sender = sender.getUniqueId();
        this.receiver = receiver.getUniqueId();

        this.message = message;
        this.state = BeeState.PRESENTLESS;

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

            sender = senderUUID;
            receiver = receiverUUID;

        } catch (Exception e) {
            B_s_BeePost.Debug("Error loading data from file: " + e);
        }

        state = BeeState.ASCENDED;
        B_s_BeePost.ActiveBees.add(this);
    }

    // Save the data of the bee to a file
    public void Save() throws IOException {

        //Check if message folder exists
        String directoryPath = "plugins/BeePost messages/";
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            B_s_BeePost.Debug("Directory does not exist: " + directoryPath);
            return;
        }

        // Get a random file, that doesn't already exist
        File saveFile;
        do {
            saveFile = new File(directoryPath + UUID.randomUUID()+".yml");
        }
        while (!saveFile.createNewFile());

        // Open the file in yaml
        YamlConfiguration config = YamlConfiguration.loadConfiguration(saveFile);

        // Serialize the ItemStack and save it to the specified path
        config.set("message", message);
        config.set("present", present);
        config.set("sender", sender.toString());
        config.set("receiver", receiver.toString());

        // Save the file
        try {
            config.save(saveFile);
        } catch (IOException e) {
            B_s_BeePost.Debug("File saving error: " + e);
        }

        // Remove the bee
        B_s_BeePost.ActiveBees.remove(this);
        if (entity == null) return;
        entity.remove();
        state = BeeState.FINISHED;
    }

    public void BeeGoesOut(){
        Utilities.spawnBeeDisappearParticles(entity.getLocation());
        entity.remove();
        state = BeeState.ASCENDED;
    }

    public void Deliver(Player opener){
        if (!Objects.equals(opener.getUniqueId(), receiver))
        {
            B_s_BeePost.Debug(opener.getUniqueId().toString());
            B_s_BeePost.Debug(receiver.toString());
            opener.sendMessage(LanguageManager.getNotForYou());
            return;
        }

        state = BeeState.FINISHED;

        entity.getWorld().dropItem(entity.getLocation(),message);
        entity.getWorld().dropItem(entity.getLocation(),present);
        B_s_BeePost.ActiveBees.remove(this);
        entity = null;
    }

}
