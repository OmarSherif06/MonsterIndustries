package org.omar.monsterIndustries.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Team;

public class PlayerRespawnListener implements Listener {

    @EventHandler
    public void PlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team.getName().equals("EnderEnterprise"))
            event.setRespawnLocation(new Location(Bukkit.getWorlds().getFirst(), 6.5, 141, -22.5));
        else
            event.setRespawnLocation(new Location(Bukkit.getWorlds().getFirst(), 56.5, 141, 9.5));
    }

}
