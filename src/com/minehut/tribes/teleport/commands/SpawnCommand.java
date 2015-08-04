package com.minehut.tribes.teleport.commands;

import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.teleport.TeleportDestination;
import com.minehut.tribes.teleport.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Created by luke on 7/23/15.
 */
public class SpawnCommand extends Command {
    public SpawnCommand(JavaPlugin plugin) {
        super(plugin, "spawn", Rank.regular);
    }

    @Override
    public boolean call(Player player, ArrayList<String> arrayList) {

        Tribes.instance.teleportManager.teleport(player, new TeleportDestination(Tribes.instance.spawnHandler.getSpawnLocation(), TeleportManager.teleportTime));

        return false;
    }
}
