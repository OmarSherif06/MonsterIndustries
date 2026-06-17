package org.omar.monsterIndustries.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class WardenSpawnListener implements Listener {
    @EventHandler
    void WardenSpawnListener(CreatureSpawnEvent event) {
        if (event.getEntity().getType() != EntityType.WARDEN) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;

        LivingEntity oldWarden = (LivingEntity) event.getEntity();
        Location location = oldWarden.getLocation();

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(oldWarden.getUniqueId().toString());
        String teamName = (team != null) ? team.getName() : null;

        new BukkitRunnable() {
            @Override
            public void run() {
                oldWarden.teleport(new Location(oldWarden.getWorld(), 0, 0, 0));
            }
        }.runTask(getPlugin());

        new BukkitRunnable() {
            @Override
            public void run() {
                oldWarden.setHealth(0);
            }
        }.runTaskLater(getPlugin(), 2L);

        Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "summon minecraft:warden " + location.getX() + " " + location.getY() + " " + location.getZ() +
                        " {Tags:[\"boss\"],Team:\"" + teamName + "\",Brain:{memories:{\"minecraft:dig_cooldown\":{value:{},ttl:1200L},\"minecraft:is_emerging\":{value:{},ttl:85L}}}}"
        );


    }

}
