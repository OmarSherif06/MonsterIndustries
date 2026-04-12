package org.omar.monsterIndustries.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.omar.monsterIndustries.MonsterTeam;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class BuyStockCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (strings.length < 2 || strings.length > 3) {
            commandSender.sendMessage("Usage: /buystock <team> <amount> <stockamount=1>");
            return false;
        }

        MonsterTeam team = getPlugin().teams.get(strings[0]);
        int amount = Integer.parseInt(strings[1]);
        int stockAmount = 1;

        if (strings[2] != null)
            stockAmount = Integer.parseInt(strings[2]);

        team.setStock(team.getStock() + amount * stockAmount);
        Bukkit.broadcastMessage(getPlugin().prefix + "Successfully bought " + (amount * stockAmount) + " stock for team " + strings[0]);

        if (team.getStock() >= getPlugin().getConfig().getInt("win-amount")) {
            getPlugin().winGame(team);
        }

        return true;
    }
}
