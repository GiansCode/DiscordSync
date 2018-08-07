package com.thesquadmc.discordsync.plugin.util;

import com.thesquadmc.discordsync.plugin.DiscordSync;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class UtilServer
{
    private static final DiscordSync plugin = JavaPlugin.getPlugin(DiscordSync.class);

    static DiscordSync getPlugin()
    {
        return plugin;
    }

    public static void registerListener(Listener listener)
    {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    public static void dispatchCommand(String command)
    {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
