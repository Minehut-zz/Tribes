package com.minehut.tribes.tribe.building;

import com.minehut.tribes.util.HashUtils;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by luke on 7/21/15.
 */
public enum BuildingType {
    townhall("Town Hall", 3, 2000L, 7000L, Villager.Profession.PRIEST, 11.5, 64, 11.5, 16, 63, 16),
    barracks("Barracks", 3, 1000L, 5000L, Villager.Profession.BUTCHER, 23.5, 64, 24.5, 16, 63, 16),
    blacksmith("Blacksmith", 3, 1000L, 5000L, Villager.Profession.BLACKSMITH, -8.5, 64, -10.5, -15, 63, -15),
    stables("Stables", 3, 1000L, 5000L, Villager.Profession.FARMER, -7.5, 64, 19.0, -16, 63, 16);

    public String name;
    public int maxLevel;
    public Villager.Profession profession;

    public long upgrade2, upgrade3;

    public double npcX;
    public double npcY;
    public double npcZ;

    public int schemX;
    public int schemY;
    public int schemZ;

    private BuildingType(String name, int maxLevel, long upgrade2, long upgrade3, Villager.Profession profession, double npcX, double npcY, double npcZ, int schemX, int schemY, int schemZ) {
        this.name = name;
        this.profession = profession;

        this.maxLevel = maxLevel;

        this.upgrade2 = upgrade2;
        this.upgrade3 = upgrade3;

        this.npcX = npcX;
        this.npcY = npcY;
        this.npcZ = npcZ;

        this.schemX = schemX;
        this.schemY = schemY;
        this.schemZ = schemZ;
    }

    public String schematicFormat(int level) {
        String s = this.name.toLowerCase() + " " + level;
        s = s.replace(" ", "_");
        return s;
    }
}
