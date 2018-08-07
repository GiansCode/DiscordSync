package com.thesquadmc.discordsync.plugin.util;

import java.util.logging.Logger;

public class Logs
{
    private static final Logger logger = UtilServer.getPlugin().getLogger();

    public static void info(String msg)
    {
        logger.info(msg);
    }

    public static void error(String msg)
    {
        logger.severe(msg);
    }
}
