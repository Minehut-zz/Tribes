package com.minehut.tribes.util;

import com.minehut.core.Core;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by luke on 7/16/15.
 */
public class CheatUtils {
    public static void exemptFlight(Player player) {
        NCPExemptionManager.exemptPermanently(player, CheckType.MOVING_SURVIVALFLY);
    }

    public static void unexemptFlight(Player player, long delay) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Core.getInstance(), new Runnable() {
            @Override
            public void run() {
                NCPExemptionManager.unexempt(player, CheckType.MOVING_SURVIVALFLY);
            }
        }, delay);
    }
}
