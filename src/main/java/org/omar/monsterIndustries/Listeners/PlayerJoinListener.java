package org.omar.monsterIndustries.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;
import org.omar.monsterIndustries.GameManager;
import org.omar.monsterIndustries.MonsterTeam;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Team team = MonsterTeam.convertTeam(MonsterTeam.getTeam(player));

        if (GameManager.running && team == null) {
            player.teleport(new Location(Bukkit.getWorlds().getFirst(), 31, 149, -41));
            GameManager.resetPlayer(player);
        }
        else if (!GameManager.running) {
            player.teleport(new Location(Bukkit.getWorlds().getFirst(), 85, 140, 96));
            GameManager.resetPlayer(player);
        }
    }

}
