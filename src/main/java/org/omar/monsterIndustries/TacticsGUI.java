package org.omar.monsterIndustries;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
                ChatColor.GRAY + "Price: " + ChatColor.GOLD + "185 stock"
        );

        inv.setItem(10, insiderTeam);
        inv.setItem(12, stealItem);
        inv.setItem(14, marketItem);

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
            }
        }
    }

    private void marketCollapse(Player player) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        MonsterTeam monsterTeam = MonsterTeam.convertTeam(team);

        if (monsterTeam.getStock() < 185) {
            player.sendMessage("Insufficient Funds");
            player.closeInventory();
            return;
        }

        monsterTeam.setStock(monsterTeam.getStock() - 185);

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
