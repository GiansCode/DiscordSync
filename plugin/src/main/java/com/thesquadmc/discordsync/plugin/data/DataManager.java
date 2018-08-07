package com.thesquadmc.discordsync.plugin.data;

import com.thesquadmc.discordsync.common.DiscordData;
import com.thesquadmc.discordsync.common.KeyData;
import com.thesquadmc.discordsync.common.database.DatabaseConnection;
import com.thesquadmc.discordsync.common.database.SqlCredentials;
import com.thesquadmc.discordsync.plugin.util.Logs;
import com.thesquadmc.discordsync.plugin.DiscordSync;
import com.thesquadmc.discordsync.plugin.util.UtilServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DataManager implements Listener
{
    private final DiscordSync plugin;

    public DataManager(DiscordSync plugin)
    {
        this.plugin = plugin;

        UtilServer.registerListener(this);

        connectSql();

        new PeriodicReminder(plugin).runTaskTimer(plugin, 0L, (int) plugin.getConfig("check_interval") * 20L * 60L);
    }

    public void deleteKey(Player player)
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> sqlConnection.deleteKey(player.getName()));
    }

    private final Map<UUID, DiscordData> playerDiscordData = new HashMap<>();

    public void insertPlayer(Player player, DiscordData discordData)
    {
        playerDiscordData.put(player.getUniqueId(), discordData);
    }

    private final String insertDiscordDataStatement =
        "INSERT INTO discordsync_users VALUES (?,?,?,?);"
    ;

    public void insertDiscordData(Player player, DiscordData discordData)
    {
        sqlConnection.prepareStatement(insertDiscordDataStatement, statement ->
        {
             statement.setString(1, player.getUniqueId().toString());
             statement.setString(2, discordData.getDiscordId());
             statement.setString(3, discordData.getLastRank());
             statement.setString(4, discordData.getLastRoleId());

             statement.execute();
        });
    }

    private final String updateDiscordDataStatement =
        "UPDATE discordsync_users SET last_rank=?,last_role=? WHERE uuid=?;"
    ;

    public void updateDiscordData(Player player, DiscordData discordData)
    {
        sqlConnection.prepareStatement(updateDiscordDataStatement, statement ->
        {
            statement.setString(1, discordData.getLastRank());
            statement.setString(2, discordData.getLastRoleId());
            statement.setString(3, player.getUniqueId().toString());

            statement.execute();
        });
    }

    public boolean isSynced(Player player)
    {
        return playerDiscordData.containsKey(player.getUniqueId());
    }

    public DiscordData getDiscordData(Player player)
    {
        return playerDiscordData.get(player.getUniqueId());
    }

    private final String getAwaitingKeyDataStatement =
        "SELECT * FROM discordsync_tokens WHERE user_name=?;"
    ;

    public CompletableFuture<KeyData> getAwaitingKeyData(Player player)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try (Connection connection = sqlConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(getAwaitingKeyDataStatement))
            {
                statement.setString(1, player.getName().toLowerCase());

                ResultSet results = statement.executeQuery();

                if (!results.first())
                {
                    return null;
                }

                return parseKeyData(results);
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }

            return null;
        });
    }

    private KeyData parseKeyData(ResultSet results) throws SQLException
    {
        return new KeyData(
            results.getString("token"),
            new Date(results.getLong("expiry")),
            plugin.getDiscordManager().getMember(results.getString("discord_id"))
        );
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event)
    {
        UUID uuid = event.getUniqueId();

        sqlConnection.executeQueryAsync("SELECT * FROM discordsync_users WHERE uuid=?;", statement ->
            statement.setString(1, uuid.toString())
        ).thenAccept(results ->
        {
            try
            {
                if (!results.first())
                {
                    return;
                }

                playerDiscordData.put(uuid, parseDiscordData(results));
            }
            catch (SQLException ex)
            {
                Logs.error("Error fetching player data");
                ex.printStackTrace();
            }
        });
    }

    private DiscordData parseDiscordData(ResultSet results) throws SQLException
    {
        return new DiscordData(
            results.getString("user_id"),
            results.getString("last_rank"),
            results.getString("last_role")
        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        playerDiscordData.remove(event.getPlayer().getUniqueId());
    }

    private DatabaseConnection sqlConnection;

    private void connectSql()
    {
        String host = plugin.getConfig("sql.host");
        int port = plugin.getConfig("sql.port");
        String database = plugin.getConfig("sql.database");
        String username = plugin.getConfig("sql.username");
        String password = plugin.getConfig("sql.password");

        SqlCredentials credentials = new SqlCredentials(host, port, database, username, password);

        sqlConnection = new DatabaseConnection(credentials);
    }
}
