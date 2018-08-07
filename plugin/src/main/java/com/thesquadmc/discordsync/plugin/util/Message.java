package com.thesquadmc.discordsync.plugin.util;

import com.thesquadmc.discordsync.common.util.UtilString;
import com.thesquadmc.discordsync.plugin.DiscordSync;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public enum Message
{
    PLAYER_ONLY,

    LINK_COMMAND_USAGE,

    ALREADY_SYNCED,
    NOT_SYNCED,

    RANK_EXCLUDED,

    NO_AWAITING_KEY,
    KEY_INVALID,
    KEY_EXPIRED,

    ERROR_SYNCING_ROLE,
    RANK_NO_ROLE,

    ROLE_SYNCED,
    ROLE_SYNCED_AUTO
    ;

    private String msg;

    private void setValue(String msg)
    {
        assert this.msg == null : "Message is already set";

        this.msg = msg;
    }

    public String value()
    {
        return msg;
    }

    public void send(CommandSender player, Object... params)
    {
        if (!value().isEmpty())
        {
            player.sendMessage(UtilString.formatString(value(), params));
        }
    }

    @SuppressWarnings("unchecked")
    public static void init(DiscordSync plugin)
    {
        for (Message message : values())
        {
            Object object = plugin.getConfig().get("messages." + message.name().toLowerCase());

            if (object == null)
            {
                plugin.getLogger().severe("Value missing for message " + message.name());
                continue;
            }

            String value =
                object instanceof String ? (String) object :
                    object instanceof List ? String.join("\n", (List<String>) object) :
                        null;

            if (value == null)
            {
                plugin.getLogger().severe("Invalid data type for message " + message.name());
                continue;
            }

            message.setValue(ChatColor.translateAlternateColorCodes('&', value));
        }
    }
}

