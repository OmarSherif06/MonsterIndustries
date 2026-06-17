package org.omar.monsterIndustries.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Team;
import org.omar.monsterIndustries.Events;
import org.omar.monsterIndustries.MonsterTeam;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class DeathListener implements Listener {

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event) {
        if (Events.isShellProtection)
            return;

        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());

        int killReward = getPlugin().getConfig().getInt("kill-reward");

        if (Events.isCrunchTime)
            killReward *= 2;

        if (MonsterTeam.convertTeam(team).isEspionaged)
            killReward /= 2;

        if (team.getName().equals("EnderEnterprise")) {
            MonsterTeam monsterTeam = MonsterTeam.convertTeam(Bukkit.getScoreboardManager().getMainScoreboard().getTeam("CreeperCorp"));
            monsterTeam.setStock(monsterTeam.getStock() + killReward);
        } else {
            MonsterTeam monsterTeam = MonsterTeam.convertTeam(Bukkit.getScoreboardManager().getMainScoreboard().getTeam("EnderEnterprise"));
            monsterTeam.setStock(monsterTeam.getStock() + killReward);
        }
    }

}
