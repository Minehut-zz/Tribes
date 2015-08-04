package com.minehut.tribes.teleport;

import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.particles.*;
import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.teleport.commands.SpawnCommand;
import com.minehut.tribes.teleport.commands.TPACommand;
import com.minehut.tribes.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by luke on 7/27/15.
 */
public class TeleportManager implements Listener {
    HashMap<Player, TeleportDestination> teleporting;

    public static int teleportTime = 80; //4 seconds

    public TeleportManager(Tribes tribes) {
        this.teleporting = new HashMap<>();

        this.countdown();

        tribes.registerListener(this);

        new SpawnCommand(tribes);
        new TPACommand(tribes);
    }

    public void teleport(Player player, TeleportDestination teleportDestination) {
        if (player != null) {
            this.teleporting.put(player, teleportDestination);

            F.message(player, C.red + C.bold + "DON'T MOVE! " + C.yellow + "Teleporting in " + C.purple + TimeUtils.format(teleportDestination.totalTicks));
        }
    }

    public void countdown() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Tribes.instance, new Runnable() {
            @Override
            public void run() {
                if (!teleporting.isEmpty()) {
                    ArrayList<Player> toRemove = new ArrayList<Player>();

                    for (Player player : teleporting.keySet()) {
                        if (player != null) {
                            TeleportDestination teleportDestination = teleporting.get(player);
                            int ticks = teleportDestination.decrement();
                            if (ticks <= 0) {
                                toRemove.add(player);

                                player.teleport(teleportDestination.getDestination());
                                S.pop(player);
                            } else {
                                ParticleUtils.circle(player.getEyeLocation().add(0, 2.5f, 0), ParticleEffect.SMOKE_NORMAL, .55f, 8);

                                float height = (((float) (teleportDestination.totalTicks - teleportDestination.ticksLeft) / (float) teleportDestination.totalTicks) * 2f) + .5f;
                                ParticleUtils.circle(player.getEyeLocation().add(0, height, 0), ParticleEffect.VILLAGER_HAPPY, .55f, 8);
                            }
                        }
                    }

                    for (Player player : toRemove) {
                        teleporting.remove(player);
                    }
                }
            }
        }, 0L, 0L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.teleporting.containsKey(event.getPlayer())) {
            Location from = event.getFrom();

            if (from.getZ() != event.getTo().getZ() && from.getX() != event.getTo().getX()) {
                this.teleporting.remove(event.getPlayer());
                F.warning(event.getPlayer(), "Teleportation " + C.red + "cancelled" + C.gray + " because you moved!");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (this.teleporting.containsKey(event.getPlayer())) {
            this.teleporting.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerKickEvent event) {
        if (this.teleporting.containsKey(event.getPlayer())) {
            this.teleporting.remove(event.getPlayer());
        }
    }
}
