package me.darkwinged.RiotPolls;

import me.darkwinged.RiotPolls.commands.poll;
import me.darkwinged.RiotPolls.events.menuEvents;
import me.darkwinged.RiotPolls.libaries.CustomConfig;
import me.darkwinged.RiotPolls.libaries.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

public final class Main extends JavaPlugin {

    public static Main getInstance;
    private JDA discord;
    public TextChannel discordChannel;

    public CustomConfig fileMessage;
    public CustomConfig filePolls = new CustomConfig(this, "active-polls", true);

    public void onEnable() {
        // ---- [ Initializing instance of main class | manager classes | register placeholder ] ----
        getInstance = this;

        // ---- [ Loading Commands | Loading Events | Loading YML Files ] ----
        loadCommands();
        loadEvents();
        saveDefaultConfig();
        initializeConnection();
        filePolls.saveDefaultConfig();

        // ---- [ Loading Active Polls ] ----
        Utils.loadActivePolls();

        // ---- [ Loading lang file ] ----
        fileMessage = new CustomConfig(this, "lang/" + this.getConfig().getString("Storage.Language File"), true);
        fileMessage.saveDefaultConfig();
        // ---- [ Startup message ] ----
        getServer().getConsoleSender().sendMessage(Utils.chatColor(this.fileMessage.getConfig().getString("startup")));
    }

    public void onDisable() {
        if (discord != null) {
            discord.shutdownNow();
        }

        // ---- [ Save all polls inside of the server ] ----
        Utils.saveActivePolls();

        // ---- [ shutdown message ] ----
        getServer().getConsoleSender().sendMessage(Utils.chatColor(this.fileMessage.getConfig().getString("shutdown")));
    }

    public void loadCommands() {
        getCommand("poll").setExecutor(new poll());
    }
    public void loadEvents() {
        getServer().getPluginManager().registerEvents(new menuEvents(), this);
    }

    public void initializeConnection() {
        try {
            discord = JDABuilder.createDefault(getConfig().getString("Storage.client-token")).build().awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
        if (discord == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getConfig().getString("Storage.text-channel-id") == null) return;
        discordChannel = discord.getTextChannelById(getConfig().getString("Storage.text-channel-id"));
    }



}
