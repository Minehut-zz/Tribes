package com.minehut.tribes.damage;

import com.minehut.tribes.Tribes;
import com.minehut.tribes.damage.event.CustomDamageEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by luke on 6/9/15.
 */
public class TntTracker implements Listener {
    private HashMap<String, UUID> tntPlaced = new HashMap<>();

    public int runnableID;
    public List<TNTPrimed> previousTntEntities;

    public TntTracker(Tribes tribes) {
        this.previousTntEntities = new ArrayList<>();
//        this.runnableID = this.scanRunnable();

        tribes.registerListener(this);
    }

    public static UUID getWhoPlaced(Entity tnt) {
        if (tnt.getType().equals(EntityType.PRIMED_TNT)) {
            if (tnt.hasMetadata("source")) {
                return (UUID) tnt.getMetadata("source").get(0).value();
            }
        }
        return null;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.isCancelled()) return;

        if (event.getBlock().getType() == Material.TNT) {
            Location location = event.getBlock().getLocation();
            tntPlaced.put(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ(), event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntity().getType() == EntityType.PRIMED_TNT) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            Location location = event.getEntity().getLocation();

            if (tntPlaced.containsKey(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ())) {
                UUID playerUUID = tntPlaced.get(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                event.getEntity().setMetadata("source", new FixedMetadataValue(Tribes.instance, playerUUID));
                tntPlaced.remove(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCustomDamage(CustomDamageEvent event) {
        if (event.getTnt() != null) {
            Entity tnt = event.getTnt();
            if (tnt.hasMetadata("source")) {
                Player realDamager = Bukkit.getServer().getPlayer(getWhoPlaced(tnt));
                if (realDamager != null) {
                    event.setDamagerEntity(realDamager);
                    event.setDamagerPlayer(realDamager);
                }
            }
        }
    }
//
//    @EventHandler
//    public void onDispenser(BlockDispenseEvent event) {
//        if (event.getItem().getType() == Material.TNT) {
//            event.setCancelled(true);
//
//            TNTPrimed tnt = (TNTPrimed) event.getBlock().getWorld().spawn(event.getBlock().getLocation(), TNTPrimed.class);
//            tnt.setFuseTicks(200);
//            tnt.setVelocity(event.getVelocity());
//            F.debug("spawned custom tnt");
//
//
//        }
//    }

    public static void tagTnt(TNTPrimed tntPrimed, UUID uuid) {
        tntPrimed.setMetadata("source", new FixedMetadataValue(Tribes.instance, uuid));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() != null) {
            if (event.getEntity().getType() == EntityType.PRIMED_TNT) {
                for (Block block : event.blockList()) {
                    if (block.getType() == Material.TNT && getWhoPlaced(event.getEntity()) != null) {
                        Location location = block.getLocation();
                        tntPlaced.put(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ(), getWhoPlaced(event.getEntity()));
                    }
                }

                for (Entity entity : event.getEntity().getNearbyEntities(8, 8, 8)) {
//                    F.debug("Found tnt");
                    if (entity instanceof TNTPrimed) {
                        UUID playerUUID = getWhoPlaced(event.getEntity());
                        if(playerUUID != null) {
                            Dispenser dispenser;
//                            F.debug("found placer: " + Bukkit.getServer().getPlayer(playerUUID));
                            entity.setMetadata("source", new FixedMetadataValue(Tribes.instance, playerUUID));
                        }
                    }
                }
            }
        }
    }
}
