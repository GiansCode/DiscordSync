package com.thesquadmc.discordsync.bot.command;

import com.thesquadmc.discordsync.bot.DiscordBot;
import com.thesquadmc.discordsync.bot.util.Message;
import com.thesquadmc.discordsync.common.KeyData;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandListener extends ListenerAdapter
{
    private final DiscordBot bot;

    public CommandListener(DiscordBot bot)
    {
        this.bot = bot;

        this.linkCommand = bot.getConfig().getLinkCommand();
        this.keyDuration = Duration.ofSeconds(bot.getConfig().getKeyExpirationTime());
    }

    private final String linkCommand;
    private final Duration keyDuration;

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        User user = event.getAuthor();

        if (user.isBot() || event.getChannelType() != ChannelType.TEXT)
        {
            return;
        }

        Member member = event.getMember();
        TextChannel channel = event.getTextChannel();

        List<String> args = new ArrayList<>(Arrays.asList(event.getMessage().getContentRaw().split(" ")));
        String command = args.remove(0).toLowerCase();

        if (!command.equalsIgnoreCase(linkCommand))
        {
            return;
        }

        if (args.size() != 1)
        {
            Message.LINK_COMMAND_USAGE.send(channel, member);
            return;
        }

        bot.getDataManager().isSynced(member)
            .thenAccept(synced ->
            {
                if (synced)
                {
                    Message.ALREADY_SYNCED.send(channel, member);
                    return;
                }

                if (bot.getDataManager().hasGeneratedToken(member))
                {
                    Message.TOKEN_ALREADY_GENERATED.send(channel, member);
                    return;
                }

                String username = args.get(0);

                if (bot.getDataManager().isAwaitingToken(username))
                {
                    Message.USER_AWAITING_TOKEN.send(channel, member);
                    return;
                }

                String key = String.valueOf(System.currentTimeMillis());
                Message.KEY_GENERATED.send(channel, member, "key", key);

                KeyData keyData = new KeyData(key, keyDuration, member);
                bot.getDataManager().insertKey(username, keyData);
            });
    }
}
