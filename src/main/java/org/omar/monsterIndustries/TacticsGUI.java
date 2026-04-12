package org.omar.monsterIndustries;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class TacticsGUI implements Listener {
    private final String MENU_TITLE = ChatColor.DARK_PURPLE + "Tactics";

    // Create the inventory
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        ItemStack loveItem = createMenuItem(
                Material.RED_DYE,
                ChatColor.RED + "Spread the Love",
                ChatColor.GRAY + "Turns enemy monsters into cats",
                ChatColor.GRAY + "Price: " + ChatColor.GOLD + "250 stock"
        );

        ItemStack stealItem = createMenuItem(
                Material.LEAD,
                ChatColor.BLUE + "Attempt to Steal a Slave",
                ChatColor.GRAY + "Steals one of the enemy team's slaves",
                ChatColor.GRAY + "Price: " + ChatColor.GOLD + "50 stock"
        );

        inv.setItem(10, loveItem);
        inv.setItem(12, stealItem);

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

            if (itemName.contains("Love")) {
                spreadTheLove(player);
            } else if (itemName.contains("Steal")) {
                stealSlave(player);
            }
        }
    }

    private void spreadTheLove(Player player) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        MonsterTeam monsterTeam = MonsterTeam.convertTeam(team);

        if (monsterTeam.getStock() < 250) {
            player.sendMessage("Insufficient Funds");
            player.closeInventory();
            return;
        }

        monsterTeam.setStock(monsterTeam.getStock() - 250);

        if (team.getName().equals("EnderEnterprise")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute at @e[team=CreeperCorp,tag=mob] run summon cat ~ ~ ~");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tp @e[team=CreeperCorp,tag=mob] 0 0 0");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill @e[team=CreeperCorp,tag=mob]");
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute at @e[team=EnderEnterprise,tag=mob] run summon cat ~ ~ ~");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tp @e[team=EnderEnterprise,tag=mob] 0 0 0");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill @e[team=EnderEnterprise,tag=mob]");
        }

        Bukkit.broadcastMessage(getPlugin().prefix + team.getDisplayName() + ChatColor.RED + " Has spread the love");
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
