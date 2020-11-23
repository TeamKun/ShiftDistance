package net.teamfruit.shiftdistance;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

public final class ShiftDistance extends JavaPlugin implements Listener {
    private double radius = 0;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 0)
            return false;

        radius = NumberUtils.toDouble(args[0]);
        sender.sendMessage(new ComponentBuilder()
                .append("[かめすたプラグイン] ").color(ChatColor.LIGHT_PURPLE)
                .append("おならの強さ(半径)を ").color(ChatColor.GREEN)
                .append(String.valueOf(radius)).color(ChatColor.WHITE)
                .append(" にした").color(ChatColor.GREEN)
                .create()
        );
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return Arrays.asList("<radius>", "3.5");
        return Collections.emptyList();
    }

    public Map<Player, BukkitTask> blowingTasks = new HashMap<>();

    @EventHandler
    public void onShift(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            if (radius > 0 && player.hasPermission("shiftdistance.use"))
                blowingTasks.computeIfAbsent(player, k -> new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!(radius > 0)) {
                            cancel();
                            return;
                        }
                        Location loc = player.getLocation();
                        BoundingBox box = BoundingBox.of(loc.toVector(), radius, radius, radius);
                        repelEntitiesInAABBFromPoint(player.getWorld(), box, player.getLocation().toVector(), player);
                        if (player.isDead() || !player.isOnline() || !player.isSneaking())
                            cancel();
                    }
                }.runTaskTimer(ShiftDistance.this, 0, 1));
        } else
            Optional.ofNullable(blowingTasks.remove(player)).ifPresent(BukkitTask::cancel);
    }

    /**
     * Repels projectiles and mobs in the given AABB away from a given point
     */
    public static void repelEntitiesInAABBFromPoint(World world, BoundingBox effectBounds, Vector p, Entity exclude) {
        world.getNearbyEntities(effectBounds).stream()
                .filter(ent -> !(ent instanceof Arrow) || !ent.isOnGround())
                .filter(ent -> !ent.equals(exclude))
                .forEach(ent -> {
                    Vector t = ent.getLocation().toVector();
                    double distance = p.distance(t) + 0.1D;
                    Vector r = t.clone().subtract(p);
                    ent.setVelocity(ent.getVelocity().add(r.multiply(1.0D / 1.5D / distance)));
                });
    }

}
