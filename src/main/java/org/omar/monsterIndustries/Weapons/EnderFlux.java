package org.omar.monsterIndustries.Weapons;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class EnderFlux implements Listener {

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getHand() == null || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());

        if (player.getInventory().getItemInMainHand().getType() != Material.HEART_OF_THE_SEA) return;
        event.setCancelled(true);

        if (player.getCooldown(Material.HEART_OF_THE_SEA) > 0) return;
        player.setCooldown(Material.HEART_OF_THE_SEA, 80);

        // Consume item
        ItemStack item = player.getInventory().getItemInMainHand();
        item.setAmount(item.getAmount() - 1);
        player.getInventory().setItemInMainHand(item);

        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 1, true, false, false));
        player.setInvulnerable(true);

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.4f);

        ArrayList<LivingEntity> entities = new ArrayList<>();

        new BukkitRunnable() {
            int ticks = 0;
            double radius = 1;
            final Location location = player.getLocation().clone();

            @Override
            public void run() {

                if (ticks % 10 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, 1f, 1.2f);
                }

                for (Entity entity : location.getNearbyEntities(radius, radius, radius)) {
                    if (!(entity instanceof LivingEntity livingEntity)) continue;

                    if (entities.contains(livingEntity)) continue;
                    if (!livingEntity.getScoreboardTags().contains("mob")) continue;

                    entities.add(livingEntity);

                    livingEntity.setAI(false);

                    if (livingEntity.getAttribute(Attribute.GRAVITY) != null)
                        livingEntity.getAttribute(Attribute.GRAVITY).setBaseValue(6);

                    if (livingEntity.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER) != null)
                        livingEntity.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER).setBaseValue(100);
                }

                drawCircle(Particle.END_ROD, radius, location.getWorld(), location);

                if (ticks == 0) {
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "summon area_effect_cloud " +
                                    location.getX() + " " + location.getY() + " " + location.getZ() +
                                    " {custom_particle:{type:'trial_spawner_detection_ominous'},Radius:1f,RadiusPerTick:0.2f,Duration:60}"
                    );
                }

                ticks++;
                radius = Math.min(radius + 0.2, 24);

                if (ticks >= 60) {

                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1f, 1.4f);

                    cancel();
                }
            }
        }.runTaskTimer(getPlugin(), 0L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {

                new BukkitRunnable() {
                    int ticks = 0;

                    @Override
                    public void run() {

                        player.setVelocity(new Vector(0, 0, 0));

                        if (ticks == 0) {

                            Bukkit.dispatchCommand(
                                    Bukkit.getConsoleSender(),
                                    "summon area_effect_cloud " +
                                            player.getX() + " " + (player.getY() - 4) + " " + player.getZ() +
                                            " {custom_particle:{type:'trial_spawner_detection_ominous'},Radius:12f,Duration:60}"
                            );

                            for (LivingEntity entity : entities) {
                                entity.setAI(true);
                                entity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 1, true, false, false));
                            }
                        }

                        if (ticks % 5 == 0) {
                            player.getWorld().playSound(player.getLocation(),
                                    Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.8f);
                        }

                        ticks++;

                        if (ticks >= 60) {

                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, 1.2f);

                            player.setInvulnerable(false);
                            cancel();
                        }
                    }
                }.runTaskTimer(getPlugin(), 0L, 1L);
            }
        }.runTaskLater(getPlugin(), 60L);
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