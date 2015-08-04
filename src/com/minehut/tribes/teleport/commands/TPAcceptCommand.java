package com.minehut.tribes.teleport.commands;

import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.teleport.TeleportDestination;
import com.minehut.tribes.teleport.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by luke on 7/23/15.
 */
public class TPAcceptCommand extends Command {
    public TPACommand tpaCommand;

    public TPAcceptCommand(JavaPlugin plugin, TPACommand tpaCommand) {
        super(plugin, "tpaccept", Rank.regular);
        this.tpaCommand = tpaCommand;
    }

    @Override
    public boolean call(Player player, ArrayList<String> arrayList) {

        Player toRemove = null;

        for (Player key : tpaCommand.requests.keySet()) {
            if (tpaCommand.requests.get(key) == player) {
                toRemove = key;
                F.message(player, C.purple + key.getName() + C.yellow + " will now teleport to you in " + C.green + "5 seconds");
                Tribes.instance.teleportManager.teleport(key, new TeleportDestination(player, TeleportManager.teleportTime));
                break;
            }
        }

        if (toRemove != null) {
            tpaCommand.requests.remove(toRemove);
        }

        return false;
    }
}
