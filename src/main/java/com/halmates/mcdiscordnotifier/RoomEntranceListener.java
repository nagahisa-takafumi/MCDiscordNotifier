package com.halmates.mcdiscordnotifier;

import com.halmates.mcdiscordnotifier.discord.channel.ServerLog;
import com.halmates.mcdiscordnotifier.discord.channel.ServerStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RoomEntranceListener implements Listener {
    private MCDiscordNotifier _plugin;

    public RoomEntranceListener(MCDiscordNotifier plugin){
        _plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player joinPlayer = event.getPlayer();
        String playerName = joinPlayer.getDisplayName();
        String baseText = _plugin.getConfigMessage("on_player_join");
        String message = String.format(baseText, playerName);

        ServerLog.getInstance().createPlayerJoinMessage(joinPlayer).send();
        ServerStatus.getInstance().createPlayerJoinMessage().send();
        Bukkit.getLogger().info(message);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player leavePlayer = event.getPlayer();
        String playerName = leavePlayer.getDisplayName();
        String baseText = _plugin.getConfigMessage("on_player_quit");
        String message = String.format(baseText, playerName);

        ServerLog.getInstance().createPlayerLeaveMessage(leavePlayer).send();
        ServerStatus.getInstance().createPlayerLeaveMessage(leavePlayer).send();
        Bukkit.getLogger().info(message);
    }
}
