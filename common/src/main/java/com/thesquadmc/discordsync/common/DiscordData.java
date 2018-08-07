package com.thesquadmc.discordsync.common;

public class DiscordData
{
    private String discordId;
    private String lastRank;
    private String lastRoleId;

    public DiscordData(String discordId, String lastRank, String lastRoleId)
    {
        this.discordId = discordId;
        this.lastRank = lastRank;
        this.lastRoleId = lastRoleId;
    }

    public String getDiscordId()
    {
        return discordId;
    }

    public void setLastRank(String lastRank)
    {
        this.lastRank = lastRank;
    }

    public String getLastRank()
    {
        return lastRank;
    }

    public void setLastRoleId(String lastRoleId)
    {
        this.lastRoleId = lastRoleId;
    }

    public String getLastRoleId()
    {
        return lastRoleId;
    }
}
