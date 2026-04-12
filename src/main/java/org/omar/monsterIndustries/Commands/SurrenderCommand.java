package org.omar.monsterIndustries.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.omar.monsterIndustries.MonsterIndustries;
import org.omar.monsterIndustries.MonsterTeam;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class SurrenderCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(commandSender.getName());
        MonsterTeam enemyTeam = MonsterTeam.convertTeam(Bukkit.getScoreboardManager().getMainScoreboard().getTeam((team.getName().equals("EnderEnterprises")) ? "CreeperCorp" : "EnderEnterprise"));

        Bukkit.broadcastMessage(getPlugin().prefix + team.getDisplayName() + ChatColor.GRAY + " has surrendered!");

        MonsterIndustries.getPlugin().winGame(enemyTeam);

        return false;
    }
}
