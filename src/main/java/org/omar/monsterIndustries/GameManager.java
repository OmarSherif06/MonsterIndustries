package org.omar.monsterIndustries;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.omar.monsterIndustries.Listeners.Upgrades;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Sound.*;
import static org.omar.monsterIndustries.Listeners.SpawnerUnlocks.fillRegion;
import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class GameManager implements Listener {

    private final Map<Player, Block> ready = new HashMap<>();

    public static boolean running = false;

    public GameManager() {
        // Ready Check
        new BukkitRunnable() {
            @Override
            public void run() {
                checkReadyPlayers();
            }
        }.runTaskTimer(getPlugin(), 20, 5);


        // Random Events
        Random random = new Random();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!running) return;

                Events.EventType[] events = Events.EventType.values();
                Events.EventType randomEvent = events[random.nextInt(events.length)];
//                Events.EventType randomEvent = Events.EventType.HUNGOVER;

                switch (randomEvent) {
                    case PAYDAY:
                        Events.Payday();
                        broadcastTitle("&a&lPayday", "Paper production tripled.", 0, 60, 0);
                        playSoundToAll(Sound.ENTITY_PLAYER_LEVELUP);
                        break;

                    case MARKET_COLLAPSE:
                        Events.MarketCollapse();
                        broadcastTitle("&4&lMarket Collapse", "Paper halted. Stocks cost only 32 Paper.", 0, 60, 0);
                        playSoundToAll(Sound.BLOCK_ANVIL_LAND);
                        break;

                    case SHELL_PROTECTION:
                        Events.ShellProtection();
                        broadcastTitle("&b&lShell Protection", "Deaths give no stock to the enemy.", 0, 60, 0);
                        playSoundToAll(Sound.ITEM_SHIELD_BLOCK);
                        break;

                    case LAUNCH_DAY:
                        Events.LaunchDay();
                        broadcastTitle("&3&lLaunch Day", "Gravity dramatically reduced.", 20, 120, 20);
                        playSoundToAll(Sound.ENTITY_PHANTOM_FLAP);
                        break;

                    case CRUNCH_TIME:
                        Events.CrunchTime();
                        broadcastTitle("&6&lCrunch Time", "Double stock on kill. Attack speed boosted.", 20, 120, 20);
                        playSoundToAll(Sound.ENTITY_ZOMBIE_VILLAGER_CURE);
                        break;

                    case OFFICE_PARTY:
                        Events.OfficeParty();
                        broadcastTitle("&d&lOffice Party", "Saturation, Speed I, Jump Boost I for all.", 20, 120, 20);
                        playSoundToAll(Sound.ENTITY_VILLAGER_CELEBRATE);
                        break;

                    case HOLIDAYS:
                        Events.Holidays();
                        broadcastTitle("&e&lHolidays", "A random player from each team goes on holiday.", 20, 120, 20);
                        playSoundToAll(Sound.ENTITY_ENDERMAN_TELEPORT);
                        break;

                    case HUNGOVER:
                        Events.Hungover();
                        broadcastTitle("&5&lHungover", "Nausea, Slowness I, Blindness I for all players.", 20, 120, 20);
                        playSoundToAll(Sound.ENTITY_GENERIC_DRINK);
                        break;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {

                        if (Events.isPayday) {
                            Events.Payday();
                            broadcastTitle("&a&lPayday Deactivated!", "", 0, 40, 0);
                        }

                        if (Events.isMarketCollapse) {
                            Events.MarketCollapse();
                            broadcastTitle("&4&lMarket Collapse Deactivated!", "", 0, 40, 0);
                        }

                        if (Events.isShellProtection) {
                            Events.ShellProtection();
                            broadcastTitle("&b&lShell Protection Deactivated!", "", 0, 40, 0);
                        }

                        if (Events.isLaunchDay) {
                            Events.LaunchDay();
                            broadcastTitle("&3&lLaunch Day Deactivated!", "", 0, 40, 0);
                        }

                        if (Events.isCrunchTime) {
                            Events.CrunchTime();
                            broadcastTitle("&6&lCrunch Time Deactivated!", "", 0, 40, 0);
                        }

                        if (Events.isOfficeParty) {
                            Events.OfficeParty();
                            broadcastTitle("&d&lOffice Party Deactivated!", "", 0, 40, 0);
                        }

                        if (Events.isHolidays) {
                            Events.Holidays();
                            broadcastTitle("&e&lHolidays Ended!", "", 0, 40, 0);
                        }

                        if (Events.isHungover) {
                            Events.Hungover();
                            broadcastTitle("&5&lHungover Ended!", "", 0, 40, 0);
                        }

                    }
                }.runTaskLater(getPlugin(), 2400L);


            }
        }.runTaskTimer(getPlugin(), 0L, 14400L);
    }

    @EventHandler
    public void onPlayerStepOnPressurePlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.STONE_PRESSURE_PLATE) return;

        Player player = event.getPlayer();
        Team team = MonsterTeam.convertTeam(MonsterTeam.getTeam(player));
        Block below = clickedBlock.getLocation().add(0, -1, 0).getBlock();

        if (below.getType() != Material.RED_CONCRETE) return;

        Bukkit.broadcastMessage(getPlugin().prefix +
                ((team.getName().equals("EnderEnterprise")) ? ChatColor.DARK_PURPLE : ChatColor.GREEN) +
                player.getName() + ChatColor.GRAY + " is ready!");

        below.setType(Material.LIME_CONCRETE);

        player.playSound(player.getLocation(), ENTITY_PLAYER_LEVELUP, 1f, 1f);

        player.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                clickedBlock.getLocation().add(0.5, 1, 0.5),
                30,
                0.3, 0.3, 0.3,
                0.01
        );

        ready.put(player, clickedBlock);

        checkBothTeamsTeleport();
    }

    private void checkReadyPlayers() {
        ready.entrySet().removeIf(entry -> {
            Player player = entry.getKey();
            Block plate = entry.getValue();

            if (!player.isOnline()) return true;

            Block underPlayer = player.getLocation().getBlock();
            if (underPlayer.equals(plate)) return false;

            Block below = plate.getLocation().add(0, -1, 0).getBlock();
            if (below.getType() == Material.LIME_CONCRETE)
                below.setType(Material.RED_CONCRETE);

            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            if (team != null) {
                Bukkit.broadcastMessage(getPlugin().prefix +
                        ((team.getName().equals("EnderEnterprise")) ? ChatColor.DARK_PURPLE : ChatColor.GREEN) +
                        player.getName() + ChatColor.GRAY + " is not ready!");
            }

            return true;
        });
    }

    private void checkBothTeamsTeleport() {
        Team enderTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("EnderEnterprise");
        Team otherTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream()
                .filter(t -> !t.getName().equals("EnderEnterprise"))
                .findFirst().orElse(null);

        if (enderTeam == null || otherTeam == null) return;

        // Count only online Players in each team
        long enderSize = enderTeam.getEntries().stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .count();

        long otherSize = otherTeam.getEntries().stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .count();

        long enderReady = ready.keySet().stream()
                .filter(p -> enderTeam.hasEntry(p.getName()))
                .count();

        long otherReady = ready.keySet().stream()
                .filter(p -> otherTeam.hasEntry(p.getName()))
                .count();

        int enderRequired = (enderSize == 1) ? 1 : (int)Math.min(2, enderSize);
        int otherRequired = (otherSize == 1) ? 1 : (int)Math.min(2, otherSize);

        if (enderSize == 0 || otherSize == 0) return;

        if (enderReady >= enderRequired && otherReady >= otherRequired) {
//            resetGame();

            // Teleport EnderEnterprise
            ready.keySet().stream()
                    .filter(p -> enderTeam.hasEntry(p.getName()))
                    .limit(2)
                    .forEach(p -> {
                        MonsterTeam.teleportToSpawn(p);

                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255, false, false, false));
                    });

            // Teleport other team
            ready.keySet().stream()
                    .filter(p -> otherTeam.hasEntry(p.getName()))
                    .limit(2)
                    .forEach(p -> {
                        MonsterTeam.teleportToSpawn(p);

                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255, false, false, false));
                    });

            new BukkitRunnable() {
                int count = 3;

                @Override
                public void run() {
                    if (count <= 0) {
                        cancel();

                        if (count <= 0) {
                            Bukkit.getOnlinePlayers().stream()
                                    .filter(p -> !enderTeam.hasEntry(p.getName()) && !otherTeam.hasEntry(p.getName()))
                                    .forEach(p -> {
                                        p.teleport(new Location(Bukkit.getWorlds().getFirst(), 31, 149, -41));
                                        p.sendMessage(getPlugin().prefix + ChatColor.RED + "You are not on a team! Teleported to the Spectator Hub.");
                                    });
                        }

                        for (Player p : Bukkit.getOnlinePlayers()) {

                            p.sendTitle(ChatColor.RED + "START", "", 0, 20, 0);

                            p.playSound(
                                    p.getLocation(),
                                    ENTITY_PLAYER_LEVELUP,
                                    1f,
                                    1f
                            );
                        }

                        running = true;
                        return;
                    }

                    for (Player p : Bukkit.getOnlinePlayers()) {

                        p.sendTitle(ChatColor.RED + "" + count, "", 0, 20, 0);

                        p.playSound(
                                p.getLocation(),
                                Sound.BLOCK_NOTE_BLOCK_PLING,
                                1f,
                                3 - count
                        );
                    }

                    count--;
                }
            }.runTaskTimer(getPlugin(), 0, 20);
        }
    }

    public static void resetGame() {
        // Stop Game
        running = false;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill @e[type=item]");

        for (Map.Entry<String, MonsterTeam> teamMap : getPlugin().teams.entrySet()) {
            MonsterTeam monsterTeam = teamMap.getValue();

            // Reset Stocks
            monsterTeam.setStock(0);

            // Reset all levels blocks
            monsterTeam.clearLevels();

            getLogger().info("Refreshing villagers for team: " + teamMap.getKey());

            Upgrades.refreshVillagers(MonsterTeam.convertTeam(monsterTeam));

            // Kill all slaves
            monsterTeam.clearSlaves();

            // Reset all upgrade blocks
            for (Location upgradeBlockLocation : monsterTeam.getUpgradeBlocks()) {
                upgradeBlockLocation.getBlock().setType(Material.RESPAWN_ANCHOR, false);
            }
            monsterTeam.getUpgradeBlocks().clear();


            // Reset Trial Spawners
            // EnderEnterprise
            World world = Bukkit.getWorld("world");
            Location loc = new Location(world, -6, 142, 2);
            Location loc2 = new Location(world, -16, 142, 2);

            BlockData data = Bukkit.createBlockData(Material.TRIAL_SPAWNER);
            org.bukkit.block.data.type.TrialSpawner trialSpawner = (org.bukkit.block.data.type.TrialSpawner) data;
            trialSpawner.setTrialSpawnerState(TrialSpawner.State.ACTIVE);

            Block block = loc.getBlock();
            block.setType(Material.TRIAL_SPAWNER, false);
            block.setBlockData(trialSpawner);

            block = loc2.getBlock();
            block.setType(Material.TRIAL_SPAWNER, false);
            block.setBlockData(trialSpawner);

            fillRegion(loc.getWorld(),
                    new Location(loc.getWorld(), -6, 141, 3),
                    new Location(loc.getWorld(), -7, 143, 4),
                    Material.MAGENTA_STAINED_GLASS);

            fillRegion(loc.getWorld(),
                    new Location(loc.getWorld(), -16, 141, 3),
                    new Location(loc.getWorld(), -16, 143, 4),
                    Material.MAGENTA_STAINED_GLASS);

            for (Player p : Bukkit.getOnlinePlayers()) {
                Team team = MonsterTeam.convertTeam(monsterTeam);
                if (team == null) continue;
                team.removeEntry(p.getName());
            }
        }

        // Empty Chests/Hoppers
        // Creeper Side paper chest
        BlockState state = new Location(Bukkit.getWorlds().getFirst(), 39, 140, -7).getBlock().getState();
        if (state instanceof Container container)
            container.getInventory().clear();

        // Creeper Side hoppers
        for (int i = 0; i < 2; i++) {
            Block block = new Location(Bukkit.getWorlds().getFirst(), 39, 141, (-7 - i)).getBlock();
            state = block.getState();
            if (state instanceof Container container)
                container.getInventory().clear();
        }

        // Ender Side paper chest
        state = new Location(Bukkit.getWorlds().getFirst(), 23, 140, -8).getBlock().getState();
        if (state instanceof Container container)
            container.getInventory().clear();

        // Ender Side hoppers
        for (int i = 0; i < 2; i++) {
            Block block = new Location(Bukkit.getWorlds().getFirst(), 23, 141, (-8 + i)).getBlock();
            state = block.getState();
            if (state instanceof Container container)
                container.getInventory().clear();
        }

        // Creeper Side non-paper chest
        state = new Location(Bukkit.getWorlds().getFirst(), 54, 141, -14).getBlock().getState();
        if (state instanceof Container container)
            container.getInventory().clear();

        // Ender Side non-paper chest
        state = new Location(Bukkit.getWorlds().getFirst(), 8, 141, -1).getBlock().getState();
        if (state instanceof Container container)
            container.getInventory().clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            Location loc = new Location(Bukkit.getWorld("world"), 85, 140, 96);
            // DEBUGGING
            p.teleport(loc);

            p.setGameMode(GameMode.ADVENTURE);

            p.closeInventory();
            resetPlayer(p);
        }
    }

    public static void resetPlayer(Player player) {
        if (player == null) return;

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);

        player.updateInventory(); // Forces client refresh

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(5);

        player.setExp(0);
        player.setExperienceLevelAndProgress(0);

        player.clearActivePotionEffects();

        player.getAttribute(Attribute.GRAVITY).setBaseValue(0.08);
        player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(4);
    }

    public static void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        String coloredTitle = ChatColor.translateAlternateColorCodes('&', title);
        String coloredSubtitle = ChatColor.translateAlternateColorCodes('&', subtitle);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(coloredTitle, coloredSubtitle, fadeIn, stay, fadeOut);
        }
    }

    public static void playSoundToAll(Sound sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, 1f, 1f);
        }
    }

}