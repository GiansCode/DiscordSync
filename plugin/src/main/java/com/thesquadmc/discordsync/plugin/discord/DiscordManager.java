package com.thesquadmc.discordsync.plugin.discord;

import com.thesquadmc.discordsync.common.DiscordData;
import com.thesquadmc.discordsync.common.util.UtilString;
import com.thesquadmc.discordsync.plugin.DiscordSync;
import com.thesquadmc.discordsync.plugin.util.Logs;
import com.thesquadmc.discordsync.plugin.util.Message;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class DiscordManager
{
    private final DiscordSync plugin;

    public DiscordManager(DiscordSync plugin)
    {
        this.plugin = plugin;

        try
        {
            jda = new JDABuilder(AccountType.BOT)
                .setToken(plugin.getConfig("bot_token"))
                .buildBlocking();

            guild = jda.getGuildById(plugin.getConfig("guild_id"));
            commands = plugin.getConfig("commands", new ArrayList<>());
        }
        catch (LoginException | InterruptedException ex)
        {
            Logs.error("Could not connect to discord bot");
            ex.printStackTrace();
        }
    }

    public void syncRole(Player player, Member member)
    {
        Role role = findRoleForPlayer(player);

        if (role == null)
        {
            Message.RANK_NO_ROLE.send(player);
            return;
        }

        Message.ROLE_SYNCED.send(player);

        commands.forEach(command ->
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                UtilString.formatString(command, "player", player.getName())
            )
        );

        plugin.getDataManager().deleteKey(player);

        String primaryGroup = plugin.getVaultManager().getPrimaryGroup(player);
        DiscordData discordData = new DiscordData(member.getUser().getId(), primaryGroup, role.getId());

        plugin.getDataManager().insertPlayer(player, discordData);
        plugin.getDataManager().insertDiscordData(player, discordData);

        updateRole(member, role);
    }

    public void updateRole(Player player, DiscordData discordData, Role oldRole)
    {
        Member member = getMember(discordData.getDiscordId());

        Role newRole = findRoleForPlayer(player);

        if (newRole == null)
        {
            return;
        }

        String primaryGroup = plugin.getVaultManager().getPrimaryGroup(player);
        discordData.setLastRank(primaryGroup);
        discordData.setLastRoleId(newRole.getId());

        plugin.getDataManager().updateDiscordData(player, discordData);

        updateRoles(member, oldRole, newRole);

        Message.ROLE_SYNCED_AUTO.send(player);
    }

    private Role findRoleForPlayer(Player player)
    {
        String primaryGroup = plugin.getVaultManager().getPrimaryGroup(player);
        String roleId = plugin.getConfig("ranks." + primaryGroup);

        if (roleId == null)
        {
            Logs.error("No role associated with rank " + primaryGroup);
            return null;
        }

        Role role = getRole(roleId);

        if (role == null)
        {
            Logs.error("Role not found with ID in config (" + roleId + ")");
            return null;
        }

        return role;
    }

    private void updateRole(Member member, Role newRole)
    {
        guild.getController().addRolesToMember(member, newRole).queue();
    }

    private void updateRoles(Member member, Role oldRole, Role newRole)
    {
        guild.getController().modifyMemberRoles(member, singletonList(newRole), singletonList(oldRole)).queue();
    }

    private List<String> commands;

    private JDA jda;
    private Guild guild;

    public Member getMember(String id)
    {
        return guild.getMemberById(id);
    }

    public Role getRole(String id)
    {
        return guild.getRoleById(id);
    }
}
