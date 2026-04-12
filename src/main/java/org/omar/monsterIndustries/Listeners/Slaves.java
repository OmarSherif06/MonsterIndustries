package org.omar.monsterIndustries.Listeners;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Team;
import org.omar.monsterIndustries.MonsterTeam;
import org.omar.monsterIndustries.SlaveManager;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class Slaves implements Listener {

    @EventHandler
    public void onButtonPress(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Material type = clicked.getType();
        if (type != Material.WARPED_BUTTON && type != Material.CRIMSON_BUTTON) return;

        Switch button = (Switch) clicked.getBlockData();
        BlockFace attachedFace = button.getFacing().getOppositeFace();
        Block attached = clicked.getRelative(attachedFace);

        if (attached.getType() != Material.REINFORCED_DEEPSLATE || button.isPowered()) return;

        Player player = event.getPlayer();
        Team team = MonsterTeam.convertTeam(MonsterTeam.getTeam(player));
        if (team == null) return;

        MonsterTeam monsterTeam = MonsterTeam.convertTeam(team);
        if (monsterTeam == null) return;

        SlaveManager.buySlave(player, monsterTeam);
    }

}