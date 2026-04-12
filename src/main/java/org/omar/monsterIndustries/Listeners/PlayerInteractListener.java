package org.omar.monsterIndustries.Listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.omar.monsterIndustries.Events;
import org.omar.monsterIndustries.MonsterTeam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class PlayerInteractListener implements Listener {

    private final HashMap<UUID, Long> potCooldowns = new HashMap<>();
    private final long POT_COOLDOWN = 1000;

    @EventHandler
    public void onPlayerInteractLever(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Player player = event.getPlayer();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());

        if (clicked.getType() == Material.LEVER) {
            Switch lever = (Switch) clicked.getBlockData();
            BlockFace attachedFace = lever.getFacing().getOppositeFace();
            Block attached = clicked.getRelative(attachedFace);

            if (attached.getType() == Material.BONE_BLOCK && !lever.isPowered()) {
                int amount = getPlugin().teams.get(team.getName()).getBones();

                player.sendActionBar(ChatColor.WHITE + "+" + amount + " Bone");
                player.getInventory().addItem(new ItemStack(Material.BONE, amount));

                player.playSound(player.getLocation(), Sound.ENTITY_SKELETON_STEP, 1.0f, 1.0f);
            }

        } else if (clicked.getType() == Material.DECORATED_POT) {
            event.setCancelled(true);

            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            long last = potCooldowns.getOrDefault(uuid, 0L);

            if (now - last < POT_COOLDOWN)  return;

            potCooldowns.put(uuid, now);

            clicked.getWorld().playSound(clicked.getLocation(),
                    Sound.BLOCK_DECORATED_POT_INSERT, 1.0f, 1.0f);

            clicked.getWorld().spawnParticle(
                    org.bukkit.Particle.DUST_PLUME,
                    clicked.getLocation().add(0.5, 0.7, 0.5),
                    15, // particle count
                    0.2, 0.2, 0.2, // spread
                    0.01
            );

            player.swingMainHand();

            int amount = getPlugin().teams.get(team.getName()).getGunpowder();
            player.sendActionBar(ChatColor.GRAY + "+" + amount + " Gunpowder");
            player.getInventory().addItem(new ItemStack(Material.GUNPOWDER, amount));
        } else if (clicked.getType() == Material.STONE_BUTTON) {
            Switch button = (Switch) clicked.getBlockData();
            BlockFace attachedFace = button.getFacing().getOppositeFace();
            Block attached = clicked.getRelative(attachedFace);

            if (attached.getType() == Material.GOLD_BLOCK && !button.isPowered()) {
                int amount = getPlugin().getConfig().getInt("stock-per-stack");
                int price = 64;

                if (Events.isMarketCollapse)
                    price = 32;

                MonsterTeam monsterTeam = getPlugin().teams.get(team.getName());

                if (monsterTeam.getStock() >= getPlugin().getConfig().getInt("win-amount") / 2) {
                    player.sendMessage(getPlugin().prefix + ChatColor.RED + "50% threshold reached cannot buy anymore (kill the enemy team)");
                    return;
                }

                if (!player.getInventory().containsAtLeast(new ItemStack(Material.PAPER), price)) {
                    player.sendMessage(ChatColor.RED + "You need at least 64 Paper to buy stock!");
                    return;
                }

                player.getInventory().removeItem(new ItemStack(Material.PAPER, price));


                monsterTeam.setStock(monsterTeam.getStock() + amount);
                Bukkit.broadcastMessage(getPlugin().prefix + "Successfully bought " + (amount) + " stock for team " + team.getDisplayName());


            }
        }
    }
}
