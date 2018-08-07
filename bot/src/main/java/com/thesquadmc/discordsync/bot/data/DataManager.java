package com.thesquadmc.discordsync.bot.data;

import com.thesquadmc.discordsync.bot.DiscordBot;
import com.thesquadmc.discordsync.common.KeyData;
import com.thesquadmc.discordsync.common.database.DatabaseConnection;
import net.dv8tion.jda.core.entities.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class DataManager
{
    private final DiscordBot bot;

    public DataManager(DiscordBot bot)
    {
        this.bot = bot;

        connectSql();
    }

    public CompletableFuture<Boolean> isSynced(Member member)
    {
        return sqlConnection.executeExistsQueryAsync("SELECT * FROM discordsync_users WHERE user_id=?;", statement ->
            statement.setString(1, member.getUser().getId())
        );
    }

    private final String hasGeneratedTokenStatement =
        "SELECT * FROM discordsync_tokens WHERE discord_id=?;"
    ;

    public boolean hasGeneratedToken(Member member)
    {
        try (Connection connection = sqlConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(hasGeneratedTokenStatement))
        {
            statement.setString(1, member.getUser().getId());

            ResultSet results = statement.executeQuery();

            if (!results.first())
            {
                return false;
            }

            return isKeyActive(results);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    private final String hasWaitingTokenStatement =
        "SELECT * FROM discordsync_tokens WHERE user_name=?;"
    ;

    public boolean isAwaitingToken(String username)
    {
        try (Connection connection = sqlConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(hasWaitingTokenStatement))
        {
            statement.setString(1, username.toLowerCase());

            ResultSet results = statement.executeQuery();

            if (!results.first())
            {
                return false;
            }

            return isKeyActive(results);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    private boolean isKeyActive(ResultSet results) throws SQLException
    {
        Date expiry = new Date(results.getLong("expiry"));

        if (expiry.after(new Date()))
        {
            return true;
        }
        else
        {
            sqlConnection.deleteKey(results.getString("user_name"));
            return false;
        }
    }

    public void insertKey(String username, KeyData keyData)
    {
        sqlConnection.prepareStatement("INSERT INTO discordsync_tokens VALUES (?,?,?,?);", statement ->
        {
            statement.setString(1, username.toLowerCase());
            statement.setString(2, keyData.getMember().getUser().getId());
            statement.setString(3, keyData.getKey());
            statement.setLong(4, keyData.getExpiry().getTime());

            statement.execute();
        });
    }

    private DatabaseConnection sqlConnection;

    private void connectSql()
    {
        sqlConnection = new DatabaseConnection(bot.getConfig().getSqlCredentials());
    }
}
