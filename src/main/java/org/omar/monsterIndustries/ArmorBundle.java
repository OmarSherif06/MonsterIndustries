package org.omar.monsterIndustries;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class ArmorBundle implements Listener {

    @EventHandler
    public void onPlayerInteractListener(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.getType().name().endsWith("BUNDLE")) return;


        Material boots = null, leggings = null, chest = null, helmet = null;
        String armorType = "";

        switch (item.getType()) {
            case BUNDLE:
                boots = Material.LEATHER_BOOTS;
                leggings = Material.LEATHER_LEGGINGS;
                chest = Material.LEATHER_CHESTPLATE;
                helmet = Material.LEATHER_HELMET;
                armorType = "Leather";
                break;
            case ORANGE_BUNDLE:
                boots = Material.COPPER_BOOTS;
                leggings = Material.COPPER_LEGGINGS;
                chest = Material.COPPER_CHESTPLATE;
                helmet = Material.COPPER_HELMET;
                armorType = "Copper";
                break;
            case WHITE_BUNDLE:
                boots = Material.IRON_BOOTS;
                leggings = Material.IRON_LEGGINGS;
                chest = Material.IRON_CHESTPLATE;
                helmet = Material.IRON_HELMET;
                armorType = "Iron";
                break;
            case CYAN_BUNDLE:
                boots = Material.DIAMOND_BOOTS;
                leggings = Material.DIAMOND_LEGGINGS;
                chest = Material.DIAMOND_CHESTPLATE;
                helmet = Material.DIAMOND_HELMET;
                armorType = "Diamond";
                break;
            case BLACK_BUNDLE:
                boots = Material.NETHERITE_BOOTS;
                leggings = Material.NETHERITE_LEGGINGS;
                chest = Material.NETHERITE_CHESTPLATE;
                helmet = Material.NETHERITE_HELMET;
                armorType = "Netherite";
                break;
            default:
                return;
        }

        player.getInventory().setBoots(new ItemStack(boots));
        player.getInventory().setLeggings(new ItemStack(leggings));
        player.getInventory().setChestplate(new ItemStack(chest));
        player.getInventory().setHelmet(new ItemStack(helmet));

        item.setAmount(item.getAmount() - 1);

        player.sendMessage(getPlugin().prefix + "You equipped " + armorType + " Armor!");
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.02);
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 10, 0.4, 0.4, 0.4, 0.05);
    }
}
