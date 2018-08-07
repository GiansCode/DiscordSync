package com.thesquadmc.discordsync.bot;

import com.thesquadmc.discordsync.bot.command.CommandListener;
import com.thesquadmc.discordsync.bot.data.DataManager;
import com.thesquadmc.discordsync.bot.util.Logs;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;

public class DiscordBot
{
    public static void main(String[] args)
    {
        new DiscordBot();
    }

    private DiscordBot()
    {
        if (!loadConfig() || !connect())
        {
            Logs.severe("Could not load bot");
            return;
        }

        dataManager = new DataManager(this);
    }

    private BotConfig config;

    public BotConfig getConfig()
    {
        return config;
    }

    private DataManager dataManager;

    public DataManager getDataManager()
    {
        return dataManager;
    }

    private JDA jda;
    private Guild guild;

    /* Initialisation */
    private boolean loadConfig()
    {
        config = new BotConfig(this);

        return config.init();
    }

    private boolean connect()
    {
        try
        {
            jda = new JDABuilder(AccountType.BOT)
                .setToken(config.getBotToken())
                .addEventListener(new CommandListener(this))
                .buildBlocking();

            Logs.info("Connected to bot " + jda.getSelfUser().getName());

            guild = jda.getGuildById(config.getGuildId());

            return true;
        }
        catch (Exception ex)
        {
            Logs.severe("Error connecting to Discord:");
            ex.printStackTrace();
        }

        return false;
    }
}
