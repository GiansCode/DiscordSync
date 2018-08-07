package com.thesquadmc.discordsync.common.database;

public class SqlCredentials
{
    final String host;
    final int port;
    final String database;
    final String username;
    final String password;

    public SqlCredentials(String host, int port, String database, String username, String password)
    {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }
}
