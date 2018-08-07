package com.thesquadmc.discordsync.plugin.data;

import com.thesquadmc.discordsync.common.DiscordData;
import com.thesquadmc.discordsync.plugin.DiscordSync;
import com.thesquadmc.discordsync.plugin.util.Message;
import net.dv8tion.jda.core.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PeriodicReminder extends BukkitRunnable
{
    private final DiscordSync plugin;

    PeriodicReminder(DiscordSync plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        Bukkit.getOnlinePlayers().forEach(this::handlePlayer);
    }

    private void handlePlayer(Player player)
    {
        if (plugin.getVaultManager().isExcluded(player))
        {
            return;
        }

        if (!plugin.getDataManager().isSynced(player))
        {
            Message.NOT_SYNCED.send(player);
            return;
        }

        String primaryGroup = plugin.getVaultManager().getPrimaryGroup(player);
        DiscordData discordData = plugin.getDataManager().getDiscordData(player);

        if (!discordData.getLastRank().equalsIgnoreCase(primaryGroup))
        {
            // They've got a new rank, we can auto-sync
            Role oldRole = plugin.getDiscordManager().getRole(discordData.getLastRoleId());

            plugin.getDiscordManager().updateRole(player, discordData, oldRole);
        }
    }
}
