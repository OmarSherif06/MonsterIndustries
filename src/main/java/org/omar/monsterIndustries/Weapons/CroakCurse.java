package org.omar.monsterIndustries.Weapons;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class CroakCurse implements Listener {

    @EventHandler
    public void PotionSplashEvent(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        ItemMeta meta =  potion.getItem().getItemMeta();

        if (!(meta.hasDisplayName() && meta.getDisplayName().contains("Croak Curse"))) return;

        List<Location> mobLocations = new ArrayList<>();
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity.getScoreboardTags().contains("mob") && !entity.getScoreboardTags().contains("boss")) {
                mobLocations.add(entity.getLocation());
                entity.teleport(new Location(potion.getWorld(), 0, 0, 0));
                entity.setHealth(0);
            }
        }

        final List<LivingEntity> frogs = new ArrayList<>();
        for (Location location : mobLocations) {
            LivingEntity frog = (LivingEntity) potion.getWorld().spawnEntity(location, EntityType.FROG);
            frogs.add(frog);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (LivingEntity frog : frogs) {
                    frog.setHealth(0);
                }
            }
        }.runTaskLater(getPlugin(), 100L);

        potion.getWorld().playSound(potion.getLocation(), Sound.ENTITY_FROG_DEATH, 5, 1);


    }

}
