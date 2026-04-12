package org.omar.monsterIndustries;

import org.bukkit.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.omar.monsterIndustries.Commands.BuyStockCommand;
import org.omar.monsterIndustries.Commands.LevelCommand;
import org.omar.monsterIndustries.Commands.ReloadConfigCommand;
import org.omar.monsterIndustries.Commands.SurrenderCommand;
import org.omar.monsterIndustries.Listeners.*;
import org.omar.monsterIndustries.Weapons.Arrows;
import org.omar.monsterIndustries.Weapons.Zap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class MonsterIndustries extends JavaPlugin {

    private static MonsterIndustries plugin;
    public final Map<String, MonsterTeam> teams = new HashMap<>();
    public String prefix;

    public static MonsterIndustries getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        saveDefaultConfig();

        prefix = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("messages.prefix", ""));

        teams.put("EnderEnterprise", new MonsterTeam());
        teams.put("CreeperCorp", new MonsterTeam());

        setupScoreboard();

        Bukkit.getPluginManager().registerEvents(new PlayerStepOnPressurePlateListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerStepOnTripwireListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new Slaves(), this);
        Bukkit.getPluginManager().registerEvents(new Upgrades(), this);
        Bukkit.getPluginManager().registerEvents(new SpawnerUnlocks(), this);
        Bukkit.getPluginManager().registerEvents(new ArmorBundle(), this);
        Bukkit.getPluginManager().registerEvents(new GameManager(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);

        Bukkit.getPluginManager().registerEvents(new PotionSplashListener(), this);

        getServer().getPluginManager().registerEvents(new TacticsGUI(), this);

        // Weapons
        Bukkit.getPluginManager().registerEvents(new Zap(), this);
        Bukkit.getPluginManager().registerEvents(new Arrows(), this);

        getCommand("setlevel").setExecutor(new LevelCommand());
        getCommand("buyStock").setExecutor(new BuyStockCommand());
        getCommand("reloadConfig").setExecutor(new ReloadConfigCommand());
        getCommand("tactics").setExecutor((sender, command, label, args) -> {
            if (sender instanceof org.bukkit.entity.Player player) {
                new TacticsGUI().open(player);
            }
            return true;
        });
        getCommand("surrender").setExecutor(new SurrenderCommand());

        Bukkit.getScheduler().runTaskLater(this, () -> {
            List<Map<?, ?>> villagers = getConfig().getMapList("villagers");
            for (Map<?, ?> villagerData : villagers) {
                String command = (String) villagerData.get("command");

                if (command != null && !command.isEmpty()) {
                    boolean success = Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            command
                    );
                }
            }
        }, 20L);
        getLogger().info(prefix + " Monster Industries plugin has been enabled!");

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill @e[tag=slave]");

//        resetGame();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void setupScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();

        Scoreboard scoreboard = manager.getMainScoreboard();

        // Register objective if it doesn't exist
        Objective stockObjective = scoreboard.getObjective("stock");
        if (stockObjective == null) {
            stockObjective = scoreboard.registerNewObjective("stock", "dummy", "Stocks");
            stockObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        for (Team t : board.getTeams()) {
            if (t == null) continue;
            t.unregister();
        }

        // Register EnderEnterprise
        Team endermanTeam = board.registerNewTeam("EnderEnterprise");
        endermanTeam.setColor(ChatColor.DARK_PURPLE);
        endermanTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);

        // Register CreeperCorp
        Team creeperTeam = board.registerNewTeam("CreeperCorp");
        creeperTeam.setColor(ChatColor.GREEN);
        creeperTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);

        Bukkit.getOnlinePlayers().forEach(p -> p.setScoreboard(scoreboard));
    }

    public void winGame(MonsterTeam monsterTeam) {
        Team team = MonsterTeam.convertTeam(monsterTeam);

        String winMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.win").replace("{team}", team.getDisplayName()));

        Bukkit.broadcastMessage(prefix + winMessage);

//        resetGame();
    }

}