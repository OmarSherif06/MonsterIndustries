package org.omar.monsterIndustries.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static org.omar.monsterIndustries.GameManager.resetGame;
import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class ReloadConfigCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        getPlugin().reloadConfig();

        sender.sendMessage(getPlugin().prefix + ChatColor.GREEN + "Successfully reloaded the config!");

        resetGame();

        return true;
    }
}
