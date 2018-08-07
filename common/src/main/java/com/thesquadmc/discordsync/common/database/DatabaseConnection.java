package com.thesquadmc.discordsync.common.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class DatabaseConnection
{
    public DatabaseConnection(SqlCredentials credentials)
    {
        connect(credentials);
    }

    private void connect(SqlCredentials credentials)
    {
        HikariConfig config = new HikariConfig();
        config.setPoolName("discordsync");

        config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        config.setJdbcUrl(String.format("jdbc:mysql//%s:%s/%s", credentials.host, credentials.port, credentials.database));

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useUnicode","true");
        config.addDataSourceProperty("characterEncoding","utf8");

        config.addDataSourceProperty("serverName", credentials.host);
        config.addDataSourceProperty("port", credentials.port);
        config.addDataSourceProperty("databaseName", credentials.database);

        config.setMaximumPoolSize(Runtime.getRuntime().availableProcessors() + 1);
        config.setMinimumIdle(config.getMaximumPoolSize());
        config.setMaxLifetime(1800000L);
        config.setConnectionTimeout(5000L);

        config.setUsername(credentials.username);
        config.setPassword(credentials.password);

        config.setConnectionTimeout(30_000L);

        connectionPool = new HikariDataSource(config);

        createTable();
    }

    private void createTable()
    {
        executeStatement(
            "CREATE TABLE IF NOT EXISTS discordsync_users (" +
                "`uuid` VARCHAR(36) NOT NULL, " +
                "`user_id` VARCHAR(255) NOT NULL, " +
                "`last_rank` TINYTEXT NOT NULL, " +
                "`last_role` TINYTEXT NOT NULL, " +
                "PRIMARY KEY (`uuid`), " +
                "UNIQUE INDEX `user_id` (`user_id` ASC)" +
            ");"
        );

        executeStatement(
            "CREATE TABLE IF NOT EXISTS discordsync_tokens (" +
                "`user_name` VARCHAR(16) NOT NULL, " +
                "`discord_id` VARCHAR(255) NOT NULL, " +
                "`token` TINYTEXT NOT NULL, " +
                "`expiry` BIGINT NOT NULL, " +
                "PRIMARY KEY (`user_name`), " +
                "UNIQUE INDEX `user_id` (`discord_id` ASC)" +
            ");"
        );
    }

    private HikariDataSource connectionPool;

    public Connection getConnection() throws SQLException
    {
        if (connectionPool == null || connectionPool.isClosed())
        {
            throw new SQLException("Connection is not open");
        }

        return connectionPool.getConnection();
    }

    public void deleteKey(String username)
    {
        prepareStatement("DELETE FROM discordsync_tokens WHERE user_name=?;", statement ->
        {
            statement.setString(1, username.toLowerCase());

            statement.execute();
        });
    }

    public CompletableFuture<ResultSet> executeQueryAsync(String statement, DatabaseConsumer<PreparedStatement> consumer)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement))
            {
                consumer.accept(preparedStatement);

                return preparedStatement.executeQuery();
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }

            return null;
        });
    }

    public CompletableFuture<Boolean> executeExistsQueryAsync(String statement, DatabaseConsumer<PreparedStatement> consumer)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement))
            {
                consumer.accept(preparedStatement);

                return preparedStatement.executeQuery().next();
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }

            return null;
        });
    }

    public void executeStatement(String statement)
    {
        prepareStatement(statement, PreparedStatement::execute);
    }

    public void prepareStatement(String statement, DatabaseConsumer<PreparedStatement> consumer)
    {
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement))
        {
            consumer.accept(preparedStatement);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
}
