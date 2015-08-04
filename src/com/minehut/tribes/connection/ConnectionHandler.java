package com.minehut.tribes.connection;

import com.minehut.tribes.Tribes;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by luke on 7/20/15.
 */
public class ConnectionHandler implements Listener {

    public ConnectionHandler() {
        Tribes.instance.registerListener(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        World world = event.getPlayer().getWorld();

        if (!world.getName().equalsIgnoreCase("wild")) {
            event.getPlayer().teleport(Tribes.instance.spawnHandler.getSpawnLocation());
        }
    }
}
