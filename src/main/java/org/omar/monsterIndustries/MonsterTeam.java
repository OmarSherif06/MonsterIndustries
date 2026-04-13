package org.omar.monsterIndustries;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class MonsterTeam {
    private int stock = 0;

    private int coal;
    private int paper;
    private int bones;
    private int amethyst;
    private int gunpowder;
    private int spidereye;
    private int villager;

    private ArrayList<Entity> slaves = new ArrayList<Entity>();

    public ArrayList<Entity> getSlaves() { return slaves; }

    private ArrayList<Location> upgradeBlocks = new ArrayList<Location>();

    public ArrayList<Location> getUpgradeBlocks() { return upgradeBlocks; }

    public MonsterTeam() {
        clearLevels();
    }

    public int getSpiderEye() {
        return spidereye;
    }

    public void setSpiderEye(int spidereye) {
        this.spidereye = spidereye;
    }

    public int getCoal() {
        return coal;
    }

    public void setCoal(int coal) {
        this.coal = coal;
    }

    public int getPaper() {
        return paper;
    }

    public void setPaper(int paper) {
        this.paper = paper;
    }

    public int getBones() {
        return bones;
    }

    public void setBones(int bones) {
        this.bones = bones;
    }

    public int getAmethyst() {
        return amethyst;
    }

    public void setAmethyst(int amethyst) {
        this.amethyst = amethyst;
    }

    public int getGunpowder() {
        return gunpowder;
    }

    public void setGunpowder(int gunpowder) {
        this.gunpowder = gunpowder;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;

        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Team team = convertTeam(this);
        if (team == null) return;
        String teamName = team.getName();


        var objective = scoreboard.getObjective("stock");

        var score = objective.getScore(team.getDisplayName());

        int newValue = Math.max(0, this.stock);
        score.setScore(newValue);

        if (getStock() >= getPlugin().getConfig().getInt("win-amount"))
            getPlugin().winGame(this);
    }

    public int getVillager() {
        return villager;
    }

    public void setVillager(int villager) {
        this.villager = villager;
    }

    public static MonsterTeam convertTeam(Team bukkitTeam) {
        if (bukkitTeam == null) return null;
        String name = bukkitTeam.getName();
        return getPlugin().teams.getOrDefault(name, null);
    }

    public static Team convertTeam(MonsterTeam monsterTeam) {
        if (monsterTeam == null) return null;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (Map.Entry<String, MonsterTeam> entry : getPlugin().teams.entrySet()) {
            if (entry.getValue().equals(monsterTeam)) {
                return scoreboard.getTeam(entry.getKey());
            }
        }
        return null;
    }

    public void clearSlaves() {
        for (Entity slave : this.getSlaves()) {
            if (slave instanceof Creeper creeper)
                creeper.setHealth(0);
            else if (slave instanceof Enderman enderman)
                enderman.setHealth(0);
            else if (slave instanceof Zombie zombie)
                zombie.setHealth(0);
        }
        this.getSlaves().clear();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill @e[tag=slave]");
    }

    public void clearLevels() {
        this.coal = 1;
        this.paper = 1;
        this.bones = 1;
        this.amethyst = 1;
        this.gunpowder = 1;
        this.spidereye = 1;
        this.villager = 1;
    }

    public void broadcast(String msg) {
        Set<OfflinePlayer> members = convertTeam(this).getPlayers();
        for (OfflinePlayer p : members) {
            if (p.isOnline()) {
                Player online = p.getPlayer();
                if (online != null)
                    online.sendMessage(getPlugin().prefix + msg);
            }
        }
    }

    public static MonsterTeam getTeam(Player player) {
        return MonsterTeam.convertTeam(Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName()));
    }

    public static void teleportToSpawn(Player player) {
        Team team = MonsterTeam.convertTeam(MonsterTeam.getTeam(player));

        if (team.getName().equals("EnderEnterprise"))
            player.teleport(new Location(Bukkit.getWorlds().getFirst(), 6.5, 141, -22.5));
        else
            player.teleport(new Location(Bukkit.getWorlds().getFirst(), 56.5, 141, 9.5));

    }
}
