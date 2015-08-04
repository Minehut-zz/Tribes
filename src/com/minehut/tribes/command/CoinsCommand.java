package com.minehut.tribes.command;

import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.tribe.player.TribePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by luke on 7/23/15.
 */
public class CoinsCommand extends Command {
    public CoinsCommand(JavaPlugin plugin) {
        super(plugin, "coins", Arrays.asList("money", "bal", "balance"), Rank.regular);
    }

    @Override
    public boolean call(Player player, ArrayList<String> arrayList) {

        Tribes.instance.tribeManager.tribePlayerManager.addCoins(player, 100, "Hack.exe");

        TribePlayer tribePlayer = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(player);
        F.message(player, "You have " + C.green + tribePlayer.coins + " coins");

        return false;
    }
}
