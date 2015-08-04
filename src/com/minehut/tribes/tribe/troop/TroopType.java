package com.minehut.tribes.tribe.troop;

import org.bukkit.entity.EntityType;

/**
 * Created by luke on 7/21/15.
 */
public enum TroopType {
    zombie("Zombie", EntityType.ZOMBIE, 40, 2),
    archer("Archer", EntityType.SKELETON, 30, 1.8);

    public String name;
    public EntityType entityType;

    public long basePrice;
    public long priceScale;

    public double baseHealth;
    public double healthScale;

    public double baseDamage;
    public double damageScale;

    private TroopType(String name, EntityType entityType, double baseHealth, double healthScale) {
        this.name = name;
        this.entityType = entityType;
        this.baseHealth = baseHealth;
        this.healthScale = healthScale;
    }
}
