package com.thesquadmc.discordsync.common;

import net.dv8tion.jda.core.entities.Member;

import java.time.Duration;
import java.util.Date;

public class KeyData
{
    private String key;
    private Date expiry;
    private Member member;

    public KeyData(String key, Duration duration, Member member)
    {
        this(key, new Date(System.currentTimeMillis() + duration.toMillis()), member);
    }

    public KeyData(String key, Date expiry, Member member)
    {
        this.key = key;
        this.expiry = expiry;
        this.member = member;
    }

    public String getKey()
    {
        return key;
    }

    public boolean isActive()
    {
        return expiry.after(new Date());
    }

    public Date getExpiry()
    {
        return expiry;
    }

    public Member getMember()
    {
        return member;
    }
}
