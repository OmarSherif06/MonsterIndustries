package org.omar.monsterIndustries;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class Events {

    public enum EventType {
        PAYDAY,
        MARKET_COLLAPSE,
        SHELL_PROTECTION,
        LAUNCH_DAY,
        CRUNCH_TIME,
        OFFICE_PARTY,
        HOLIDAYS,
        HUNGOVER
    }

    public static boolean isPayday = false;
    public static boolean isMarketCollapse = false;
    public static boolean isShellProtection = false;
    public static boolean isLaunchDay = false;
    public static boolean isCrunchTime = false;
    public static boolean isOfficeParty = false;
    public static boolean isHolidays = false;
    public static boolean isHungover = false;

    public static void Payday() {
        if (!isPayday) {
            for (Map.Entry<String, MonsterTeam> entry : getPlugin().teams.entrySet()) {
                MonsterTeam monsterTeam = entry.getValue();
                isPayday = true;

                monsterTeam.setPaper(3);
            }
        } else {
            for (Map.Entry<String, MonsterTeam> entry : getPlugin().teams.entrySet()) {
                MonsterTeam monsterTeam = entry.getValue();
                isPayday = false;

                monsterTeam.setPaper(1);
            }
        }
    }

    public static void MarketCollapse() {
        if (!isMarketCollapse) {
            for (Map.Entry<String, MonsterTeam> entry : getPlugin().teams.entrySet()) {
                MonsterTeam monsterTeam = entry.getValue();
                isMarketCollapse = true;

                monsterTeam.setPaper(0);
            }
        } else {
            for (Map.Entry<String, MonsterTeam> entry : getPlugin().teams.entrySet()) {
                MonsterTeam monsterTeam = entry.getValue();
                isMarketCollapse = false;

                monsterTeam.setPaper(1);
            }
        }
    }

    public static void ShellProtection() {
        isShellProtection = !isShellProtection;
    }

    public static void LaunchDay() {
        if (!isLaunchDay) {
            for (Player player : Bukkit.getWorlds().getFirst().getPlayers()) {
                isLaunchDay = true;

                player.getAttribute(Attribute.GRAVITY).setBaseValue(0.02);
            }
        } else {
            for (Player player : Bukkit.getWorlds().getFirst().getPlayers()) {
                isLaunchDay = false;

                player.getAttribute(Attribute.GRAVITY).setBaseValue(0.08);
            }
        }
    }

    public static void CrunchTime() {
        if (!isCrunchTime) {
            for (Player player : Bukkit.getWorlds().getFirst().getPlayers()) {
                isCrunchTime = true;

                player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(5);
            }
        } else {
            for (Player player : Bukkit.getWorlds().getFirst().getPlayers()) {
                isCrunchTime = false;

                player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(4);
            }
        }
    }

    public static void OfficeParty() {
        if (!isOfficeParty) {
            for (Player player : Bukkit.getWorlds().getFirst().getPlayers()) {
                isOfficeParty = true;

                player.setSaturation(20);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 2400, 1, false, false));
            }
        } else {
            for (Player player : Bukkit.getWorlds().getFirst().getPlayers()) {
                isOfficeParty = false;

                player.removePotionEffect(PotionEffectType.SPEED);
                player.removePotionEffect(PotionEffectType.JUMP_BOOST);
            }
        }
    }

    private static Player first = null;
    private static Player second = null;
    public static void Holidays() {
        if (!isHolidays) {
            isHolidays = true;

            Team firstTeam = null;

            ArrayList<Player> allPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

            Collections.shuffle(allPlayers);

            for (Player player : allPlayers) {

                if (first == null) {
                    first = player;
                    firstTeam = MonsterTeam.convertTeam(MonsterTeam.getTeam(player));
                    continue;
                }

                Team team = MonsterTeam.convertTeam(MonsterTeam.getTeam(player));
                if (team == null || firstTeam == null) continue;

                if (!team.getName().equals(firstTeam.getName())) {
                    second = player;
                    break;
                }
            }
            Location holiday = new Location(Bukkit.getWorlds().getFirst(), 3, 144, 290);

            if (first != null)
                first.teleport(holiday);

            if (second != null)
                second.teleport(holiday);

        } else {
            isHolidays = false;

            MonsterTeam.teleportToSpawn(first);
            MonsterTeam.teleportToSpawn(second);
        }
    }

    public static void Hungover() {
        if (!isHungover) {
            for (Player player : Bukkit.getWorlds().getFirst().getPlayers()) {
                isHungover = true;

//                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 2400, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2400, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2400, 0, false, false));
            }
        } else {
            for (Player player : Bukkit.getWorlds().getFirst().getPlayers()) {
                isHungover = false;

//                player.removePotionEffect(PotionEffectType.NAUSEA);
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.removePotionEffect(PotionEffectType.BLINDNESS);
            }
        }
    }
}
