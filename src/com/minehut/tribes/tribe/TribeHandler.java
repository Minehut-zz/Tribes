package com.minehut.tribes.tribe;

import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.module.Module;
import com.minehut.tribes.tribe.chunk.TribeChunk;
import com.minehut.tribes.tribe.chunk.TribeChunkHandler;
import com.minehut.tribes.tribe.troop.Troop;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * Created by luke on 7/19/15.
 */
public class TribeHandler implements Module {
    public Tribe tribe;
    public TribeChunkHandler tribeChunkHandler;
    public boolean alreadyJoined = false;

    public TribeHandler(Tribe tribe) {
        this.tribe = tribe;
        this.tribeChunkHandler = new TribeChunkHandler(tribe);

        Tribes.instance.registerListener(this);
    }

    @EventHandler
    public void onJoin(PlayerChangedWorldEvent event) {
        if (this.tribe.isLocatedInsideTribe(event.getPlayer())) {
            if (this.tribe.contains(event.getPlayer())) {
                F.message(event.getPlayer(), "Welcome to your tribe!");

                if (!this.alreadyJoined) {
                    this.alreadyJoined = true;

                    for (Troop troop : this.tribe.tribeData.troops) {
                        troop.spawn(this.tribe);
                    }

                    for (TribeChunk tribeChunk : this.tribe.tribeData.tribeChunks) {
                        if(tribeChunk.building != null) {
                            tribeChunk.building.spawn(tribe);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void unload() {
        this.tribeChunkHandler.unload();
        HandlerList.unregisterAll(this);
    }
}
