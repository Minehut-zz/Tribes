package com.minehut.tribes.tribe.building.buildings;

import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.tribe.building.Building;
import com.minehut.tribes.tribe.building.BuildingType;
import com.minehut.tribes.tribe.chunk.TribeChunk;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by luke on 7/22/15.
 */
public class BarracksBuilding extends Building {

    public BarracksBuilding(TribeChunk tribeChunk, int level) {
        super(BuildingType.barracks, level,  tribeChunk);
    }

    @Override
    public void onInventoryClick(Player player, ItemStack itemStack) {

    }

    @Override
    public void generateInventory() {

    }

    @Override
    public void onClick(Player player) {

    }

    @Override
    public void extraUnload() {

    }
}
