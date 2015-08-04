package com.minehut.tribes.tribe;

import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.module.Module;
import com.minehut.tribes.tribe.chunk.TribeChunk;
import com.minehut.tribes.tribe.troop.Troop;
import com.minehut.tribes.util.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by luke on 7/19/15.
 */
public class Tribe implements Module {
    public TribeData tribeData;
    public World world;
    public TribeHandler tribeHandler;

    public Tribe(TribeData tribeData, World world) {
        this.tribeData = tribeData;
        this.world = world;
        this.tribeHandler = new TribeHandler(this);
    }

    public ArrayList<Player> getOnlinePlayers() {
        ArrayList<Player> online = new ArrayList<>();

        for (DataPlayer dataPlayer : this.tribeData.dataPlayers) {
            Player player = Bukkit.getServer().getPlayer(dataPlayer.uuid);
            if (player != null) {
                online.add(player);
            }
        }

        return online;
    }

    public boolean isLocatedInsideTribe(Player player) {
        return this.world == player.getWorld();
    }

    public boolean contains(Player player) {
        return this.tribeData.contains(player);
    }

    public boolean belongsToTribe(Entity entity) {
        for (TribeChunk tribeChunk : this.tribeData.tribeChunks) {
            if (tribeChunk.building != null) {
                if (tribeChunk.building.npc != null) {
                    if (tribeChunk.building.npc == entity) {
                        return true;
                    }
                }
            }
        }

        for (Troop troop : this.tribeData.troops) {
            if (troop.entity != null) {
                if (troop.entity == entity) {
                    return true;
                }
            }
        }

        for (Player player : this.getOnlinePlayers()) {
            if (entity == player) {
                return true;
            }
        }

        return false;
    }

    public boolean isInWorld(Location location) {
        return location.getWorld() == this.world;
    }

    @Override
    public void unload() {
        for (Player player : this.world.getEntitiesByClass(Player.class)) {
            player.teleport(Tribes.instance.spawnHandler.getSpawnLocation());
            F.message(player, "Your Tribe was forcibly shutdown.");
        }

        for (Troop troop : this.tribeData.troops) {
            troop.unload();
        }

        WorldUtils.removeHostileEntities(world);

        Bukkit.getServer().unloadWorld(this.world, true);

        this.tribeHandler.unload();
    }
}
