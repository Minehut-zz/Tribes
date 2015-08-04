package com.minehut.tribes.tribe.chunk;

import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.module.Module;
import com.minehut.tribes.tribe.Tribe;
import com.minehut.tribes.tribe.player.TribePlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

/**
 * Created by luke on 7/21/15.
 */
public class TribeChunkHandler implements Module {
    public Tribe tribe;
    public ArrayList<TribeChunk> tribeChunks;
    public World world;


    public TribeChunkHandler(Tribe tribe) {
        this.tribe = tribe;
        this.tribeChunks = tribe.tribeData.tribeChunks;
        this.world = tribe.world;

        Tribes.instance.registerListener(this);
    }

    public TribeChunk getTribeChunk(Chunk chunk) {
        for (TribeChunk tribeChunk : this.tribeChunks) {
            if (tribeChunk.xCoord == chunk.getX() && tribeChunk.zCoord == chunk.getZ()) {
                return tribeChunk;
            }
        }
        return null;
    }

    public boolean attemptBuild(Player player, Location location) {
        if (this.tribe.isInWorld(location)) {
            TribePlayer tribePlayer = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(player);
            if (tribePlayer.tribeData != null && tribePlayer.tribeData == tribe.tribeData) {
                TribeChunk tribeChunk = this.getTribeChunk(location.getChunk());

                if (tribeChunk != null) {
                    if (tribeChunk.tribeChunkType == TribeChunk.TribeChunkType.wild) {

                        if (tribePlayer.tribeData.hasElder(player)) {
                            return true;
                        }

                        if (tribeChunk.hasOwner()) {
                            if (tribeChunk.owner.equals(player.getUniqueId())) {
                                return true;
                            }
                        }

                        F.warning(player, "A " + C.yellow + "Tribal Elder " + C.gray + "must grant you permission to build here");
                    } else {
                        F.warning(player, "You are not allowed to build on " + C.yellow + "Tribal Core Plots");
                    }

                } else {
                    F.warning(player, "You can only build in your own tribe");
                }
            } else {
                F.warning(player, "You can only build in plots your tribe owns");

            }
            return false;
        }
        return true;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!this.attemptBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!this.attemptBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if(event.getEntity().getWorld() == this.tribe.world) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() != null) {
            if (!this.attemptBuild(event.getPlayer(), event.getClickedBlock().getLocation())) {
                event.setCancelled(true);
            }

            Chunk chunk = event.getClickedBlock().getChunk();
            F.message(event.getPlayer(), "Chunk: " + C.green + chunk.getX() + "x" + chunk.getZ());
        }
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }
}
