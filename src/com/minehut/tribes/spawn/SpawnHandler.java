package com.minehut.tribes.spawn;

import com.minehut.tribes.Tribes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Listener;

/**
 * Created by luke on 7/20/15.
 */
public class SpawnHandler implements Listener {
    public World world;
    public Location spawnLocation;

    public SpawnHandler() {
        Tribes.instance.registerListener(this);

        this.world = Bukkit.getServer().getWorlds().get(0);

        for (Villager villager : this.world.getEntitiesByClass(Villager.class)) {
            villager.remove();
        }

        this.spawnLocation = new Location(world, 23.5, 73.5, 38.5);
        this.spawnLocation.setYaw(170);
    }

    public boolean inside(Player player) {
        return player.getWorld() == this.world;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
}
