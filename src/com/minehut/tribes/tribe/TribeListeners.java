package com.minehut.tribes.tribe;

import com.minehut.tribes.Tribes;
import com.minehut.tribes.damage.event.CustomDamageEvent;
import com.minehut.tribes.tribe.player.TribePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by luke on 7/24/15.
 */
public class TribeListeners implements Listener {

    public TribeListeners() {
        Tribes.instance.registerListener(this);
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if (event.getDamagerPlayer() != null && event.getHurtPlayer() != null) {
            TribePlayer tribePlayer = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(event.getDamagerPlayer());
            if (tribePlayer.tribeData != null) {
                if (tribePlayer.tribeData.contains(event.getHurtPlayer())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
