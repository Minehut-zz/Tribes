package com.minehut.tribes.teleport.commands;

import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.tribe.player.TribePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by luke on 7/23/15.
 */
public class TPACommand extends Command {
    public TPAcceptCommand tpAcceptCommand;
    public HashMap<Player, Player> requests;

    public TPACommand(JavaPlugin plugin) {
        super(plugin, "tpa", Rank.regular);

        this.requests = new HashMap<>();

        this.tpAcceptCommand = new TPAcceptCommand(plugin, this);
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        if (args != null && args.size() == 1) {

            Player to = Bukkit.getServer().getPlayer(args.get(0));
            if (to != null) {
                if(to != player) {
                    TribePlayer tribePlayer = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(player);
                    TribePlayer tribeTo = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(to);

                    boolean check = false;

                    if (tribePlayer.tribeData != null && tribeTo.tribeData != null) {
                        if (tribePlayer.tribeData == tribeTo.tribeData) {
                            check = true;
                        }
                    } else {
                        if (tribePlayer.tribeData == null && tribeTo.tribeData == null) {
                            check = true;
                        }
                    }

                    if(check) {
                        this.requests.put(player, to);

                        F.message(to, C.purple + player.getName() + C.yellow + " has requested to teleport to you.");
                        F.message(to, "Accept by typing " + C.aqua + "/tpaccept");

                        F.message(player, "You have sent a /tpa request to " + C.purple + to.getName());
                    } else {
                        F.warning(player, "You must be in the same tribe as " + C.purple + to.getName() + C.gray + " to /tpa");
                    }

                } else {
                    F.warning(player, "You cannot /tpa to yourself");
                }
            } else {
                F.warning(player, C.red + args.get(0) + C.gray + " is not online");
            }

        } else {
            F.warning(player, "/tpa (player)");
        }

        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (this.requests.containsKey(event.getPlayer())) {
            this.requests.remove(event.getPlayer());
            return;
        }

        if (this.requests.containsValue(event.getPlayer())) {
            for (Player player : this.requests.keySet()) {
                if (this.requests.get(player) == event.getPlayer()) {
                    this.requests.remove(player);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (this.requests.containsKey(event.getPlayer())) {
            this.requests.remove(event.getPlayer());
            return;
        }

        if (this.requests.containsValue(event.getPlayer())) {
            for (Player player : this.requests.keySet()) {
                if (this.requests.get(player) == event.getPlayer()) {
                    this.requests.remove(player);
                    return;
                }
            }
        }
    }
}
