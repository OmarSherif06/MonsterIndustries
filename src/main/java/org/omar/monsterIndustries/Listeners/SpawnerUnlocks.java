package org.omar.monsterIndustries.Listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class SpawnerUnlocks implements Listener {

    @EventHandler
    public void onPlayerInteractLever(PlayerInteractEvent event) {
        if (event.getHand() == null || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND)
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.TRIAL_SPAWNER) return;

        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        Location loc = clicked.getLocation();

        BlockData existingData = clicked.getBlockData();
        TrialSpawner currentSpawner = (TrialSpawner) existingData;

        if (currentSpawner.getTrialSpawnerState() == TrialSpawner.State.INACTIVE)
            return;

        boolean isNormalSpawner;
        boolean isOminousSpawner;
        if (team.getName().equals("EnderEnterprise")) {
            isNormalSpawner = loc.getBlockX() == -6 && loc.getBlockY() == 142 && loc.getBlockZ() == 2;
            isOminousSpawner = loc.getBlockX() == -16 && loc.getBlockY() == 142 && loc.getBlockZ() == 2;
        } else {
            isNormalSpawner = loc.getBlockX() == 68 && loc.getBlockY() == 142 && loc.getBlockZ() == -17;
            isOminousSpawner = loc.getBlockX() == 78 && loc.getBlockY() == 142 && loc.getBlockZ() == -17;

        }

        if (isNormalSpawner) {
            if (player.getInventory().getItemInMainHand().getType() != Material.TRIAL_KEY) {
                player.sendMessage(getPlugin().prefix + ChatColor.RED + "You must be holding a Trial key");
                player.setVelocity(player.getLocation().getDirection().multiply(-0.6));
                return;
            }
            player.getInventory().removeItem(new ItemStack(Material.TRIAL_KEY, 1));
        } else if (isOminousSpawner) {
            if (player.getInventory().getItemInMainHand().getType() != Material.OMINOUS_TRIAL_KEY) {
                player.sendMessage(getPlugin().prefix + ChatColor.RED + "You must be holding an Ominous trial key");
                player.setVelocity(player.getLocation().getDirection().multiply(-0.6));
                return;
            }
            player.getInventory().removeItem(new ItemStack(Material.OMINOUS_TRIAL_KEY, 1));
        }

        BlockData data = Bukkit.createBlockData(Material.TRIAL_SPAWNER);
        TrialSpawner spawner = (TrialSpawner) data;
        spawner.setTrialSpawnerState(TrialSpawner.State.EJECTING_REWARD);
        loc.getBlock().setBlockData(spawner);

        World world = loc.getWorld();
        Location doorLocation;
        if (isNormalSpawner) {
            if (team.getName().equals("EnderEnterprise"))
                doorLocation = new Location(loc.getWorld(), -8, 141, 3.5);
            else
                doorLocation = new Location(loc.getWorld(), 70, 141, -18.5);

        } else {
            if (team.getName().equals("EnderEnterprise"))
                doorLocation = new Location(loc.getWorld(), -17, 141, 3.5);
            else
                doorLocation = new Location(loc.getWorld(), 79, 141, -18.5);
        }

        world.playSound(doorLocation, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.6f);
        world.playSound(doorLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);

        // Run repeating particle task for 3 seconds (~60 ticks)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 60) {
                    this.cancel();
                    return;
                }
                world.spawnParticle(Particle.ENCHANT, doorLocation, 100, 1, 1.5, 1.5, 3);
                ticks += 5;
            }
        }.runTaskTimer(getPlugin(), 0L, 5L);


        new BukkitRunnable() {
            @Override
            public void run() {
                BlockData newData = Bukkit.createBlockData(Material.TRIAL_SPAWNER);
                TrialSpawner inactive = (TrialSpawner) newData;
                inactive.setTrialSpawnerState(TrialSpawner.State.INACTIVE);
                loc.getBlock().setBlockData(inactive);

                if (isNormalSpawner) {
                    if (team.getName().equals("EnderEnterprise")) {
                        fillRegion(loc.getWorld(),
                            new Location(loc.getWorld(), -8, 141, 4),
                            new Location(loc.getWorld(), -8, 143, 3),
                            Material.AIR);
                    } else {
                        fillRegion(loc.getWorld(),
                            new Location(loc.getWorld(), 70, 141, -19),
                            new Location(loc.getWorld(), 70, 143, -18),
                            Material.AIR);

                    }
                }

                if (isOminousSpawner) {
                    if (team.getName().equals("EnderEnterprise")) {
                        fillRegion(loc.getWorld(),
                            new Location(loc.getWorld(), -17, 141, 4),
                            new Location(loc.getWorld(), -17, 144, 3),
                            Material.AIR);
                    } else {
                        fillRegion(loc.getWorld(),
                            new Location(loc.getWorld(), 79, 141, -19),
                            new Location(loc.getWorld(), 79, 144, -18),
                            Material.AIR);

                    }
                }

                world.playSound(doorLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);
                world.spawnParticle(Particle.EXPLOSION, doorLocation.add(0.5, 0.5, 0.5), 2, 0, 0, 0, 0.01);
                world.spawnParticle(Particle.CRIT, doorLocation, 30, 0.6, 0.6, 0.6, 0.05);

                Bukkit.broadcastMessage(getPlugin().prefix + ChatColor.WHITE + "Team " + team.getDisplayName() + ChatColor.WHITE + " Has unlocked a new room!");


                Location start = doorLocation.clone().add(0.5, 1, 0.5);

                Vector direction;
                if (team.getName().equals("EnderEnterprise"))
                    direction = new Vector(1, 0, 0);
                else
                    direction = new Vector(-1, 0, 0);

                int totalSteps = 15;
                double distancePerStep = 1;

                world.playSound(start, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 1.4f);

                new BukkitRunnable() {
                    int step = 0;

                    @Override
                    public void run() {
                        if (step > totalSteps) {
                            this.cancel();
                            return;
                        }

                        Location point = start.clone().add(direction.clone().multiply(step * distancePerStep));
                        world.spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);

                        step++;
                    }
                }.runTaskTimer(getPlugin(), 0L, 1L);

            }
        }.runTaskLater(getPlugin(), 60L);
    }


    public static void fillRegion(World world, Location corner1, Location corner2, Material material) {
        if (world == null) return;

        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(material);
                }
            }
        }
    }
}
