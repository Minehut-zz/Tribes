package com.minehut.tribes.tribe.building.buildings;

import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.items.EnchantGlow;
import com.minehut.core.util.common.items.ItemStackFactory;
import com.minehut.core.util.common.particles.ParticleEffect;
import com.minehut.core.util.common.particles.ParticleUtils;
import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.tribe.building.Building;
import com.minehut.tribes.tribe.building.BuildingType;
import com.minehut.tribes.tribe.chunk.TribeChunk;
import com.minehut.tribes.tribe.player.TribePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by luke on 7/22/15.
 */
public class StablesBuilding extends Building {

    public StablesBuilding(TribeChunk tribeChunk, int level) {
        super(BuildingType.stables, level,  tribeChunk);
    }

    @Override
    public void onInventoryClick(Player player, ItemStack itemStack) {
        TribePlayer tribePlayer = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(player);

        if (itemStack.getType() == Material.MONSTER_EGG) {
            Egg egg = getEgg(itemStack);

            if (tribePlayer.hasEnoughCoins(egg.price)) {
                player.closeInventory();
                Tribes.instance.tribeManager.tribePlayerManager.addCoins(player, -egg.price, "Purchase " + C.purple + egg.entityType.toString());

                this.spawnMob(player, egg);
            } else {
                F.warning(player, "You do not have enough gold");
            }
        }
    }

    public Location getMobSpawnLocation(Player player) {
        return new Location(player.getWorld(), -8.5, 64, 22.5);
    }

    public void spawnMob(Player player, Egg egg) {
        Location spawn = this.getMobSpawnLocation(player);

        ParticleEffect.SMOKE_LARGE.display(1f, 1f, 1f, .05f, 40, spawn, 15);
        ParticleEffect.HEART.display(1f, 1f, 1f, .05f, 40, spawn, 15);
        ParticleEffect.REDSTONE.display(1f, 1f, 1f, .05f, 40, spawn, 15);
        ParticleEffect.FIREWORKS_SPARK.display(1f, 1f, 1f, .05f, 40, spawn, 15);

        LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(spawn, egg.entityType);

        spawn.getWorld().playSound(spawn, Sound.IRONGOLEM_HIT, 10, 1);
    }

    @Override
    public void generateInventory() {

        if (this.level >= 1) {
            super.inventory.setItem(20, createShopEgg(Egg.cow));
            super.inventory.setItem(22, createShopEgg(Egg.chicken));
            super.inventory.setItem(24, createShopEgg(Egg.cow));
        }

    }

    public Egg getEgg(ItemStack itemStack) {
        for (Egg egg : Egg.values()) {
            if (itemStack.getDurability() == egg.data) {
                return egg;
            }
        }
        return null;
    }

    public ItemStack createShopEgg(Egg egg) {

        ItemStack itemStack = ItemStackFactory.createSpawnEgg(C.yellow + egg.entityType.toString(), egg.data,
                Arrays.asList(
                        "",
                        C.gray + "Cost: " + C.gold + egg.price,
                        ""
                ));
        EnchantGlow.addGlow(itemStack);
        return itemStack;
    }

    @Override
    public void onClick(Player player) {

    }

    @Override
    public void extraUnload() {

    }

    public enum Egg {
        chicken(100, EntityType.CHICKEN, 93),
        cow(300, EntityType.COW, 92);


        public long price;
        public EntityType entityType;
        public short data;

        Egg(long price, EntityType entityType, int data) {
            this.price = price;
            this.entityType = entityType;
            this.data = (short) data;
        }
    }
}
