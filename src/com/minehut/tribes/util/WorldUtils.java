package com.minehut.tribes.util;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

/**
 * Created by luke on 8/1/15.
 */
public class WorldUtils {
    public static void removeHostileEntities(World world) {
        ArrayList<EntityType> entities = new ArrayList<>();
        entities.add(EntityType.ZOMBIE);
        entities.add(EntityType.PIG_ZOMBIE);
        entities.add(EntityType.SKELETON);
        entities.add(EntityType.WITCH);
        entities.add(EntityType.WITHER);
        entities.add(EntityType.VILLAGER);

        for (LivingEntity livingEntity : world.getEntitiesByClass(LivingEntity.class)) {
            if (entities.contains(livingEntity.getType())) {
                livingEntity.remove();
            }
        }
    }
}
