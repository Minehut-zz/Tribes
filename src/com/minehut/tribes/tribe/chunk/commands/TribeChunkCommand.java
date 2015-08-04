package com.minehut.tribes.tribe.chunk.commands;

import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.sound.S;
import com.minehut.core.util.common.uuid.NameFetcher;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.tribe.Tribe;
import com.minehut.tribes.tribe.chunk.TribeChunk;
import com.minehut.tribes.tribe.player.TribePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by luke on 7/23/15.
 */
public class TribeChunkCommand extends Command {
    public TribeChunkCommand(JavaPlugin plugin) {
        super(plugin, "chunk", Arrays.asList("plot"), Rank.regular);
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        TribePlayer tribePlayer = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(player);
        if (tribePlayer.tribeData != null) {
            if (tribePlayer.tribeData.hasElder(player)) {
                Tribe tribe = Tribes.instance.tribeManager.getTribe(player);
                if (tribe != null && tribe.isInWorld(player.getLocation())) {
                    TribeChunk tribeChunk = tribe.tribeHandler.tribeChunkHandler.getTribeChunk(player.getLocation().getChunk());
                    if (tribeChunk != null) {
                        if (args != null && !args.isEmpty()) {
                            if (args.size() == 1) {
                                if (args.get(0).equalsIgnoreCase("view")) {
                                    if (tribeChunk.tribeChunkType == TribeChunk.TribeChunkType.wild) {
                                        if (tribeChunk.hasOwner()) {
                                            Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                                                @Override
                                                public void run() {
                                                    NameFetcher nameFetcher = new NameFetcher(Arrays.asList(tribeChunk.owner));
                                                    try {
                                                        Map<UUID, String> names = nameFetcher.call();
                                                        if (player != null) {
                                                            F.message(player, "This chunk is owned by " + C.purple + names.get(tribeChunk.owner));
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });

                                        } else {
                                            F.message(player, "This chunk is not owned by any Tribe Member");
                                            F.message(player, "Grant ownership to a MEMBER with " + C.aqua + "/chunk setowner (player)");
                                        }
                                    } else {
                                        F.message(player, "This is a " + C.green + "Core Chunk");
                                        F.message(player, "These chunks cannot be built upon");
                                    }
                                }
                            } else if (args.size() == 2) {
                                if (args.get(0).equalsIgnoreCase("setowner")) {
                                    if (tribeChunk.tribeChunkType == TribeChunk.TribeChunkType.wild) {
                                        Player owner = Bukkit.getServer().getPlayer(args.get(1));
                                        if (owner != null) {
                                            if (tribePlayer.tribeData.contains(owner)) {
                                                tribeChunk.setOwner(owner.getUniqueId());

                                                F.message(player, "You have given ownership of this plot to " + C.purple + owner.getName());
                                                S.plingHigh(player);

                                                F.message(owner, "You have been given ownership of a plot");
                                                S.plingHigh(owner);

                                                Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Tribes.instance.tribeManager.updateTribeDataInDatabase(tribePlayer.tribeData);
                                                    }
                                                });
                                            } else {
                                                F.warning(player, C.purple + owner.getName() + C.gray + " is not part of your tribe");
                                            }
                                        } else {
                                            F.warning(player, C.purple + args.get(1) + C.gray + " is not online");
                                        }
                                    } else {
                                        F.warning(player, "This is a " + C.yellow + "Core Chunk");
                                        F.warning(player, "Wild Chunks can be found outside tribe walls");
                                    }
                                }
                            }

                        } else {
                            this.showCommands(player);
                        }
                    } else {
                        F.warning(player, "You must be inside your tribe to use /chunk");
                    }
                } else {
                    F.warning(player, "You must be inside your tribe to use /chunk");
                }
            } else {
                F.warning(player, "/chunk is only avaliable to " + C.yellow + "Tribal Elders");
            }
        } else {
            F.warning(player, "You do not belong to a tribe");
        }

        return false;
    }

    public void showCommands(Player player) {
        F.warning(player, "/chunk setowner (player)");
        F.warning(player, "/chunk view");
    }
}
