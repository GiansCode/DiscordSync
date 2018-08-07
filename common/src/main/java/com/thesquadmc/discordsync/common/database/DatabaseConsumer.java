package com.thesquadmc.discordsync.common.database;

import java.sql.SQLException;

@FunctionalInterface
public interface DatabaseConsumer<T>
{
    void accept(T data) throws SQLException;
}
