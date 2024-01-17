package me.leandecks.jumprandomizer;

import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class JumpRandomizer extends JavaPlugin implements Listener, CommandExecutor {

    private Boolean challengeStarted = Boolean.FALSE;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage("moin moin");
    }

    private final Set<UUID> prevPlayersOnGround = Sets.newHashSet();

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (challengeStarted) {
            Player player = e.getPlayer();
            if (player.getVelocity().getY() > 0) {
                double jumpVelocity = (double) 0.42F;
                if (player.hasPotionEffect(PotionEffectType.JUMP)) {
                    jumpVelocity += (double) ((float) (player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() + 1) * 0.1F);
                }
                if (e.getPlayer().getLocation().getBlock().getType() != Material.LADDER && prevPlayersOnGround.contains(player.getUniqueId())) {
                    if (!player.isOnGround() && Double.compare(player.getVelocity().getY(), jumpVelocity) == 0) {

                        EntityType[] entities = EntityType.values();
                        int randomNumber = (int) (Math.random() * entities.length);
                        EntityType randomEntity = entities[randomNumber];

                        Objects.requireNonNull(player.getLocation().getWorld()).spawnEntity(player.getLocation(), randomEntity);

                    }
                }
            }
            if (player.isOnGround()) {
                prevPlayersOnGround.add(player.getUniqueId());
            } else {
                prevPlayersOnGround.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent e) {
        if (e.getEntityType() == EntityType.ENDER_DRAGON) {
            challengeStarted = Boolean.FALSE;
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "timer pause");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "gamemode spectator @a");
            Bukkit.broadcastMessage(ChatColor.AQUA + "> The Ender Dragon was defeated. Challenge completed!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            switch (command.getName()) {
                case "jumprandomizer":
                    if (challengeStarted) {
                        challengeStarted = Boolean.FALSE;
                        Bukkit.broadcastMessage(ChatColor.AQUA + "> Challenge stopped");
                    } else {
                        challengeStarted = Boolean.TRUE;
                        Bukkit.broadcastMessage(ChatColor.AQUA + "> Challenge started");
                    }
                    break;
            }
        }
        return true;
    }

}

