package com.minehut.tribes.damage.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Luke on 10/18/14.
 */
public class CustomRespawnEvent extends Event {
    public Player player;
    public Location spawn;


    public CustomRespawnEvent(Player player, Location spawn) {
        this.player = player;
        this.spawn = spawn;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
