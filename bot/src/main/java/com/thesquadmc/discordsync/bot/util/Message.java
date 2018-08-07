package com.thesquadmc.discordsync.bot.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.thesquadmc.discordsync.common.util.UtilString;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public enum Message
{
    ALREADY_SYNCED,
    LINK_COMMAND_USAGE,
    KEY_GENERATED,
    USER_AWAITING_TOKEN,
    TOKEN_ALREADY_GENERATED,
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

    public void send(TextChannel channel, Member member, Object... params)
    {
        if (!value().isEmpty())
        {
            channel.sendMessage(UtilString.formatString(value().replace("%tag%", member.getAsMention()), params)).queue();
        }
    }

    public static void init(JsonObject jsonData)
    {
        JsonObject messages = jsonData.getAsJsonObject("messages");

        for (Message message : values())
        {
            JsonElement element = messages.get(message.name().toLowerCase());

            if (element == null)
            {
                Logs.severe("Value missing for message " + message.name());
                continue;
            }

            String value =
                element instanceof JsonPrimitive ? element.getAsJsonPrimitive().getAsString() :
                    element instanceof JsonArray ? joinJsonArray(element.getAsJsonArray()) :
                        null;

            if (value == null)
            {
                Logs.severe("Invalid data type for message " + message.name());
                continue;
            }

            message.setValue(value);
        }
    }

    private static String joinJsonArray(JsonArray array)
    {
        StringBuilder value = new StringBuilder();
        array.forEach(element -> value.append(element.getAsString()).append("\n"));

        return value.toString();
    }
}
