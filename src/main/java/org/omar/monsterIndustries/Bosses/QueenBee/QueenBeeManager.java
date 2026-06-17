package org.omar.monsterIndustries.Bosses.QueenBee;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.omar.monsterIndustries.MonsterIndustries.getPlugin;

public class QueenBeeManager implements Listener {

    final int MAXBEES = 15;
    int beeCounter = 0;

    LivingEntity bossEntity = null;

    public QueenBeeManager() {
        Bukkit.getScheduler().runTaskTimer(
                getPlugin(),
                () -> {
                    if (bossEntity == null || bossEntity.isDead()) return;

                    Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(bossEntity.getUniqueId().toString());

                    for (int i = 0; i < 3; i++) {
                        spawnBee(bossEntity.getLocation(), team);
                    }
                },
                400L, // first run after 20 seconds
                400L  // repeat every 20 seconds
        );
    }

    @EventHandler
    void QueenBeeSpawnEvent(CreatureSpawnEvent event) {
        if (event.getEntity().getType() != EntityType.BEE) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;

        bossEntity = (LivingEntity) event.getEntity();

        if (!bossEntity.getScoreboardTags().contains("boss")) return;
        Location location = bossEntity.getLocation();

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(bossEntity.getUniqueId().toString());

        for (int i = 0; i < 3; i++) {
            spawnBee(location, team);
        }
    }

    @EventHandler
    void QueenBeeDeathEvent(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.BEE) return;
        if (bossEntity != event.getEntity()) return;

        Location oldLoc = bossEntity.getLocation().clone();

        List<Bee> offspring = bossEntity.getWorld().getEntitiesByClass(Bee.class)
                .stream()
                .filter(b -> b.getScoreboardTags().contains("offspring"))
                .toList();

        if (offspring.isEmpty()) return;

        Bee randomBee = offspring.get(
                ThreadLocalRandom.current().nextInt(offspring.size())
        );

        Location newLoc = randomBee.getLocation().clone();

        Team team = Bukkit.getScoreboardManager()
                .getMainScoreboard()
                .getEntryTeam(bossEntity.getUniqueId().toString());

        String teamName = (team != null) ? team.getName() : "";

        String command =
                "summon bee " + newLoc.getX() + " " + newLoc.getY() + " " + newLoc.getZ() +
                        " {Team:\"" + teamName +
                        "\",Health:100f,HasNectar:1b,Tags:[\"mob\",\"boss\"],CustomName:" +
                        "[{\"bold\":true,\"color\":\"#FFBB00\",\"shadow_color\":-4555258,\"text\":\"Q\"}," +
                        "{\"bold\":true,\"color\":\"#FFC734\",\"shadow_color\":-4555258,\"text\":\"u\"}," +
                        "{\"bold\":true,\"color\":\"#FFD468\",\"shadow_color\":-4555258,\"text\":\"e\"}," +
                        "{\"bold\":true,\"color\":\"#FFD879\",\"shadow_color\":-4555258,\"text\":\"e\"}," +
                        "{\"bold\":true,\"color\":\"#FFCC45\",\"shadow_color\":-4555258,\"text\":\"n \"}," +
                        "{\"bold\":true,\"color\":\"#FFBF11\",\"shadow_color\":-4555258,\"text\":\"B\"}," +
                        "{\"bold\":true,\"color\":\"#FFC323\",\"shadow_color\":-4555258,\"text\":\"e\"}," +
                        "{\"bold\":true,\"color\":\"#FFDC8A\",\"shadow_color\":-4555258,\"text\":\"e\"}]," +
                        "attributes:[" +
                        "{id:\"minecraft:max_health\",base:100}," +
                        "{id:\"minecraft:scale\",base:6}" +
                        "]}";

        drawBeeTransfer(oldLoc, newLoc);

        Bukkit.getWorlds().getFirst().playSound(
                oldLoc,
                Sound.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE,
                1,
                1
        );

        randomBee.setAI(false);
        randomBee.setInvulnerable(true);

        new BukkitRunnable() {

            double scale = 1.0;

            @Override
            public void run() {

                AttributeInstance scaleAttr =
                        randomBee.getAttribute(Attribute.SCALE);

                if (scaleAttr != null) {
                    scale += 0.25;
                    scaleAttr.setBaseValue(scale);
                }

                randomBee.getWorld().spawnParticle(
                        Particle.WAX_ON,
                        randomBee.getLocation().add(0, 1, 0),
                        6,
                        0.25, 0.25, 0.25,
                        0
                );

                randomBee.getWorld().spawnParticle(
                        Particle.DUST,
                        randomBee.getLocation().add(0, 1, 0),
                        2,
                        new Particle.DustOptions(
                                Color.fromRGB(255, 200, 0),
                                1.5f
                        )
                );

                if (scale >= 6.0) {
                    randomBee.teleport(new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0));

                    randomBee.setHealth(0.0);

                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            command
                    );

                    Bukkit.getScheduler().runTaskLater(
                            getPlugin(),
                            () -> {
                                bossEntity = bossEntity.getWorld()
                                        .getEntitiesByClass(Bee.class)
                                        .stream()
                                        .filter(b -> b.getScoreboardTags().contains("boss"))
                                        .findFirst()
                                        .orElse(null);
                            },
                            1L
                    );

                    cancel();
                }
            }

        }.runTaskTimer(getPlugin(), 0L, 2L);
    }

    private void drawBeeTransfer(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector());

        double distance = direction.length();
        direction.normalize();

        for (double d = 0; d <= distance; d += 0.25) {
            Location point = from.clone().add(direction.clone().multiply(d));

            point.getWorld().spawnParticle(
                    Particle.DUST,
                    point,
                    1,
                    new Particle.DustOptions(
                            Color.fromRGB(255, 200, 0),
                            1.5f
                    )
            );

            point.getWorld().spawnParticle(
                    Particle.WAX_ON,
                    point,
                    1,
                    0.05, 0.05, 0.05,
                    0
            );
        }
    }

    @EventHandler
    void BeeDamageEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Bee bee)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        Bukkit.getScheduler().runTaskLater(
                getPlugin(),
                () -> {
                    bee.setHasStung(false);
                    bee.setTarget(player);
                },
                5L
        );
    }

    @EventHandler
    void BeeDeathEvent(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.BEE) return;
        if (bossEntity == null) return;

        beeCounter--;
    }

    void spawnBee(Location loc, Team team) {
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {

            List<Bee> offspring = loc.getWorld().getEntitiesByClass(Bee.class)
                    .stream()
                    .filter(b -> b.getScoreboardTags().contains("offspring"))
                    .toList();

            if (offspring.isEmpty()) return;

            for (Bee bee : offspring) {

                Player nearest = null;
                double bestDist = Double.MAX_VALUE;

                for (Player p : loc.getWorld().getPlayers()) {
                    double d = p.getLocation().distanceSquared(bee.getLocation());
                    if (d < bestDist && Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(p.getName()) != team) {
                        bestDist = d;
                        nearest = p;
                    }
                }

                if (nearest != null) {
                    bee.setTarget(nearest);
                }
            }

        }, 1L);

        if (beeCounter >= MAXBEES) return;
        beeCounter++;

        String teamName = (team != null) ? team.getName() : null;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        if (team.getName().equals("EnderEnterprise"))
            x = Math.max(x, 27);
        else
            x = Math.min(x, 35);

        String command =
                "summon bee " + x + " " + y + " " + z +
                        " {Team:\"" + teamName +
                        "\",Tags:[\"mob\",\"offspring\"],CustomName:" +
                        "{\"bold\":true,\"color\":\"yellow\",\"shadow_color\":-2185414,\"text\":\"Offspring Bee\"}," +
                        "attributes:[" +
                        "{id:\"minecraft:attack_damage\",base:12}," +
                        "{id:\"minecraft:follow_range\",base:40}," +
                        "{id:\"minecraft:scale\",base:1}," +
                        "{id:\"minecraft:tempt_range\",base:40}" +
                        "]}";

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @EventHandler
    void QueenBeeTargets(EntityTargetEvent event) {
        if (event.getEntity().getType() != EntityType.BEE) return;

        if (!event.getEntity().getScoreboardTags().contains("boss")) return;

        event.setTarget(null);
    }
}
