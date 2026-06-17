package org.omar.monsterIndustries.Bosses.MotherSpider;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scoreboard.Team;

public class MotherSpiderDeathEvent implements Listener {

    @EventHandler
    void SpiderDeathEvent(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.CAVE_SPIDER) return;

        LivingEntity bossEntity = (LivingEntity) event.getEntity();

        if (!bossEntity.getScoreboardTags().contains("boss")) return;

        Location location = bossEntity.getLocation();

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(bossEntity.getUniqueId().toString());
        String teamName = (team != null) ? team.getName() : null;

        String command = "summon spider " + x + " " + y + " " + z + " {CustomNameVisible:1b,Team:\"" + teamName + "\",Health:20f,Tags:[\"mob\"],CustomName:[{\"color\":\"#FF0000\",\"text\":\"S\"},{\"color\":\"#DC0000\",\"text\":\"p\"},{\"color\":\"#B90000\",\"text\":\"i\"},{\"color\":\"#950000\",\"text\":\"d\"},{\"color\":\"#720000\",\"text\":\"e\"},{\"color\":\"#4F0000\",\"text\":\"r\"},{\"color\":\"#720000\",\"text\":\"l\"},{\"color\":\"#950000\",\"text\":\"i\"},{\"color\":\"#B90000\",\"text\":\"n\"},{\"color\":\"#FF0000\",\"text\":\"g\"}],attributes:[{id:\"minecraft:attack_damage\",base:15},{id:\"minecraft:follow_range\",base:70},{id:\"minecraft:max_health\",base:20},{id:\"minecraft:scale\",base:0.3},{id:\"minecraft:tempt_range\",base:30}]}";

        for (int i = 0; i < 10; i++) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

}
