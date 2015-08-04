package com.minehut.tribes.damage;

import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.damage.event.CustomDamageEvent;
import com.minehut.tribes.damage.event.CustomDeathEvent;
import com.minehut.tribes.damage.event.CustomRespawnEvent;
import com.minehut.tribes.tribe.player.TribePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Created by Luke on 2/6/15.
 */
public class DamageManager implements Listener {
	public TntTracker tntTracker;

	public DamageManager(Tribes tribes) {
		this.tntTracker = new TntTracker(tribes);

		Tribes.instance.registerListener(this);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVanillaDamage(EntityDamageEvent event) {
		if(event.isCancelled()) return;

		// Make sure its a living npc
		if (!(event.getEntity() instanceof LivingEntity)) {
			return;
		}

		LivingEntity hurtEntity = (LivingEntity) event.getEntity();
		Projectile projectile = getProjectile(event);
		EntityDamageEvent.DamageCause damageCause = event.getCause();
		double damage = event.getFinalDamage();
		LivingEntity damagerEntity = null;

		TNTPrimed tnt = null;

		// Check to see if there is a damager
		if (event instanceof EntityDamageByEntityEvent) {

			if (((EntityDamageByEntityEvent) event).getDamager() instanceof TNTPrimed) {
				tnt = (TNTPrimed) ((EntityDamageByEntityEvent) event).getDamager();
			}

			else if (((EntityDamageByEntityEvent) event).getDamager() != null
					&& !(((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile)) {
				damagerEntity = (LivingEntity) ((EntityDamageByEntityEvent) event).getDamager();
			}
		}

		// Get the arrow shooter
		if (projectile != null) {
			if (projectile.getShooter() instanceof LivingEntity) {
				damagerEntity = (LivingEntity) projectile.getShooter();
			}
		}

		CustomDamageEvent damageEvent;
		if(tnt == null) {
			damageEvent = new CustomDamageEvent(hurtEntity, damagerEntity, projectile, damageCause, damage, true, false, null);
		} else {
			damageEvent = new CustomDamageEvent(hurtEntity, tnt, projectile, damageCause, damage, true, false, null);
		}

		Tribes.instance.getServer().getPluginManager().callEvent(damageEvent);

		if (damageEvent.isCancelled()) {
			/* Disable knockback */
			event.setDamage(0);
			event.setCancelled(true);
		} else {
			/* Keep knockback */
			event.setDamage(0);
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		CustomRespawnEvent respawn = new CustomRespawnEvent(event.getPlayer(), Tribes.instance.spawnHandler.getSpawnLocation());
		Tribes.instance.getServer().getPluginManager().callEvent(respawn);
		event.getPlayer().teleport(respawn.getSpawn());
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onCustomDamage(CustomDamageEvent event) {
		if(event.isCancelled()) return;

		if (event.getHurtPlayer() != null && event.getDamagerPlayer() != null) {
			if (!Tribes.instance.tribeManager.inSameTribe(event.getHurtPlayer(), event.getDamagerPlayer())) {
				Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(event.getHurtPlayer()).lastDamagedFrom = event.getDamagerPlayer().getUniqueId();
			}
		}
	}

	@EventHandler
	public void onCustomDeath(CustomDeathEvent event) {
		if (event.getDeadPlayer() != null && event.getKillerPlayer() == null) {
			TribePlayer tribePlayer = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(event.getDeadPlayer());
			UUID lastHit = tribePlayer.lastDamagedFrom;

			if (lastHit != null) {
				Player killer = Bukkit.getServer().getPlayer(lastHit);
				if (killer != null) {
					event.setKillerPlayer(killer);
				}
				tribePlayer.lastDamagedFrom = null;
			}
		}
	}

	/* No arrows on ground */
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(!(event.getEntity() instanceof FishHook)) {
			event.getEntity().remove();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void cancelDeath(CustomDamageEvent event) {
		if(event.isCancelled()) return;

		if ((((Damageable)event.getHurtEntity()).getHealth() - event.getDamage()) <= 0D) {
			if (event.getHurtPlayer() == null && !(event.getHurtEntity() instanceof Player)) {
				event.getHurtEntity().setHealth(0d); //Instantly kill npc
				return;
			}

			Tribes.instance.getServer().getPluginManager().callEvent(new CustomDeathEvent(event));
			event.setCancelled(true);

			CustomRespawnEvent respawn = new CustomRespawnEvent(event.getHurtPlayer(), Tribes.instance.spawnHandler.getSpawnLocation());
			Tribes.instance.getServer().getPluginManager().callEvent(respawn);
			event.getHurtPlayer().teleport(respawn.getSpawn());
		} else {
			damage(event);
		}
	}

	void damage(CustomDamageEvent event) {

//		if (event.getDamagerPlayer() != null && event.getHurtPlayer() != null) {
//			API.getAPI().getGamePlayer(event.getHurtPlayer().getName()).setLastHit(event.getDamagerPlayer().getName());
//		} todo: hit tracking.

		event.getHurtEntity().damage(event.getDamage());

		//Flame
		if (event.getProjectile() != null) {
			if (event.getProjectile().getFireTicks() != 0) {
//				Bukkit.broadcastMessage("[Before] fire ticks: " + Integer.toString(event.getHurtEntity().getFireTicks()));

				event.getHurtEntity().setFireTicks(40); //0.7 seconds

//				Bukkit.broadcastMessage("[After] fire ticks: " + Integer.toString(event.getHurtEntity().getFireTicks()));
			}
		}

		//Knockback
//		if(event.getDamagerEntity() != null) {
//			Vector trajectory = getTrajectory2d(event.getDamagerEntity(), event.getHurtEntity());
//			trajectory.multiply(0.10D * event.getDamage());
//			trajectory.setY(Math.abs(trajectory.getY()));
//
//			velocity(event.getHurtEntity(),
//					trajectory, 0.06D + trajectory.length() * 0.8D, false, 0.0D, Math.abs(0.03D * event.getDamage()), 0.2D + 0.04D * event.getDamage(), true);
//		}

		//Remove projectile
		if (event.getProjectile() != null) {
			event.getProjectile().remove();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onArrowHitSoundEffect(CustomDamageEvent event) {
		if(event.isCancelled()) return;

		//Arrow hit sound effect
		if (event.getProjectile() != null) {
			if (event.getDamagerPlayer() != null) {
				if (event.getDamagerPlayer() != event.getHurtEntity()) {
					S.arrowHit(event.getDamagerPlayer());
				}
			}
		}
	}

	public static Vector getTrajectory2d(Entity from, Entity to) {

		return getTrajectory2d(from.getLocation().toVector(), to.getLocation().toVector());
	}

	public static Vector getTrajectory2d(Location from, Location to)
	{
		return getTrajectory2d(from.toVector(), to.toVector());
	}

	public static Vector getTrajectory2d(Vector from, Vector to)
	{
		return to.subtract(from).setY(0).normalize();
	}

	public static void velocity(Entity ent, double str, double yAdd, double yMax, boolean groundBoost)
	{
		velocity(ent, ent.getLocation().getDirection(), str, false, 0.0D, yAdd, yMax, groundBoost);
	}

	public static void velocity(Entity ent, Vector vec, double str, boolean ySet, double yBase, double yAdd, double yMax, boolean groundBoost)
	{
		if ((Double.isNaN(vec.getX())) || (Double.isNaN(vec.getY())) || (Double.isNaN(vec.getZ())) || (vec.length() == 0.0D)) {
			return;
		}

		if (ySet) {
			vec.setY(yBase);
		}

		vec.normalize();
		vec.multiply(str);

		vec.setY(vec.getY() + yAdd);

		if (vec.getY() > yMax) {
			vec.setY(yMax);
		}
		if ((groundBoost) &&
				(ent.isOnGround())) {
			vec.setY(vec.getY() + 0.03D);
		}

		ent.setFallDistance(0.0F);
		ent.setVelocity(vec);
	}

	Projectile getProjectile(EntityDamageEvent event) {
		if (!(event instanceof EntityDamageByEntityEvent)) {
			return null;
		}
		EntityDamageByEntityEvent eventEE = (EntityDamageByEntityEvent)event;

		if ((eventEE.getDamager() instanceof Projectile)) {
			return (Projectile) eventEE.getDamager();
		}
		return null;
	}

}
