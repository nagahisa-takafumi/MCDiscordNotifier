package com.halmates.mcdiscordnotifier.discord.channel;

import com.halmates.mcdiscordnotifier.MCDiscordNotifier;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;

public class ServerLog extends ListenerAdapter {
    private static final String CRAFATAR_URL = "https://crafatar.com/avatars/%s";
    private MCDiscordNotifier _plugin;
    private TextChannel _channel;
    private static ServerLog _instance;
    private MessageEmbed _tempMe;

    private ServerLog(MCDiscordNotifier plugin) throws LoginException, InterruptedException {
        _plugin = plugin;
        String token = _plugin.getConfigValue("DISCORD.TOKEN");
        String channelId = _plugin.getConfigValue("DISCORD.CHANNEL.SERVER_LOG");
        JDA jda = JDABuilder.createDefault(token).build();
        jda.addEventListener(this);
        jda.awaitReady();
        _channel = jda.getTextChannelById(channelId);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        String botTag = event.getJDA().getSelfUser().getAsTag();
        String baseText = _plugin.getConfigMessage("on_discord_ready");
        String message = String.format(baseText, botTag);
        Bukkit.getLogger().info(message);
    }

    public static void createInstance(MCDiscordNotifier plugin) throws LoginException, InterruptedException {
        _instance = new ServerLog(plugin);
    }

    public static ServerLog getInstance(){
        return _instance;
    }

    public ServerLog createServerEnableMessage(){
        EmbedBuilder eb = new EmbedBuilder();
        String serverVersion = _plugin.getServerVersion();
        int maxPlayers = Bukkit.getServer().getMaxPlayers();

        String text = _plugin.getConfigMessage("on_server_enable");

        eb.setColor(Color.CYAN);
        eb.setTitle(text);

        text = _plugin.getConfigString("version");
        eb.addField(text, serverVersion, true);

        text = _plugin.getConfigString("max_players");
        eb.addField(text, String.valueOf(maxPlayers), true);

        _tempMe = eb.build();
        return this;
    }

    public ServerLog createServerDisableMessage(){
        EmbedBuilder eb = new EmbedBuilder();
        String serverVersion = _plugin.getServerVersion();
        int maxPlayers = Bukkit.getServer().getMaxPlayers();

        String text = _plugin.getConfigMessage("on_server_disable");

        eb.setColor(Color.RED);
        eb.setTitle(text);

        text = _plugin.getConfigString("version");
        eb.addField(text, serverVersion, true);

        text = _plugin.getConfigString("max_players");
        eb.addField(text, String.valueOf(maxPlayers), true);

        _tempMe = eb.build();
        return this;
    }

    public ServerLog createPlayerJoinMessage(Player player){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.GREEN);

        String text = _plugin.getConfigString("on_player_join");
        eb.setTitle(text);

        eb.setThumbnail(String.format(CRAFATAR_URL, player.getUniqueId()));

        text = _plugin.getConfigString("player_name");
        eb.addField(text, player.getDisplayName(), true);

        text = _plugin.getConfigString("player_lv");
        eb.addField(text, String.valueOf(player.getLevel()), true);

        _tempMe = eb.build();
        return this;
    }

    public ServerLog createPlayerLeaveMessage(Player player){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.YELLOW);

        String text = _plugin.getConfigString("on_player_quit");
        eb.setTitle(text);

        eb.setThumbnail(String.format(CRAFATAR_URL, player.getUniqueId()));

        text = _plugin.getConfigString("player_name");
        eb.addField(text, player.getDisplayName(), true);

        text = _plugin.getConfigString("player_lv");
        eb.addField(text, String.valueOf(player.getLevel()), true);

        _tempMe = eb.build();
        return this;
    }

    public void send(){
        _channel.sendMessage(_tempMe).complete();
    }
}
