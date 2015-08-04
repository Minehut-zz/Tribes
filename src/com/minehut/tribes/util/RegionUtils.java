package com.minehut.tribes.util;

import com.minehut.tribes.Tribes;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

/**
 * Created by luke on 7/23/15.
 */
public class RegionUtils {
    public static boolean isInSafezone(Player player) {

        for(ProtectedRegion r : WGBukkit.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation())) {
            if (r.getFlag(DefaultFlag.PVP) != null) {
                if (r.getFlag(DefaultFlag.PVP).equals(StateFlag.State.DENY)) {
                    return true;
                }
            }
        }
        return false;
    }
}
