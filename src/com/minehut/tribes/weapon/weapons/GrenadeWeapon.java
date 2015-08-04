package com.minehut.tribes.weapon.weapons;

import com.minehut.core.util.common.items.ItemStackFactory;
import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.damage.TntTracker;
import com.minehut.tribes.damage.event.CustomDamageEvent;
import com.minehut.tribes.weapon.Weapon;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by luke on 7/30/15.
 */
public class GrenadeWeapon extends Weapon{
    public ArrayList<TNTPrimed> spawnedTNT;
    public ArrayList<FallingBlock> fallingBlocks;
    public double damageScale = 1;
    public double strength;
    public boolean regen;
    public boolean spawnBarriers;
    public int explodeRadius = -1;
    public int fuse = -1;

    public GrenadeWeapon() {
        super("Grenade", Material.TNT, 40, ActivateMode.BOTH, WeaponType.RANGED, Rarity.LEGENDARY);
        this.spawnedTNT = new ArrayList<>();
        this.fallingBlocks = new ArrayList<>();
        this.regen = true;
        this.spawnBarriers = true;
        this.strength = 1.3;
    }

    public void setExplodeRadius(int explodeRadius) {
        this.explodeRadius = explodeRadius;
    }

    public void setFuse(int fuse) {
        this.fuse = fuse;
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {

    }

    @Override
    public void onRightClick(Player player) {
    }

    @Override
    public void onRightClickAir(Player player) {

    }

    @Override
    public void onHitPlayer(Player player, Player hurt) {

    }

    @Override
    public void onHitEntity(Player player, LivingEntity hurt) {

    }

    @Override
    public void offCooldown(Player player) {

    }

    @Override
    public void onLeftClickAir(Player player) {

    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if(this.damageScale != 1) {
            if (event.getDamagerEntity() != null) {
                if (event.getDamagerEntity() instanceof TNTPrimed) {
                    if (this.spawnedTNT.contains(event.getDamagerEntity())) {
                        event.setDamage(event.getDamage() * this.damageScale);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) return;

        if (event.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            if (this.spawnedTNT.contains(tnt)) {
                this.spawnedTNT.remove(tnt);
                this.handleCustomExplosion(event);

                if (this.explodeRadius == -1) {
                    List<Block> toRemove = new ArrayList<>();
                    for (Block block : event.blockList()) {
                        if (block.getLocation().distance(event.getLocation()) > this.explodeRadius) {
                            toRemove.add(block);
                        }
                    }

                    for (Block block : toRemove) {
                        event.blockList().remove(block);
                    }
                }
            }
        }
    }

    public void handleCustomExplosion(EntityExplodeEvent e) {
        if ((e.getEntity().getType() == EntityType.PRIMED_TNT) &&
                (!e.blockList().isEmpty()))
        {
            final List<BlockState> blocks = new ArrayList();
            for (Block b : e.blockList()) {
                if ((b.getType() != Material.AIR) && (b.getType() != Material.BARRIER) && (b.getType() != Material.BEDROCK) &&
                        (!blocks.contains(b.getState())))
                {
                    double rangeMinX = -1.0D;
                    double rangeMaxX = 1.0D;
                    double rangeMinY = 0.5D;
                    double rangeMaxY = 1.0D;
                    double rangeMinZ = -1.0D;
                    double rangeMaxZ = 1.0D;
                    Random r = new Random();
                    double randomValueX = rangeMinX + (rangeMaxX - rangeMinX) * r.nextDouble();
                    double randomValueY = rangeMinY + (rangeMaxY - rangeMinY) * r.nextDouble();
                    double randomValueZ = rangeMinZ + (rangeMaxZ - rangeMinZ) * r.nextDouble();

                    blocks.add(b.getState());
                    FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData());
                    fb.setDropItem(false);
                    fb.setVelocity(new Vector(randomValueX, randomValueY, randomValueZ));
                    this.fallingBlocks.add(fb);

                    if(this.spawnBarriers) {
                        b.setType(Material.BARRIER);
                    } else {
                        b.setType(Material.AIR);
                    }
                }
            }
            if(this.regen) {
                new BukkitRunnable() {
                    int i = 17;

                    public void run() {
                        if (this.i > 0) {
                            this.i -= 1;
                        } else {
                            regen(blocks, 6);
                            cancel();
                        }
                    }
                }.runTaskTimer(Tribes.instance, 5L, 5L);

                e.blockList().clear();
            }
        }
    }

    public void regen(final List<BlockState> blocks, int speed) {
        new BukkitRunnable() {
            int i = -1;

            public void run() {

                if (this.i != blocks.size() - 1) {
                    this.i += 1;
                    BlockState bs = (BlockState) blocks.get(this.i);
                    bs.getBlock().setType(bs.getType());
                    bs.getBlock().setData(bs.getData().getData());
                    bs.getBlock().getWorld().playEffect(bs.getLocation(), Effect.STEP_SOUND, bs.getBlock().getType());
                } else {
                    for (BlockState bs : blocks) {
                        bs.getBlock().setType(bs.getType());
                        bs.getBlock().setData(bs.getData().getData());
                    }
                    blocks.clear();
                    cancel();
                }
            }
        }.runTaskTimer(Tribes.instance, speed, speed);
    }

    @EventHandler
    public void blockForm(EntityChangeBlockEvent e)
    {
        if ((e.getEntity() instanceof FallingBlock)) {
            FallingBlock block = (FallingBlock)e.getEntity();
            if(this.fallingBlocks.contains(block)) {
                this.fallingBlocks.remove(block);
                e.getBlock().getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getBlockId());
                e.getEntity().remove();
            }
        }
        e.setCancelled(true);
    }

    @Override
    public void onLeftClick(Player player) {
        TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getLocation(), EntityType.PRIMED_TNT);
        tnt.setVelocity(player.getLocation().getDirection().multiply(this.strength));

//        tnt.setMetadata("source", new FixedMetadataValue(MGM.getInstance(), player.getUniqueId()));
        TntTracker.tagTnt(tnt, player.getUniqueId());
        this.spawnedTNT.add(tnt);

        if (this.fuse > 0) {
            tnt.setFuseTicks(this.fuse);
        }

        S.pop(player);

        super.putOnCooldown(player);
    }

    @Override
    public void onLeftClickBlock(Player player, Block block) {

    }

    @Override
    public void onRightClickBlock(Player player, Block block) {

    }

    @Override
    public void onRightClickPlayer(Player player, Player target) {

    }

    @Override
    public void extraUnload() {

    }
}
