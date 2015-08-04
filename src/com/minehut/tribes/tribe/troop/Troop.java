package com.minehut.tribes.tribe.troop;

import com.minehut.core.util.common.chat.C;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.damage.event.CustomDamageEvent;
import com.minehut.tribes.module.Module;
import com.minehut.tribes.tribe.player.TribePlayer;
import com.minehut.tribes.tribe.Tribe;
import com.minehut.tribes.tribe.TribeData;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Created by luke on 7/21/15.
 */
public class Troop implements Module {
    public transient TribeData tribeData;

    public TroopType troopType;
    public int level;

    public transient Tribe tribe;

    public transient LivingEntity entity;

    public Troop(TroopType troopType, TribeData tribeData, int level) {
        this.troopType = troopType;
        this.tribeData = tribeData;
        this.level = level;
    }

    public void spawn(Tribe tribe) {
        this.tribe = tribe;
        Tribes.instance.registerListener(this);

        Location location = new Location(tribe.world, 7.5, 65, 22.5);
        this.entity = (LivingEntity) tribe.world.spawnEntity(location, this.troopType.entityType);
        this.entity.setCustomName(C.yellow + this.troopType.name + C.red + " [Level " + this.level + "]");
        this.entity.setCustomNameVisible(true);
        this.entity.setRemoveWhenFarAway(false);
    }

    @EventHandler
    public void onTroopCustomDamage(CustomDamageEvent event) {
        if(event.getDamagerEntity() != null) {
            if (this.entity != null && event.getDamagerEntity() == this.entity) {
                if (event.getDamagerPlayer() != null) {
                    TribePlayer tribePlayer = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(event.getDamagerPlayer());
                    if (tribePlayer.tribeData == this.tribeData) {
                        event.setCancelled(true);
                    }
                }

                else if (this.tribe.belongsToTribe(event.getHurtEntity())) {
                    event.setCancelled(true);
                }

                else if (event.getEventCause() == EntityDamageEvent.DamageCause.FIRE) {
                    event.setCancelled(true);
                }

            }
        }
    }

    @EventHandler
    public void onDamage(CustomDamageEvent event) {
        if(this.entity != null && event.getHurtEntity() == this.entity) {
            if (event.getDamagerPlayer() != null) {
                if (this.tribeData.contains(event.getDamagerPlayer())) {
                    event.setCancelled(true);
                    event.getDamagerPlayer().sendMessage(C.yellow + this.troopType.name + "> " + C.white + "Hey, watch it! I'm a " + C.green + "tribal defender!");
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (this.entity != null && event.getEntity() == this.entity) {
            if (event.getTarget() instanceof Player) {
                Player player = (Player) event.getTarget();
                if (this.tribeData.contains(player)) {
                    event.setCancelled(true);
                }
            }

            else if (this.tribe.belongsToTribe(event.getTarget())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        if (this.entity != null && event.getEntity() == this.entity) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (this.entity != null && event.getEntity() == this.entity) {
            event.setCancelled(true);
        }
    }

    @Override
    public void unload() {
        if(this.entity != null) {
            this.entity.remove();
        }
        HandlerList.unregisterAll(this);
    }
}
