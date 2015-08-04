package com.minehut.tribes.teleport;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by luke on 7/27/15.
 */
public class TeleportDestination {
    private Player target = null;
    private Location location = null;
    public int totalTicks;
    public int ticksLeft;

    public TeleportDestination(Player target, int ticks) {
        this.target = target;
        this.totalTicks = ticks;
        this.ticksLeft = ticks;
    }

    public TeleportDestination(Location location, int ticks) {
        this.location = location;
        this.totalTicks = ticks;
        this.ticksLeft = ticks;
    }

    public int decrement() {
        this.ticksLeft--;
        return this.ticksLeft;
    }

    public Location getDestination() {
        if (this.target != null) {
            return this.target.getLocation();
        } else {
            return this.location;
        }
    }
}
