package com.minehut.tribes.tribe.chunk;

import com.minehut.tribes.schematic.Schematic;
import com.minehut.tribes.schematic.SchematicUtils;
import com.minehut.tribes.tribe.TribeData;
import com.minehut.tribes.tribe.building.Building;
import com.minehut.tribes.tribe.building.BuildingType;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.io.File;
import java.util.UUID;

/**
 * Created by luke on 7/20/15.
 */
public class TribeChunk {
    public transient TribeData tribeData;
    public transient Building building;

    public TribeChunkType tribeChunkType;
    public BuildingType buildingType;
    public int buildingLevel;

    public int xCoord;
    public int zCoord;

    public UUID owner = null;

    public TribeChunk(TribeData tribeData, int x, int z, TribeChunkType tribeChunkType, BuildingType buildingType, int buildingLevel, UUID owner) {
        this.tribeData = tribeData;
        this.tribeChunkType = tribeChunkType;
        this.buildingType = buildingType;
        this.buildingLevel = buildingLevel;

        this.owner = owner;

        this.xCoord = x;
        this.zCoord = z;
    }

    public Schematic getSchematic() {
        if (this.tribeChunkType == TribeChunkType.core) {
            return this.building.getSchematic();
        } else {
            return SchematicUtils.loadSchematic(new File("schematics/wild.schematic"));
        }
    }

    public Chunk getChunk(World world) {
        return world.getChunkAt(xCoord, zCoord);
    }

    public enum TribeChunkType {
        core,
        wild
    }

    public boolean hasOwner() {
        return this.owner != null;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}
