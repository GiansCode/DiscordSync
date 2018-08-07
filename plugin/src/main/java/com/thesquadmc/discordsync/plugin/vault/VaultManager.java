package com.thesquadmc.discordsync.plugin.vault;

import com.thesquadmc.discordsync.plugin.DiscordSync;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;

public class VaultManager
{
    public VaultManager(DiscordSync plugin)
    {
        setupPermissions();

        excludedGroups = plugin.getConfig("excluded_groups");
    }

    private Permission permissions;

    private void setupPermissions()
    {
        RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);

        if (provider != null)
        {
            permissions = provider.getProvider();
        }
        else
        {
            throw new AssertionError("No permissions plugin found");
        }
    }

    private final List<String> excludedGroups;

    public String getPrimaryGroup(Player player)
    {
        return permissions.getPrimaryGroup(player).toLowerCase();
    }

    public boolean isExcluded(Player player)
    {
        return excludedGroups.contains(getPrimaryGroup(player));
    }
}
