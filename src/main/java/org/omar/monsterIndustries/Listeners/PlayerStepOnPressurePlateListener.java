package org.omar.monsterIndustries.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.omar.monsterIndustries.MonsterTeam;

import java.util.HashMap;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class PlayerStepOnPressurePlateListener implements Listener {

    // Cooldown map to store the last time a block (pressure plate) was triggered
    private final HashMap<Block, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 1000;

    @EventHandler
    public void onPlayerStepOnPressurePlate(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        Material type = clickedBlock == null ? null : clickedBlock.getType();

        if (event.getAction() != Action.PHYSICAL) {
            return;
        }

        if (type == Material.STONE_PRESSURE_PLATE) {

            long currentTime = System.currentTimeMillis();

            // Check if the block is on cooldown
            if (cooldowns.containsKey(clickedBlock)) {
                long lastTriggered = cooldowns.get(clickedBlock);
                if (currentTime - lastTriggered < COOLDOWN_TIME) {
                    return;
                }
            }

            cooldowns.put(clickedBlock, currentTime);

            Block belowBlock = clickedBlock.getLocation().add(0, -1, 0).getBlock();
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            assert team != null;
            int paperAmount = MonsterTeam.convertTeam(team).getPaper();

            if (belowBlock.getType() == Material.PURPLE_CONCRETE) {
                if (!team.getName().equals("EnderEnterprise")) { return; }

                player.getInventory().addItem(new ItemStack(Material.PAPER, paperAmount));
                player.sendActionBar(ChatColor.DARK_PURPLE + "+" + paperAmount + " Paper");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

            } else if (belowBlock.getType() == Material.GREEN_CONCRETE) {
                if (!team.getName().equals("CreeperCorp")) { return; }

                player.sendActionBar(ChatColor.GREEN + "+" + paperAmount + " Paper");
                player.getInventory().addItem(new ItemStack(Material.PAPER, paperAmount));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

            } else if (belowBlock.getType() == Material.COAL_BLOCK) {
                int amount = getPlugin().teams.get(team.getName()).getCoal();

                if (MonsterTeam.convertTeam(team).isDouble) amount *= 2;

                player.sendActionBar(ChatColor.BLACK + "+" + amount + " Coal");
                player.getInventory().addItem(new ItemStack(Material.COAL, amount));
                player.playSound(player.getLocation(), Sound.UI_STONECUTTER_TAKE_RESULT, 1.0f, 1.0f);

            }
        }
    }
}