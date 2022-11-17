package me.darkwinged.RiotPolls.libaries;

import me.darkwinged.RiotPolls.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class Utils {

    private static final Main plugin = Main.getInstance;

    // ---- [ Managing chat color within the plugin ] ----
    public static String chatColor(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    // ---- [ Managing chat color within the plugin | Supports Amount ] ----
    public static String chatColor(String s, Double amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
        String converted = nf.format(amount);
        return ChatColor.translateAlternateColorCodes('&', s)
                .replaceAll("%amount%", converted);
    }

    // ---- [ Converting a lore to include colors ] ----
    public static List<String> getConvertedLore(FileConfiguration config, String path) {
        if (config == null) return null;
        List<String> oldList = config.getStringList(path + ".lore");
        List<String> newList = new ArrayList<>();
        for (String a : oldList)
            newList.add(ChatColor.translateAlternateColorCodes('&', a));
        return newList;
    }

    // ---- [ Available space ] ----
    public static boolean hasSpace(Player player, ItemStack targetItem) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType() == targetItem.getType()) {
                if (item.getAmount() != item.getMaxStackSize()) {
                    item.setAmount(item.getAmount() + 1);
                    return true;
                }
            }
        }
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(targetItem);
            return true;
        }
        return false;
    }

    // ---- [ Send an Embed to discord ] ----
    public static String sendPoll(String title, String description, int voteUp, int voteDown, strutPoll poll) {
        if (plugin.discordChannel == null) return "";
        String messageID = "";
        EmbedBuilder pollEmbed = new EmbedBuilder()
                .setAuthor("Submitter - " + poll.getAuthor(), null,
                        "https://crafatar.com/avatars/" + Bukkit.getPlayer(poll.getAuthor()).getUniqueId().toString() + "?default=MHF_Steve&overlay")
                .setColor(Color.green)
                .setDescription("**New Suggestion - " + replaceAllColorCodes(title) + "**\n" + replaceAllColorCodes(description) + "\n\n**__Results__**\n✅ " + voteUp + " | ❌ " + voteDown)
                .setFooter("**Submitted: " + poll.getCreated().getDayOfMonth() + "-" + poll.getCreated().getMonthValue() + "-" + poll.getCreated().getYear() + "**");


        plugin.discordChannel.sendMessageEmbeds(pollEmbed.build()).queue((message) -> {
            polls.add(new strutPoll(poll.getTitle(), poll.getDescription(), poll.getAuthor(), poll.getAuthorID(), poll.getUpVotes(), poll.getDownVotes(), poll.getCreated(), message.getIdLong()));
        });
        return messageID;
    }

    // ---- [ Update Embed ] ----
    public static void updatePoll(Player player) {
        if (plugin.discordChannel == null) return;
        strutPoll poll = polls.get(polls_temp.get(player.getUniqueId()));

        EmbedBuilder pollEmbed = new EmbedBuilder()
                .setAuthor("Submitter - " + poll.getAuthor(), null,
                        "https://crafatar.com/avatars/" + poll.getAuthorID() + "?default=MHF_Steve&overlay")
                .setColor(Color.green)
                .setDescription("**New Suggestion - " + replaceAllColorCodes(poll.getTitle()) + "**\n" + replaceAllColorCodes(poll.getDescription()) + "\n\n**__Results__**\n✅ " + poll.getUpVotes() + " | ❌ " + poll.getDownVotes())
                .setFooter("**Submitted: " + poll.getCreated().getDayOfMonth() + "-" + poll.getCreated().getMonthValue() + "-" + poll.getCreated().getYear() + "**");

        plugin.discordChannel.retrieveMessageById(String.valueOf(poll.getMessageID())).queue((message ->
                message.editMessageEmbeds(pollEmbed.build()).queue()));

        Utils.poll_ID.remove(player.getUniqueId());
    }

    // ---- [ Remove Embed ] ----
    public static void removeEmbed(String messageID) {
        if (plugin.discordChannel == null) return;
        plugin.discordChannel.retrieveMessageById(messageID).queue(message -> message.delete().queue());
        if (plugin.filePolls.getConfig().contains(messageID)) {
            plugin.filePolls.getConfig().set(messageID, null);
            plugin.filePolls.saveConfig();
        }
    }

    // ---- [ Removes all color codes from string ] ----
    public static String replaceAllColorCodes(String string) {
        return string.replaceAll("&a", "")
                .replaceAll("&b", "")
                .replaceAll("&c", "")
                .replaceAll("&d", "")
                .replaceAll("&e", "")
                .replaceAll("&f", "")
                .replaceAll("&1", "")
                .replaceAll("&2", "")
                .replaceAll("&3", "")
                .replaceAll("&4", "")
                .replaceAll("&5", "")
                .replaceAll("&6", "")
                .replaceAll("&7", "")
                .replaceAll("&8", "")
                .replaceAll("&9", "")
                .replaceAll("&k", "")
                .replaceAll("&l", "")
                .replaceAll("&m", "")
                .replaceAll("&n", "")
                .replaceAll("&o", "")
                .replaceAll("&r", "");

    }

    // ---- [ Cached Items ] ----
    public static List<strutPoll> polls = new ArrayList<>();
    public static Map<UUID, strutPoll> poll_ID = new HashMap<>();
    public static Map<UUID, Integer> polls_temp = new HashMap<>();

    // ---- [ Saving active polls ] ----
    public static void saveActivePolls() {
        for (strutPoll poll : polls) {
            plugin.filePolls.getConfig().set(poll.getMessageID() + ".title", poll.getTitle());
            plugin.filePolls.getConfig().set(poll.getMessageID() + ".description", poll.getDescription());
            plugin.filePolls.getConfig().set(poll.getMessageID() + ".author", poll.getAuthor());
            plugin.filePolls.getConfig().set(poll.getMessageID() + ".authorID", poll.getAuthorID().toString());
            plugin.filePolls.getConfig().set(poll.getMessageID() + ".upVotes", poll.getUpVotes());
            plugin.filePolls.getConfig().set(poll.getMessageID() + ".downVotes", poll.getDownVotes());
            plugin.filePolls.getConfig().set(poll.getMessageID() + ".created", poll.getCreated().toString());
            plugin.filePolls.getConfig().set(poll.getMessageID() + ".messageID", poll.getMessageID());

            List<String> list = new ArrayList<>();
            for (UUID uuid : poll.getVotes()) {
                list.add(uuid.toString());
            }
            plugin.filePolls.getConfig().set(poll.getMessageID() + ".votes", list);

            plugin.filePolls.saveConfig();
        }
    }

    // ---- [ Loading active polls ] ----
    public static void loadActivePolls() {
        for (String key : plugin.filePolls.getConfig().getKeys(false)) {
            List<UUID> list = new ArrayList<>();
            for (String uuid : plugin.filePolls.getConfig().getStringList(key + ".votes")) {
                list.add(UUID.fromString(uuid));
            }

            polls.add(new strutPoll(plugin.filePolls.getConfig().getString(key + ".title"),
                    plugin.filePolls.getConfig().getString(key + ".description"),
                    plugin.filePolls.getConfig().getString(key + ".author"),
                    UUID.fromString(plugin.filePolls.getConfig().getString(key + ".authorID")),
                    plugin.filePolls.getConfig().getInt(key + ".upVotes"),
                    plugin.filePolls.getConfig().getInt(key + ".downVotes"),
                    LocalDateTime.parse(plugin.filePolls.getConfig().getString(key + ".created")),
                    plugin.filePolls.getConfig().getLong(key + ".messageID"),
                    list)
            );
        }
    }

    // ---- [ Method for working out position of poll in array ] ----
    public static int slotToArrayIndex(int slot) {
        switch (slot) {
            case 10:
                return 0;
            case 11:
                return 1;
            case 12:
                return 2;
            case 13:
                return 3;
            case 14:
                return 4;
            case 15:
                return 5;
            case 16:
                return 6;
        }
        return 0;
    }
}
