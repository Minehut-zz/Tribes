package com.minehut.tribes.world;

import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Created by luke on 7/20/15.
 */
public class WorldUtils {

    public static World getWorld(String name) {
        for (World world : Bukkit.getServer().getWorlds()) {
            if (world.getName().equalsIgnoreCase(name)) {
                return world;
            }
        }
        return null;
    }
}
