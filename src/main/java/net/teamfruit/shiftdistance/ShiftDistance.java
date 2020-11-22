package net.teamfruit.shiftdistance;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ShiftDistance extends JavaPlugin implements Listener {
    private double radius = 10;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Map<Player, BukkitTask> blowingTasks = new HashMap<>();

    @EventHandler
    public void onShift(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking())
            blowingTasks.computeIfAbsent(player, k -> new BukkitRunnable() {
                @Override
                public void run() {
                    Location loc = player.getLocation();
                    player.getWorld().getNearbyEntities(player.getLocation(),
                            radius, radius, radius,
                            e -> e.getLocation().distance(loc) < radius)
                            .forEach(entity -> entity.setVelocity(
                                    entity.getLocation().subtract(loc).toVector().normalize()));

                    if (player.isDead() || !player.isOnline() || !player.isSneaking())
                        cancel();
                }
            }.runTaskTimer(ShiftDistance.this, 0, 1));
        else
            Optional.ofNullable(blowingTasks.remove(player)).ifPresent(BukkitTask::cancel);
    }

}
