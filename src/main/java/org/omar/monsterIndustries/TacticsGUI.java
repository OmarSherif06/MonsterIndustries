package org.omar.monsterIndustries;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.omar.monsterIndustries.Listeners.SpawnerUnlocks;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class TacticsGUI implements Listener {
    private final String MENU_TITLE = ChatColor.DARK_PURPLE + "Tactics";

    // Create the inventory
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        ItemStack insiderTeam = createMenuItem(
                Material.EMERALD,
                ChatColor.GREEN + "Insider Trading",
                ChatColor.GRAY + "Doubles all non-paper resource generation for 1 minute",
                ChatColor.GRAY + "Price: " + ChatColor.GOLD + "100 stock"
        );

        ItemStack stealItem = createMenuItem(
                Material.LEAD,
                ChatColor.BLUE + "Attempt to Steal a Slave",
                ChatColor.GRAY + "Steals one of the enemy team's slaves",
                ChatColor.GRAY + "Price: " + ChatColor.GOLD + "50 stock"
        );

        ItemStack marketItem = createMenuItem(
                Material.IRON_DOOR,
                ChatColor.WHITE + "Markets' Closed!",
                ChatColor.GRAY + "Lock enemy team's shops for 1 minute 30 seconds",
                ChatColor.GRAY + "Price: " + ChatColor.GOLD + "200 stock"
        );

        ItemStack corporateItem = createMenuItem(
                Material.FLINT_AND_STEEL,
                ChatColor.WHITE + "Corporate Espionage",
                ChatColor.GRAY + "Kills will grant only 50% stocks for enemy team for 2 minutes",
                ChatColor.GRAY + "Price: " + ChatColor.GOLD + "135 stock"
        );

        ItemStack frameEnemyItem = createMenuItem(
                Material.IRON_BARS,
                ChatColor.GRAY + "Frame Enemy",
                ChatColor.GRAY + "Imprisons one of the enemy team for 1 minute",
                ChatColor.GRAY + "Price: " + ChatColor.GOLD + "225 stock"
        );

        inv.setItem(10, insiderTeam);
        inv.setItem(12, stealItem);
        inv.setItem(14, marketItem);
        inv.setItem(16, corporateItem);
        inv.setItem(28, frameEnemyItem);

        player.openInventory(inv);
    }

    private ItemStack createMenuItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(loreLines));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(MENU_TITLE)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            String itemName = clicked.getItemMeta().getDisplayName();

            if (itemName.contains("Insider")) {
                insiderTrading(player);
            } else if (itemName.contains("Steal")) {
                stealSlave(player);
            } else if (itemName.contains("Market")) {
                marketCollapse(player);
            } else if (itemName.contains("Corporate")) {
                corporate(player);
            } else if (itemName.contains("Frame")) {
                frameEnemy(player);
            }
        }
    }

    private void frameEnemy(Player player) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        MonsterTeam monsterTeam = MonsterTeam.convertTeam(team);

        if (monsterTeam.getStock() < 225) {
            player.sendMessage(ChatColor.RED + "Insufficient Funds");
            player.closeInventory();
            return;
        }

        boolean isEnder = team.getName().equals("EnderEnterprise");
        Team enemyTeam = Bukkit.getScoreboardManager()
                .getMainScoreboard()
                .getTeam(isEnder ? "CreeperCorp" : "EnderEnterprise");

        List<Player> enemies = enemyTeam.getEntries().stream()
                .map(Bukkit::getPlayerExact)
                .filter(p -> p != null && p.isOnline())
                .toList();

        monsterTeam.setStock(monsterTeam.getStock() - 225);

        Player target = enemies.get((int) (Math.random() * enemies.size()));

        Bukkit.broadcastMessage(
                getPlugin().prefix +
                        team.getDisplayName() +
                        ChatColor.RED +
                        " has framed " +
                        target.getName() + "!"
        );

        player.closeInventory();

        if (enemyTeam.getName().equals("CreeperCorp")) {
            target.teleport(new Location(Bukkit.getWorlds().getFirst(), 7, 141, 8.5));
        } else {
            target.teleport(new Location(Bukkit.getWorlds().getFirst(), 55.5, 141, -22.5));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                MonsterTeam.teleportToSpawn(target);
            }
        }.runTaskLater(getPlugin(), (20 * 60)); // one minute

    }

    private void corporate(Player player) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        MonsterTeam monsterTeam = MonsterTeam.convertTeam(team);

        if (monsterTeam.getStock() < 135) {
            player.sendMessage(ChatColor.RED + "Insufficient Funds");
            player.closeInventory();
            return;
        }

        monsterTeam.setStock(monsterTeam.getStock() - 135);

        monsterTeam.isEspionaged = true;

        Bukkit.broadcastMessage(
                getPlugin().prefix +
                        team.getDisplayName() +
                        ChatColor.RED + " has activated CORPORATE ESPIONAGE!"
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_PILLAGER_CELEBRATE, 0.6f, 1.2f);
        }

        player.closeInventory();

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {

            monsterTeam.isEspionaged = false;

            Bukkit.broadcastMessage(
                    getPlugin().prefix +
                            ChatColor.GREEN + "Corporate Espionage has ended."
            );

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_PILLAGER_HURT, 0.6f, 1.2f);
            }

        }, 120 * 20L);
    }

    private void marketCollapse(Player player) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        MonsterTeam monsterTeam = MonsterTeam.convertTeam(team);

        if (monsterTeam.getStock() < 200) {
            player.sendMessage("Insufficient Funds");
            player.closeInventory();
            return;
        }

        monsterTeam.setStock(monsterTeam.getStock() - 200);

        Location location1;
        Location location2;
        World world = Bukkit.getWorlds().getFirst();
        if (team.getName().equals("EnderEnterprise")) {
            location1 = new Location(world, 58, 141, -19);

            location2 = new Location(world, 58, 142, -18);
        } else {
            location1 = new Location(world, 4, 141, 4);

            location2 = new Location(world, 4, 142, 3);
        }

        SpawnerUnlocks.fillRegion(world, location1, location2, Material.RED_STAINED_GLASS);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1f);
        }

        Bukkit.broadcastMessage(getPlugin().prefix +
                team.getDisplayName() + ChatColor.WHITE +
                " has closed the market!");

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {

            SpawnerUnlocks.fillRegion(world, location1, location2, Material.AIR);

            Bukkit.broadcastMessage(getPlugin().prefix + team.getDisplayName() + ChatColor.GREEN + "Market is open again!");

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
            }

        }, 90 * 20L);
    }

    private void insiderTrading(Player player) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        MonsterTeam monsterTeam = MonsterTeam.convertTeam(team);

        if (monsterTeam.getStock() < 100) {
            player.sendMessage("Insufficient Funds");
            player.closeInventory();
            return;
        }

        monsterTeam.setStock(monsterTeam.getStock() - 100);

        monsterTeam.isDouble = true;

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            monsterTeam.isDouble = false;
            Bukkit.broadcastMessage(getPlugin().prefix + team.getDisplayName() + ChatColor.GREEN + " Insider Trading deactivated");

            for (Player soundPlayer : Bukkit.getOnlinePlayers()) {
                player.getWorld().playSound(soundPlayer, Sound.ENTITY_VILLAGER_NO, 1, 1);
            }
        }, 1200L);


        Bukkit.broadcastMessage(getPlugin().prefix + team.getDisplayName() + ChatColor.GREEN + " Has activated Insider Trading");
        for (Player soundPlayer : Bukkit.getOnlinePlayers()) {
            player.getWorld().playSound(soundPlayer, Sound.ENTITY_VILLAGER_TRADE, 1, 1);
        }
        player.closeInventory();
    }

    private void stealSlave(Player player) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        MonsterTeam monsterTeam = MonsterTeam.convertTeam(team);


        if (monsterTeam.getStock() < 50) {
            player.sendMessage("Insufficient Funds");
            player.closeInventory();
            return;
        }

        monsterTeam.setStock(monsterTeam.getStock() - 50);
        boolean isEnder = team.getName().equals("EnderEnterprise");
        MonsterTeam enemyTeam = MonsterTeam.convertTeam(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(isEnder ? "CreeperCorp" : "EnderEnterprise"));
        if (enemyTeam.getSlaves().isEmpty()) {
            player.sendMessage(getPlugin().prefix + "Enemy team has no slaves (Skill Issue)");
            player.closeInventory();
            return;
        }

        if (monsterTeam.getSlaves().size() < 5)
            SlaveManager.addSlave(monsterTeam, isEnder, player.getWorld());
        SlaveManager.removeLastSlave(enemyTeam);

        Bukkit.broadcastMessage(getPlugin().prefix + team.getDisplayName() + ChatColor.RED + " Has stolen a slave from the other team");

        player.closeInventory();
    }
}
