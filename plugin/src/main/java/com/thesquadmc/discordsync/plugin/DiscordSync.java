package com.thesquadmc.discordsync.plugin;

import com.thesquadmc.discordsync.plugin.command.MainCommand;
import com.thesquadmc.discordsync.plugin.data.DataManager;
import com.thesquadmc.discordsync.plugin.vault.VaultManager;
import com.thesquadmc.discordsync.plugin.discord.DiscordManager;
import com.thesquadmc.discordsync.plugin.util.Message;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordSync extends JavaPlugin
{
    private DataManager dataManager;
    private DiscordManager discordManager;
    private VaultManager vaultManager;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        Message.init(this);

        dataManager = new DataManager(this);
        discordManager = new DiscordManager(this);
        vaultManager = new VaultManager(this);

        getCommand("link").setExecutor(new MainCommand(this));
    }

    public <T> T getConfig(String path)
    {
        return getConfig(path, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfig(String path, Object defaultValue)
    {
        return (T) getConfig().get(path, defaultValue);
    }

    /* Dependency Access */
    public DataManager getDataManager()
    {
        return dataManager;
    }

    public DiscordManager getDiscordManager()
    {
        return discordManager;
    }

    public VaultManager getVaultManager()
    {
        return vaultManager;
    }
}
