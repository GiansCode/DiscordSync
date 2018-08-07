package com.thesquadmc.discordsync.bot;

import com.google.gson.JsonObject;
import com.thesquadmc.discordsync.bot.util.Logs;
import com.thesquadmc.discordsync.bot.util.Message;
import com.thesquadmc.discordsync.bot.util.UtilJson;
import com.thesquadmc.discordsync.common.database.SqlCredentials;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BotConfig
{
    private final DiscordBot bot;

    BotConfig(DiscordBot bot)
    {
        this.bot = bot;
    }

    public String getBotToken()
    {
        return getString("bot_token");
    }

    public String getGuildId()
    {
        return getString("guild_id");
    }

    public String getLinkCommand()
    {
        return getString("link_command");
    }

    public int getKeyExpirationTime()
    {
        return getInt("key_expiration_time");
    }

    public SqlCredentials getSqlCredentials()
    {
        return new SqlCredentials(
            sqlObject.get("host").getAsString(),
            sqlObject.get("port").getAsInt(),
            sqlObject.get("database").getAsString(),
            sqlObject.get("username").getAsString(),
            sqlObject.get("password").getAsString()
        );
    }

    /* Loading */

    private String getString(String path)
    {
        return configObject.get(path).getAsString();
    }

    private int getInt(String path)
    {
        return configObject.get(path).getAsInt();
    }

    private JsonObject configObject;
    private JsonObject sqlObject;

    public boolean init()
    {
        return loadConfig();
    }

    private boolean loadConfig()
    {
        File file = new File("config.json");

        if (!file.exists())
        {
            copyConfigResource(file);
            Logs.info("Created config file - please enter information and re-run the program");

            return false;
        }

        configObject = UtilJson.parse(file);

        if (configObject == null)
        {
            Logs.severe("Error parsing config from file");
            return false;
        }

        Message.init(configObject);

        sqlObject = configObject.getAsJsonObject("sql");
        return true;
    }

    private void copyConfigResource(File destination)
    {
        InputStream stream = DiscordBot.class.getClassLoader().getResourceAsStream("config.json");

        try
        {
            Files.copy(stream, Paths.get(destination.getAbsolutePath()));
        }
        catch (IOException ex)
        {
            Logs.severe("Unable to copy JAR resource to disk");
        }
    }
}