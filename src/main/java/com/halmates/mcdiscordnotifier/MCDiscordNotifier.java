package com.halmates.mcdiscordnotifier;

import com.halmates.mcdiscordnotifier.discord.channel.ServerLog;
import com.halmates.mcdiscordnotifier.discord.channel.ServerStatus;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

public final class MCDiscordNotifier extends JavaPlugin {
    private static String _serverName;
    private static String _serverVersion;
    private static FileConfiguration _config;
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        _config = getConfig();

        _serverName = getConfigString("server_name");
        _serverVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        try {
            ServerStatus.createInstance(this);
            ServerLog.createInstance(this);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String message = this.getConfigMessage("on_plugin_enable");
        Bukkit.getLogger().info(message);
        ServerLog.getInstance().createServerEnableMessage().send();
        ServerStatus.getInstance().createServerEnableMessage().send();

        getServer().getPluginManager().registerEvents(new RoomEntranceListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        String message = this.getConfigMessage("on_plugin_disable");
        Bukkit.getLogger().info(message);
        ServerLog.getInstance().createServerDisableMessage().send();
        ServerStatus.getInstance().createServerDisableMessage().send();
    }

    public String getConfigValue(String key) {
        if (_config == null) {
            return null;
        }
        return _config.getString(key);
    }

    public String getConfigString(String key) {
        if (_config == null) {
            return null;
        }
        return _config.getString("string." + key);
    }

    public String getConfigMessage(String key) {
        if (_config == null) {
            return null;
        }
        return _config.getString("message." + key);
    }

    public void setConfigValue(String key, String value){
        _config.set(key, value);
    }

    public String getServerVersion(){
        return _serverVersion;
    }

    public String getServerName(){
        return _serverName;
    }
}
