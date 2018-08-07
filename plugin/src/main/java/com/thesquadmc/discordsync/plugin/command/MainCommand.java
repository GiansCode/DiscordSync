package com.thesquadmc.discordsync.plugin.command;

import com.thesquadmc.discordsync.common.KeyData;
import com.thesquadmc.discordsync.plugin.DiscordSync;
import com.thesquadmc.discordsync.plugin.util.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor
{
    private final DiscordSync plugin;

    public MainCommand(DiscordSync plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            Message.PLAYER_ONLY.send(sender);
            return true;
        }

        handleCommand((Player) sender, args);
        return true;
    }

    private void handleCommand(Player player, String[] args)
    {
        if (plugin.getDataManager().isSynced(player))
        {
            Message.ALREADY_SYNCED.send(player);
            return;
        }

        if (plugin.getVaultManager().isExcluded(player))
        {
            Message.RANK_EXCLUDED.send(player);
            return;
        }

        if (args.length != 1)
        {
            Message.LINK_COMMAND_USAGE.send(player);
            return;
        }

        plugin.getDataManager().getAwaitingKeyData(player)
            .thenAccept(keyData -> handleAwaiting(player, keyData, args[0]));
    }

    private void handleAwaiting(Player player, KeyData keyData, String inputKey)
    {
        if (keyData == null)
        {
            Message.NO_AWAITING_KEY.send(player);
            return;
        }

        if (keyData.getKey().equalsIgnoreCase(inputKey))
        {
            if (!keyData.isActive())
            {
                Message.KEY_EXPIRED.send(player);
                plugin.getDataManager().deleteKey(player);

                return;
            }

            plugin.getDiscordManager().syncRole(player, keyData.getMember());
        }
        else
        {
            Message.KEY_INVALID.send(player);
        }
    }
}
