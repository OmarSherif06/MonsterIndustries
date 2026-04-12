package org.omar.monsterIndustries.Listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.omar.monsterIndustries.MonsterTeam;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class Upgrades implements Listener {

    static Map<Material, String> upgradeNames = new HashMap<>() {{
        put(Material.COAL_BLOCK, "Coal");
        put(Material.NETHER_WART_BLOCK, "Spider Eye");
        put(Material.BONE_BLOCK, "Bone");
        put(Material.LIGHT_GRAY_CONCRETE_POWDER, "Gunpowder");
        put(Material.EMERALD_BLOCK, "Villager");
    }};

    @EventHandler
    public void playerUpgradeEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) return;

        MonsterTeam monsterTeam = getPlugin().teams.get(team.getName());
        if (monsterTeam == null) return;

        if (clicked.getType() != Material.RESPAWN_ANCHOR) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.GLOWSTONE) {
            event.setCancelled(true);
            player.sendMessage(getPlugin().prefix + ChatColor.RED + "Must be holding " + ChatColor.YELLOW + "Glowstone");
            player.setVelocity(player.getLocation().getDirection().multiply(-0.6));
            return;
        }

        BlockData blockData = clicked.getBlockData();
        RespawnAnchor respawnAnchor = (RespawnAnchor) blockData;
        Block respawnAnchorBlock = clicked;


        Block belowBlock = clicked.getRelative(BlockFace.DOWN);
        Location blockLoc = clicked.getLocation();
        BlockState state = clicked.getState();

        if (!monsterTeam.getUpgradeBlocks().contains(blockLoc)) {
            monsterTeam.getUpgradeBlocks().add(blockLoc);

            new BukkitRunnable() {

                @Override
                public void run() {
                    if(state.getBlockData() instanceof RespawnAnchor anchor){
                        if (respawnAnchor.getCharges() >= respawnAnchor.getMaximumCharges())
                            anchor.setCharges(0);
                        else
                            anchor.setCharges(1);
                        state.setBlockData(anchor);
                    }
                    state.update();
                }
            }.runTaskLater(getPlugin(), 1L);
        }

        if (respawnAnchor.getCharges() >= respawnAnchor.getMaximumCharges()) {
            player.sendMessage(ChatColor.RED + "This is already at max level!");
            event.setCancelled(true);
            return;
        }

        new BukkitRunnable() {
                @Override
                public void run() {
                    upgradeResource(team, belowBlock.getType(), (RespawnAnchor) blockLoc.getBlock().getBlockData());
                }
            }.runTaskLater(getPlugin(), 2L);
    }

    private static void upgradeResource(Team team, Material block, RespawnAnchor respawnAnchor) {
        int newLevel = respawnAnchor.getCharges() + 1;
        Bukkit.broadcastMessage("RESPAWN ANCHOR: " + respawnAnchor.getCharges());

        switch (block) {
            case COAL_BLOCK -> getPlugin().teams.get(team.getName()).setCoal(newLevel);
            case NETHER_WART_BLOCK -> getPlugin().teams.get(team.getName()).setSpiderEye(newLevel);
            case BONE_BLOCK -> getPlugin().teams.get(team.getName()).setBones(newLevel);
            case LIGHT_GRAY_CONCRETE_POWDER -> getPlugin().teams.get(team.getName()).setGunpowder(newLevel);
            case EMERALD_BLOCK -> getPlugin().teams.get(team.getName()).setVillager(newLevel);
        }

        if (block == Material.EMERALD_BLOCK) {
            refreshVillagers(team);
            Bukkit.broadcastMessage("UPGRADES.java");
        }

        if (upgradeNames.get(block) == null) {
            Bukkit.broadcastMessage(getPlugin().prefix + "There seems to be a problem with this upgrade, please contact an admin");
            return;
        }

        Bukkit.broadcastMessage(
                team.getDisplayName() + ChatColor.GRAY + " has leveled up their "
                        + ChatColor.YELLOW + upgradeNames.get(block)
                        + ChatColor.GRAY + " to Level " + ChatColor.GOLD + newLevel + ChatColor.GRAY + "!"
        );
    }

    public static void refreshVillagers(Team team) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tp @e[tag=shop,team=" + team.getName() + "] 0 0 0");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill @e[tag=shop,team=" + team.getName() + "]");

        if (team == null) return;

        int level = getPlugin().teams.get(team.getName()).getVillager();

        String teamName = team.getName().equals("CreeperCorp") ? "villagers-creeper" : "villagers-ender";
        List<Map<?, ?>> villagers = getPlugin().getConfig().getMapList(teamName);
        for (Map<?, ?> villagerData : villagers) {
            String command = (String) villagerData.get("command");
            String villagerTeam = (String) villagerData.get("team");
            if (command == null || command.isEmpty()) continue;

            executeVillagerCommand(command, level);
        }

        Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "execute as @e[tag=shop,team=" + team.getName() + "] at @s run particle minecraft:end_rod ~ ~1 ~ 0.3 0.8 0.3 0.02 80"
        );

    }

    public static void executeVillagerCommand(String command, int level) {
        Pattern pattern = Pattern.compile("Offers:\\{Recipes:\\[(.+)]}}");
        Matcher matcher = pattern.matcher(command);

        if (matcher.find()) {
            String recipes = matcher.group(1);
            String[] allRecipes = extractFullRecipes(recipes);

            // Split recipes into groups divided by barrier trades
            List<List<String>> levelGroups = new ArrayList<>();
            List<String> currentGroup = new ArrayList<>();

            for (String r : allRecipes) {
                if (r.contains("id:\"minecraft:barrier\"")) {
                    // Barrier marks end of a level group
                    if (!currentGroup.isEmpty()) {
                        levelGroups.add(new ArrayList<>(currentGroup));
                        currentGroup.clear();
                    }
                } else {
                    currentGroup.add(r.trim());
                }
            }
            if (!currentGroup.isEmpty()) levelGroups.add(currentGroup);

            List<String> validRecipes = new ArrayList<>();

            if (levelGroups.size() > 1) {
                // There are barriers → unlock full groups per level
                for (int i = 0; i < Math.min(level, levelGroups.size()); i++) {
                    validRecipes.addAll(levelGroups.get(i));
                }
            } else {
                // No barriers → fallback to normal one-by-one unlock
                for (int i = 0; i < Math.min(level, allRecipes.length); i++) {
                    validRecipes.add(allRecipes[i]);
                }
            }

            StringBuilder newRecipes = new StringBuilder();
            for (int i = 0; i < validRecipes.size(); i++) {
                if (i > 0) newRecipes.append(",");
                newRecipes.append(validRecipes.get(i));
            }

            String modified = matcher.replaceFirst("Offers:{Recipes:[" + newRecipes + "]}}");
            modified = cleanCommand(modified);
            runVillagerCommand(modified);

        } else {
            Bukkit.getLogger().info("[MonsterIndustries] Executing summon (no recipes found): " + command);
            runVillagerCommand(command);
        }
    }

    public static String[] extractFullRecipes(String recipes) {
        int depth = 0;
        StringBuilder current = new StringBuilder();
        List<String> result = new ArrayList<>();

        for (char c : recipes.toCharArray()) {
            if (c == '{') depth++;
            if (c == '}') depth--;
            current.append(c);
            if (depth == 0 && c == '}') {
                result.add(current.toString().trim());
                current.setLength(0);
            }
        }

        return result.toArray(new String[0]);
    }


    private static String cleanCommand(String cmd) {
        return cmd.replaceAll(",\\s*,", ",")
                .replaceAll("\\[,", "[")
                .replaceAll(",]", "]")
                .trim();
    }

    public static void runVillagerCommand(String cmd) {
        String toRun = cmd.startsWith("/") ? cmd.substring(1) : cmd;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), toRun);
    }


}
