package org.omar.monsterIndustries.Weapons;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.UUID;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class Arrows implements Listener {

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getHand() == null || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        Block clicked = event.getClickedBlock();

        if (player.getInventory().getItemInMainHand().getType() != Material.SPECTRAL_ARROW) return;
        if (clicked == null || clicked.getType() == Material.AIR) return;

        event.setCancelled(true);

        if (player.getCooldown(Material.SPECTRAL_ARROW) > 0) return;

        // Consume arrow
        ItemStack arrowItem = player.getInventory().getItemInMainHand();
        arrowItem.setAmount(arrowItem.getAmount() - 1);
        player.getInventory().setItemInMainHand(arrowItem);

        Location target = clicked.getLocation().add(0.5, 0, 0.5);
        World world = target.getWorld();

        // Notify team
        for (Player nearby : world.getPlayers()) {
            if (nearby.getLocation().distanceSquared(player.getLocation()) <= 50 * 50) {
                Team nearbyTeam = nearby.getScoreboard().getEntryTeam(nearby.getName());
                if (team != null && team.equals(nearbyTeam)) {
                    nearby.sendActionBar(getPlugin().prefix + player.getName() + " used " + ChatColor.BOLD + ChatColor.YELLOW + "Arrows!");
                }
            }
        }

        double radius = 3.5;

        // Charging effect
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                drawCircle(Particle.CRIT, radius, world, target);
                world.playSound(target, Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 2f);
                ticks++;

                if (ticks >= 10) {
                    // Arrow rain phase
                    rainArrows(player, world, target, radius);
                    cancel();
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 2L);

        player.setCooldown(Material.SPECTRAL_ARROW, 80);
    }

    private void rainArrows(Player caster, World world, Location center, double radius) {
        String arrowTag = "arrowSpell_" + UUID.randomUUID().toString().substring(0, 8);
        world.playSound(center, Sound.ITEM_CROSSBOW_SHOOT, 1f, 0.7f);

        new BukkitRunnable() {
            int waves = 0;

            @Override
            public void run() {
                for (LivingEntity target : world.getLivingEntities()) {
                    if (target.getLocation().distanceSquared(center) <= radius * radius) {
                        if (target instanceof Player || target.getType() == EntityType.VILLAGER)
                            continue; // skip players and villagers

                        Location spawnLoc = target.getLocation().clone().add(0, 10, 0);
                        Arrow arrow = world.spawnArrow(spawnLoc, new Vector(0, -1, 0), 1.3f, 5);
                        arrow.setShooter(caster);
                        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                        arrow.setCritical(true);
                        arrow.addScoreboardTag(arrowTag);

                        // Visual trail
                        world.spawnParticle(Particle.CRIT, spawnLoc, 5, 0.1, 0.1, 0.1, 0.01);
                    }
                }

                for (int i = 0; i < 15; i++) { // number of fake arrows per wave
                    double angle = Math.random() * Math.PI * 2;
                    double dist = Math.random() * radius * 1.2; // allow slightly beyond radius
                    double x = Math.cos(angle) * dist;
                    double z = Math.sin(angle) * dist;
                    double yOffset = 10 + Math.random() * 3; // slight height variation

                    Location randomLoc = center.clone().add(x, yOffset, z);
                    Arrow visualArrow = world.spawnArrow(randomLoc, new Vector(0, -1, 0), 1.2f, 8);
                    visualArrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                    visualArrow.setCritical(true);
                    visualArrow.addScoreboardTag(arrowTag); // still tagged for later cleanup
                    visualArrow.setGravity(true);

                    // Small visual spark
                    world.spawnParticle(Particle.CRIT, randomLoc, 4, 0.15, 0.15, 0.15, 0.02);
                }

                // Sound each wave
                world.playSound(center, Sound.ENTITY_ARROW_SHOOT, 0.6f, 1.4f);
                waves++;

                if (waves >= 3) {
                    cancel();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (Arrow arrow : world.getEntitiesByClass(Arrow.class)) {
                                if (arrow.getScoreboardTags().contains(arrowTag))
                                    arrow.remove();
                            }
                        }
                    }.runTaskLater(getPlugin(), 40L);
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 10L);
    }

    private void drawCircle(Particle particle, double radius, World world, Location loc) {
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location particleLoc = loc.clone().add(x, 1, z);
            world.spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
        }
    }
}
