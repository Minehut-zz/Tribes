package com.minehut.tribes.wild.commands;

import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.teleport.TeleportDestination;
import com.minehut.tribes.teleport.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by luke on 7/23/15.
 */
public class WildCommand extends Command {
    public WildCommand(JavaPlugin plugin) {
        super(plugin, "wild", Arrays.asList("wilderness"), Rank.regular);
    }

    @Override
    public boolean call(Player player, ArrayList<String> arrayList) {

        if (Tribes.instance.wildManager.world == null) {
            F.log("Wild world was null :(");
        }

        Tribes.instance.teleportManager.teleport(player, new TeleportDestination(Tribes.instance.wildManager.world.getSpawnLocation(), TeleportManager.teleportTime));

        return false;
    }
}
