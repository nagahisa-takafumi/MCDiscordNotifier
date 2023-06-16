package com.halmates.mcdiscordnotifier.discord.channel;

import com.halmates.mcdiscordnotifier.MCDiscordNotifier;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.ArrayList;

public class ServerStatus extends ListenerAdapter {
    private MCDiscordNotifier _plugin;
    private TextChannel _channel;
    private static ServerStatus _instance;
    private MessageEmbed _tempMe;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        String botTag = event.getJDA().getSelfUser().getAsTag();
        String baseText = _plugin.getConfigMessage("on_discord_ready");
        String message = String.format(baseText, botTag);
        Bukkit.getLogger().info(message);
    }

    private ServerStatus(MCDiscordNotifier plugin) throws LoginException, InterruptedException {
        _plugin = plugin;
        String token = _plugin.getConfigValue("DISCORD.TOKEN");
        String channelId = _plugin.getConfigValue("DISCORD.CHANNEL.SERVER_STATUS");
        JDA jda = JDABuilder.createDefault(token).build();
        jda.addEventListener(this);
        jda.awaitReady();
        _channel = jda.getTextChannelById(channelId);
    }

    public static void createInstance(MCDiscordNotifier plugin) throws LoginException, InterruptedException {
        _instance = new ServerStatus(plugin);
    }

    public static ServerStatus getInstance(){
        return _instance;
    }

    /**
     * サーバーの起動時に更新するEmbedメッセージを作成
     * @return MessageEmbed 更新後のEmbedメッセージ
     */
    public ServerStatus createServerEnableMessage(){
        EmbedBuilder eb = new EmbedBuilder();
        String serverName = _plugin.getServerName();
        String serverVersion = _plugin.getServerVersion();

        eb.setColor(Color.BLUE);
        eb.setTitle(serverName);

        String text = _plugin.getConfigString("version");
        eb.addField(text, serverVersion, true);

        text = _plugin.getConfigString("online_players");
        eb.addField(text, "0", true);

        eb.setDescription(_plugin.getConfigMessage("none_player"));

        _tempMe = eb.build();
        return this;
    }

    /**
     * サーバーの停止時に更新するEmbedメッセージを作成
     * @return MessageEmbed 更新後のEmbedメッセージ
     */
    public ServerStatus createServerDisableMessage(){
        EmbedBuilder eb = new EmbedBuilder();
        String serverName = _plugin.getServerName();

        eb.setColor(Color.RED);
        eb.setTitle(serverName);

        eb.setDescription(_plugin.getConfigMessage("disable_server"));

        _tempMe = eb.build();
        return this;
    }

    /**
     * プレイヤー入室時に更新するEmbedメッセージを作成
     * @return MessageEmbed 更新後のEmbedメッセージ
     */
    public ServerStatus createPlayerJoinMessage(){
        EmbedBuilder eb = new EmbedBuilder();
        String serverName = _plugin.getServerName();
        String serverVersion = _plugin.getServerVersion();
        int onlinePlayersCount = Bukkit.getServer().getOnlinePlayers().size();
        ArrayList<Player> players = getOnlinePlayers();

        eb.setColor(Color.GREEN);
        eb.setTitle(serverName);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            sb.append(String.format("- %s (Lv.%d)", p.getDisplayName(), p.getLevel()));
            sb.append("\n");
        }
        String text = _plugin.getConfigString("on_play");
        eb.addField(text, sb.toString(), false);

        text = _plugin.getConfigString("version");
        eb.addField(text, serverVersion, true);

        text = _plugin.getConfigString("online_players");
        eb.addField(text, String.valueOf(onlinePlayersCount), true);

        _tempMe = eb.build();
        return this;
    }

    /**
     * プレイヤー退室時に更新するEmbedメッセージを作成
     * @return MessageEmbed 更新後のEmbedメッセージ
     */
    public ServerStatus createPlayerLeaveMessage(Player player){
        EmbedBuilder eb = new EmbedBuilder();
        String serverName = _plugin.getServerName();
        String serverVersion = _plugin.getServerVersion();
        int onlinePlayersCount = Bukkit.getServer().getOnlinePlayers().size() - 1;
        ArrayList<Player> players = getOnlinePlayers(player);
        String text;

        eb.setTitle(serverName);

        if (onlinePlayersCount == 0){
            eb.setColor(Color.BLUE);
            eb.setDescription(_plugin.getConfigMessage("none_player"));
        } else {
            eb.setColor(Color.GREEN);
            eb.setColor(Color.green);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                sb.append(String.format("- %s (Lv.%d)", p.getDisplayName(), p.getLevel()));
                sb.append("\n");
            }
            text = _plugin.getConfigString("on_play");
            eb.addField(text, sb.toString(), false);
        }

        text = _plugin.getConfigString("version");
        eb.addField(text, serverVersion, true);

        text = _plugin.getConfigString("online_players");
        eb.addField(text, String.valueOf(onlinePlayersCount), true);

        _tempMe = eb.build();
        return this;
    }

    private ArrayList<Player> getOnlinePlayers(){
        ArrayList<Player> players = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player);
        }
        return players;
    }

    private ArrayList<Player> getOnlinePlayers(Player leavePlayer){
        ArrayList<Player> players = new ArrayList<>();
        String leavePlayerName = "";
        if (null != leavePlayer){
            leavePlayerName = leavePlayer.getDisplayName();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerName = player.getDisplayName();
            if (playerName.equals(leavePlayerName)){
                continue;
            }
            players.add(player);
        }
        return players;
    }

    public void send(){
        String message_id = _plugin.getConfigValue("value.server_status_message_id");

        Message statusMessage = null;

        if (!"".equals(message_id)){
            TextChannel channel = _channel;

            try{
                statusMessage = channel.retrieveMessageById(message_id).complete();
            } catch (NullPointerException e){
                e.printStackTrace();
            } catch (ErrorResponseException e2){
                Bukkit.getLogger().info("No server status message");
            }
        }

        if (null == statusMessage || "".equals(message_id)){
            Message message;

            message = _channel.sendMessage(_tempMe).complete();
            _plugin.setConfigValue("value.server_status_message_id", message.getId());
            _plugin.saveConfig();
        } else {
            statusMessage.editMessageEmbeds(_tempMe).queue();
        }
    }
}
