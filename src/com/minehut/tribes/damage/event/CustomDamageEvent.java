package com.minehut.tribes.damage.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;

/**
 * Created by Luke on 2/4/15.
 */
public class CustomDamageEvent extends Event implements Cancellable {
	private ArrayList<String> cancellers = new ArrayList();
	boolean cancelled;

	private EntityDamageEvent.DamageCause eventCause;
	private double damage;
	String cause;

	private Player damagerPlayer;
	private Player hurtPlayer;

	private TNTPrimed tnt;

	private LivingEntity damagerEntity;
	private LivingEntity hurtEntity;

	private Projectile projectile;

	private boolean ignoreArmor = false;
	private boolean knockback = true;

	public CustomDamageEvent(LivingEntity hurtEntity, LivingEntity damagerEntity, Projectile projectile, EntityDamageEvent.DamageCause eventCause, double damage, boolean knockback, boolean ignoreArmor, String cause)
	{
		this.damage = damage;
		this.eventCause = eventCause;
		this.cause = cause;

		this.tnt = null;

		this.hurtEntity = hurtEntity;
		if ((this.hurtEntity != null) && ((this.hurtEntity instanceof Player))) {
			this.hurtPlayer = ((Player) hurtEntity);
		}

		this.damagerEntity = damagerEntity;
		if ((this.damagerEntity != null) && ((this.damagerEntity instanceof Player))) {
			this.damagerPlayer = ((Player) damagerEntity);
		}

		this.projectile = projectile;

		this.knockback = knockback;
		this.ignoreArmor = ignoreArmor;

		if (this.eventCause == EntityDamageEvent.DamageCause.FALL) {
			this.ignoreArmor = true;
		}
	}

	public CustomDamageEvent(LivingEntity hurtEntity, TNTPrimed tnt, Projectile projectile, EntityDamageEvent.DamageCause eventCause, double damage, boolean knockback, boolean ignoreArmor, String cause)
	{
		this.damage = damage;
		this.eventCause = eventCause;
		this.cause = cause;

		this.hurtEntity = hurtEntity;
		if ((this.hurtEntity != null) && ((this.hurtEntity instanceof Player))) {
			this.hurtPlayer = ((Player) hurtEntity);
		}

		this.damagerEntity = null;
		this.tnt = tnt;

		this.projectile = projectile;

		this.knockback = knockback;
		this.ignoreArmor = ignoreArmor;

		if (this.eventCause == EntityDamageEvent.DamageCause.FALL) {
			this.ignoreArmor = true;
		}
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean b) {
		this.cancelled = b;
	}

	//************************************************************//

	public EntityDamageEvent.DamageCause getEventCause() {
		return eventCause;
	}

	public void setEventCause(EntityDamageEvent.DamageCause eventCause) {
		this.eventCause = eventCause;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public Player getDamagerPlayer() {
		return damagerPlayer;
	}

	public void setDamagerPlayer(Player damagerPlayer) {
		this.damagerPlayer = damagerPlayer;
	}

	public Player getHurtPlayer() {
		return hurtPlayer;
	}

	public void setHurtPlayer(Player hurtPlayer) {
		this.hurtPlayer = hurtPlayer;
	}

	public LivingEntity getDamagerEntity() {
		return damagerEntity;
	}

	public void setDamagerEntity(LivingEntity damagerEntity) {
		this.damagerEntity = damagerEntity;
	}

	public LivingEntity getHurtEntity() {
		return hurtEntity;
	}

	public void setHurtEntity(LivingEntity hurtEntity) {
		this.hurtEntity = hurtEntity;
	}

	public Projectile getProjectile() {
		return projectile;
	}

	public void setProjectile(Projectile projectile) {
		this.projectile = projectile;
	}

	public boolean isIgnoreArmor() {
		return ignoreArmor;
	}

	public void setIgnoreArmor(boolean ignoreArmor) {
		this.ignoreArmor = ignoreArmor;
	}

	public boolean isKnockback() {
		return knockback;
	}

	public void setKnockback(boolean knockback) {
		this.knockback = knockback;
	}

	public TNTPrimed getTnt() {
		return tnt;
	}

	//************************************************************//

	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}

