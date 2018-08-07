package com.thesquadmc.discordsync.bot.util;

import java.util.Date;

public final class Logs
{
    private Logs() {}

    public static void info(String msg)
    {
        System.out.println(new Date() + " - " + msg);
    }

    public static void severe(String msg)
    {
        System.err.println(new Date() + " - " + msg);
    }
}
