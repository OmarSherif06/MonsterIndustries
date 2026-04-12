package org.omar.monsterIndustries.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.omar.monsterIndustries.MonsterIndustries;
import org.omar.monsterIndustries.MonsterTeam;

public class LevelCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("monsterindustries.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /setlevel <team> <resource> <level>");
            return true;
        }

        String team = args[0];
        String resource = args[1].toLowerCase();
        int level;

        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Level must be a number.");
            return true;
        }

        MonsterTeam teamLevels = MonsterIndustries.getPlugin().teams.get(team);
        if (teamLevels == null) {
            sender.sendMessage(ChatColor.RED + "Team not found.");
            return true;
        }

        switch (resource) {
            case "paper":
                teamLevels.setPaper(level);
                break;
            case "coal":
                teamLevels.setCoal(level);
                break;
            case "bone":
                teamLevels.setBones(level);
                break;
            case "spidereye":
            case "spider_eye":
                teamLevels.setSpiderEye(level);
                break;
            case "gunpowder":
                teamLevels.setGunpowder(level);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown resource. Valid: paper, coal, bone, spidereye, gunpowder");
                return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Set " + resource + " level for " + team + " to " + level + ".");
        return true;
    }
}