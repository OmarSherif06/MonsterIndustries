package org.omar.monsterIndustries.Weapons;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class IrfritsDevastation implements Listener {

    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof SmallFireball fireball)) return;

        if (!(fireball.getShooter() instanceof Player player)) return;

        if (event.getHitEntity() instanceof LivingEntity target) {
            target.damage(20, player);
        }
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getHand() == null || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() != Material.NAUTILUS_SHELL)
            return;

        event.setCancelled(true);

        if (player.getCooldown(Material.NAUTILUS_SHELL) > 0) return;
        player.setCooldown(Material.NAUTILUS_SHELL, 80);

        // Consume item
        ItemStack item = player.getInventory().getItemInMainHand();
        item.setAmount(item.getAmount() - 1);
        player.getInventory().setItemInMainHand(item);

        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 1, true, false, false));
        player.setInvulnerable(true);

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.4f);

        new BukkitRunnable() {
            int ticks = 0;
            double radius = 1;
            final Location location = player.getLocation().clone();

            @Override
            public void run() {

                if (ticks % 10 == 0) {
                    player.getWorld().playSound(player.getLocation(),
                            Sound.BLOCK_CONDUIT_AMBIENT, 1f, 1.2f);
                }

                if (ticks == 0) {
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "summon area_effect_cloud " +
                                    location.getX() + " " + location.getY() + " " + location.getZ() +
                                    " {custom_particle:{type:'trial_spawner_detection'},Radius:1f,RadiusPerTick:0.2f,Duration:60}"
                    );
                }

                ticks++;
                radius = Math.min(radius + 0.2, 24);

                if (ticks >= 60) {

                    player.playSound(player.getLocation(),
                            Sound.BLOCK_BEACON_AMBIENT, 1f, 1.4f);

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

                        SmallFireball fireball = player.launchProjectile(SmallFireball.class);

                        fireball.setIsIncendiary(false);

                        Vector direction = player.getLocation().getDirection().normalize();
                        fireball.setVelocity(direction.multiply(1.5));

                        player.getWorld().playSound(
                                player.getLocation(),
                                Sound.ITEM_FIRECHARGE_USE,
                                0.1f,
                                1.2f
                        );

                        if (ticks % 40 == 0) {
                            player.getWorld().playSound(
                                    player.getLocation(),
                                    Sound.BLOCK_CONDUIT_AMBIENT,
                                    0.5f,
                                    1.3f
                            );
                        }

                        ticks++;

                        if (ticks >= 200) {
                            player.setInvulnerable(false);

                            player.getWorld().playSound(
                                    player.getLocation(),
                                    Sound.ENTITY_WARDEN_SONIC_BOOM,
                                    1f,
                                    1.2f
                            );

                            cancel();
                        }
                    }
                }.runTaskTimer(getPlugin(), 0L, 1L);

            }
        }.runTaskLater(getPlugin(), 60L);
    }
}