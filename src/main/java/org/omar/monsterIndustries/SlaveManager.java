package org.omar.monsterIndustries;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Set;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class SlaveManager {

    public static boolean buySlave(Player player, MonsterTeam monsterTeam) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return false;

        String teamName = team.getName();
        World world = player.getWorld();

        boolean isEnderSide = teamName.equalsIgnoreCase("EnderEnterprise");
        Location loc = getSpawnLocation(monsterTeam, world, isEnderSide);
        if (loc == null) {
            player.sendMessage(ChatColor.RED + "Maximum amount of slaves reached!");
            return false;
        }

        if (!player.getInventory().containsAtLeast(new ItemStack(Material.PAPER), 64)) {
            player.sendMessage(ChatColor.RED + "You need at least 64 Paper to buy a slave!");
            return false;
        }

        player.getInventory().removeItem(new ItemStack(Material.PAPER, 64));

        spawnSlavePair(world, loc, monsterTeam, isEnderSide);
        monsterTeam.broadcast(player.getName() + " successfully bought a slave!");
        return true;
    }

    public static void addSlave(MonsterTeam monsterTeam, boolean isEnderSide, World world) {
        Location loc = getSpawnLocation(monsterTeam, world, isEnderSide);

        if (loc == null) return;

        spawnSlavePair(world, loc, monsterTeam, isEnderSide);
    }

    public static void removeLastSlave(MonsterTeam monsterTeam) {
        List<Entity> slaves = monsterTeam.getSlaves();
        if (slaves.isEmpty()) return;

        if (MonsterTeam.convertTeam(monsterTeam).getName().equals("CreeperCorp")) {
            Entity last = slaves.remove(slaves.size() - 1);
            last.remove();
        }


        Entity last = slaves.remove(slaves.size() - 1);
        last.remove();
    }

    private static void spawnSlavePair(World world, Location loc, MonsterTeam monsterTeam, boolean isEnderSide) {
        if (isEnderSide) {
            // Creeper slave
            Creeper creeper = (Creeper) world.spawnEntity(loc, EntityType.CREEPER);
            creeper.getAttribute(Attribute.GRAVITY).setBaseValue(0.0031);
            creeper.setInvulnerable(true);
            creeper.setPersistent(true);
            creeper.setRemoveWhenFarAway(false);
            
            assignEntityToTeam(creeper, "EnderEnterprise");
            monsterTeam.getSlaves().add(creeper);
        } else {
            // Enderman + zombie pair
            Enderman enderman = (Enderman) world.spawnEntity(loc, EntityType.ENDERMAN);
            enderman.setInvulnerable(true);
            enderman.setPersistent(true);
            enderman.setRemoveWhenFarAway(false);

            Zombie zombie = world.spawn(loc, Zombie.class, as -> {
                as.setInvisible(true);
                as.setGravity(true);
                as.setCollidable(false);
                as.setSilent(true);
            });

            EntityEquipment eq = zombie.getEquipment();
            if (eq != null) eq.clear();

            zombie.getAttribute(Attribute.GRAVITY).setBaseValue(0.0031);
            zombie.setInvulnerable(true);
            zombie.setPersistent(true);
            zombie.setRemoveWhenFarAway(false);

            monsterTeam.getSlaves().add(enderman);
            monsterTeam.getSlaves().add(zombie);

            assignEntityToTeam(enderman, "CreeperCorp");
            assignEntityToTeam(zombie, "CreeperCorp");

            // Keep enderman following zombie
            Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
                if (zombie.isValid() && enderman.isValid()) {
                    enderman.teleport(zombie.getLocation());
                }
            }, 0L, 1L);
        }
    }

    private static Location getSpawnLocation(MonsterTeam team, World world, boolean isEnderSide) {
        int count = team.getSlaves().size();
        if (isEnderSide) {
            return switch (count) {
                case 0 -> new Location(world, -2.5, 140, 19.5);
                case 1 -> new Location(world, -1.5, 140, 18.5);
                case 2 -> new Location(world, -0.5, 140, 19.5);
                case 3 -> new Location(world, 0.5, 140, 18.5);
                case 4 -> new Location(world, 1.5, 140, 19.5);
                default -> null;
            };
        } else {
            return switch (count) {
                case 0 -> new Location(world, 65.5, 140, -33.5);
                case 2 -> new Location(world, 64.5, 140, -32.5);
                case 4 -> new Location(world, 63.5, 140, -33.5);
                case 6 -> new Location(world, 62.5, 140, -32.5);
                case 8 -> new Location(world, 61.5, 140, -33.5);
                default -> null;
            };
        }
    }

    private static void assignEntityToTeam(Entity entity, String teamName) {
        entity.addScoreboardTag("slave");
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(teamName);
        if (team != null)
            team.addEntry(entity.getUniqueId().toString());
    }
}
