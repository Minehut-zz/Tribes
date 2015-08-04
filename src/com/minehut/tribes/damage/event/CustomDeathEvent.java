package com.minehut.tribes.damage.event;

import com.minehut.core.util.common.event.UtilEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Luke on 10/18/14.
 */
public class CustomDeathEvent extends Event {
    LivingEntity killerEntity;
    LivingEntity deadEntity;
    Player killerPlayer;
    Player deadPlayer;
    String cause;


    public CustomDeathEvent(CustomDamageEvent event) {
        this.killerEntity = event.getDamagerEntity();
        this.deadEntity = event.getHurtEntity();
        this.killerPlayer = event.getDamagerPlayer();
        this.deadPlayer = event.getHurtPlayer();
        this.cause = UtilEvent.getCause(event.getEventCause());

        if (killerPlayer == null && killerEntity != null) {
            return;
        }

//        GamePlayer deadGamePlayer = API.getAPI().getGamePlayer(deadPlayer.getName());
//        if (killerPlayer == null && deadGamePlayer.getLastHit() != null) {
//            this.killerPlayer = Bukkit.getPlayer(deadGamePlayer.getLastHit());
//        }

        //Kill sound effect
//        if (killerPlayer != null) {
//            S.kill(killerPlayer);
//        }

//        deadGamePlayer.setLastHit(null);
    }

    public LivingEntity getKillerEntity() {
        return killerEntity;
    }

    public LivingEntity getDeadEntity() {
        return deadEntity;
    }

    public Player getKillerPlayer() {
        return killerPlayer;
    }

    public Player getDeadPlayer() {
        return deadPlayer;
    }

    public String getCause() {
        return cause;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void setKillerPlayer(Player killerPlayer) {
        this.killerPlayer = killerPlayer;
    }
}
