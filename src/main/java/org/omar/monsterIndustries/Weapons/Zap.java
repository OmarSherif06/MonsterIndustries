package org.omar.monsterIndustries.Weapons;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class Zap implements Listener {

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getHand() == null || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        Block clicked = event.getClickedBlock();

        if (player.getInventory().getItemInMainHand().getType() != Material.LIGHTNING_ROD) return;
        if (clicked == null || clicked.getType() == Material.AIR) return;

        event.setCancelled(true);

        if (player.getCooldown(Material.LIGHTNING_ROD) > 0) return;

        ItemStack rod = player.getInventory().getItemInMainHand();
        rod.setAmount(rod.getAmount() - 1);
        player.getInventory().setItemInMainHand(rod);

        Location strikeLoc = clicked.getLocation().add(1, 0, 1);
        World world = strikeLoc.getWorld();

        // Notify nearby players on the same team
        for (Player nearby : world.getPlayers()) {
            if (nearby.getLocation().distanceSquared(player.getLocation()) <= 50 * 50) {
                Team nearbyTeam = nearby.getScoreboard().getEntryTeam(nearby.getName());
                if (team.equals(nearbyTeam))
                    nearby.sendActionBar(getPlugin().prefix + player.getName() + " used " + ChatColor.BOLD + ChatColor.BLUE + "Zap!");
            }
        }

        double radius = 4;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                // draw charging circle
                drawCircle(Particle.ELECTRIC_SPARK, radius, world, strikeLoc);

                world.spawnParticle(Particle.CRIT, strikeLoc.clone().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.05);
                world.playSound(strikeLoc, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 0.3f, 2f);
//                world.playSound(strikeLoc, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1f, 2f);

                ticks++;

                if (ticks == 3) {
                    new BukkitRunnable() {
                        int ticks2 = 0;

                        @Override
                        public void run() {
                            if (ticks2 > 5) return;

                            world.spawnParticle(Particle.WAX_OFF, strikeLoc.clone().add(0, 1, 0), 200, radius / 3, 0.5, radius / 3, 0.3);
                            ticks2++;
                        }
                    }.runTaskTimer(getPlugin(), 0L, 2L);
                }

                if (ticks >= 10) {
                    // strike lightning and damage entities
                    world.strikeLightningEffect(strikeLoc);

                    for (org.bukkit.entity.Entity entity : world.getNearbyEntities(strikeLoc, radius, radius, radius)) {
                        if (entity instanceof Player) continue;
                        if (entity instanceof org.bukkit.entity.Villager) continue;

                        if (entity instanceof org.bukkit.entity.LivingEntity living) {
                            living.damage(6.0, player);
                            living.setFireTicks(60);
                            world.spawnParticle(Particle.ELECTRIC_SPARK, living.getLocation().add(0, 1.5, 0), 10, 0.2, 0.2, 0.2, 0.05);
                            world.playSound(living.getLocation(), Sound.ENTITY_GENERIC_BURN, 0.6f, 1.8f);
                        }
                    }

                    world.spawnParticle(Particle.CRIT, strikeLoc, 20, 0.3, 0.3, 0.3, 0.05);


                    player.spawnParticle(Particle.END_ROD, player.getEyeLocation(), 5, 0.1, 0.1, 0.1, 0.02);
                    world.playSound(player.getLocation(), Sound.BLOCK_VAULT_BREAK, 1f, 1f);
                    world.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.1f, 1.8f);

                    cancel();
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 2L);

        player.setCooldown(Material.LIGHTNING_ROD, 60);
    }

    public void drawCircle(Particle particle, double radius, World world, Location strikeLoc) {
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location particleLoc = strikeLoc.clone().add(x, 1, z);
            world.spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
        }
    }
}
