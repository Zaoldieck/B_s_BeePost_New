package biraw.online.b_s_BeePost;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class LanguageManager {

    private static File langFile;
    private static YamlConfiguration langConfig;

    // Default language strings
    private static String postBeeApproaching = "§6A post bee is approaching you!";
    private static String invalidUsername = "§4This username is not valid!";
    private static String postBeeDisappeared = "§4You went too far, and the post bee disappeared...";
    private static String messageSent = "§6Message was sent to: §a";
    private static String notForYou = "§cThis message is not for you!";
    private static String onlyOverworld = "§cYou can only send messages from the overworld!";

    public LanguageManager() {
        langFile = new File("plugins/bee_post_lang.yml");
        ensureDefaults();
    }

    // Ensure the language file exists and contains the default strings
    private void ensureDefaults() {
        if (!langFile.exists()) {
            try {
                langFile.createNewFile();
                saveDefaults();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadConfig();
    }

    // Save the default values into the lang file
    private void saveDefaults() {
        langConfig = new YamlConfiguration();
        langConfig.set("postBeeApproaching", postBeeApproaching);
        langConfig.set("invalidUsername", invalidUsername);
        langConfig.set("postBeeDisappeared", postBeeDisappeared);
        langConfig.set("messageSent", messageSent);
        langConfig.set("notForYou", notForYou);
        langConfig.set("onlyOverworld", onlyOverworld);

        try {
            langConfig.save(langFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load the language file and set values from it
    private void loadConfig() {
        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Fallback to defaults if a value is missing
        postBeeApproaching = langConfig.getString("postBeeApproaching", postBeeApproaching);
        invalidUsername = langConfig.getString("invalidUsername", invalidUsername);
        postBeeDisappeared = langConfig.getString("postBeeDisappeared", postBeeDisappeared);
        messageSent = langConfig.getString("messageSent", messageSent);
        notForYou = langConfig.getString("notForYou", notForYou);
        onlyOverworld = langConfig.getString("onlyOverworld", onlyOverworld);
    }

    // Getter methods to access the language strings
    public static String getPostBeeApproaching() {
        return postBeeApproaching;
    }

    public static String getInvalidUsername() {
        return invalidUsername;
    }

    public static String getPostBeeDisappeared() {
        return postBeeDisappeared;
    }

    public static String getMessageSent() {
        return messageSent;
    }

    public static String getNotForYou() {
        return notForYou;
    }

    public static String getOnlyOverworld() {
        return onlyOverworld;
    }
}
