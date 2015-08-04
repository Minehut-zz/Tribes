package com.minehut.tribes.command;

import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.core.util.UUIDFetcher;
import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.teleport.TeleportDestination;
import com.minehut.tribes.teleport.TeleportManager;
import com.minehut.tribes.tribe.player.TribePlayer;
import com.minehut.tribes.tribe.*;
import com.minehut.tribes.tribe.player.TribalRank;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by luke on 7/20/15.
 */
public class TribeComand extends Command {
    Tribes tribes;
    TribeManager tribeManager;

    public ArrayList<TribeInvite> invites;

    public TribeComand(Tribes tribes) {
        super(tribes, "tribe", Arrays.asList("t", "tribes", "tr"), Rank.regular);
        this.tribes = tribes;
        this.tribeManager = tribes.tribeManager;

        this.invites = new ArrayList<>();
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        if (args == null || args.size() == 0) {
            this.listCommands(player);
            return false;
        }

        /* ############################################################ */
        if (args.get(0).equalsIgnoreCase("create")) {
            if(args.size() == 2) {
                String name = args.get(1);

                TribeData tribeData = this.tribeManager.getTribeData(player);
                if (tribeData != null) {
                    F.message(player, "You already belong to " + C.aqua + tribeData.name);
                    F.message(player, "Leave this tribe with " + C.green + "/tribe leave");
                    return true;
                }

                if (!this.tribeManager.isNameAvaliable(name)) {
                    F.message(player, C.aqua + name + C.yellow + " is already reserved");
                    return true;
                }


                Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                    @Override
                    public void run() {
                        tribes.tribeManager.createNewTribe(player, args.get(1));
                    }
                });
            } else {
                F.message(player, "/tribe create (name)");
            }
            return false;
        }

        /* ############################################################ */
        else if (args.get(0).equalsIgnoreCase("home")) {
            TribeData tribeData = this.tribes.tribeManager.getTribeData(player);

            if (tribeData == null) {
                F.message(player, "You do not belong to a tribe :(");
            } else {
                Tribe tribe = this.tribeManager.getTribe(player);
                if (tribe == null) {
                    if (this.tribeManager.loading.contains(tribeData)) {

                        F.warning(player, C.purple + tribeData.name + C.gray + " is loading up. Please wait.");
                    } else {
                        /* Can never happen */
                    }
                } else {
                    Tribes.instance.teleportManager.teleport(player, new TeleportDestination(tribe.world.getSpawnLocation(), TeleportManager.teleportTime));
                }
            }
            return true;
        }

        /* ############################################################ */
        else if (args.get(0).equalsIgnoreCase("leave")) {
            TribeData tribeData = this.tribes.tribeManager.getTribeData(player);

            if (tribeData == null) {
                F.message(player, "You do not belong to a tribe :(");
            } else {
                if (!tribeManager.loading.contains(tribeData)) {
                    this.tribeManager.leaveTribe(player, tribeData);
                } else {
                    F.warning(player, "Please wait until your tribe has finished loading");
                }

            }
            return true;
        }

        /* ############################################################ */
        else if (args.get(0).equalsIgnoreCase("deposit")) {
            TribeData tribeData = this.tribes.tribeManager.getTribeData(player);

            if (tribeData == null) {
                F.message(player, "You do not belong to a tribe :(");
            } else {
                if (args.size() == 2) {
                    long amount = Long.parseLong(args.get(1));
                    if (amount > 0) {
                        TribePlayer tribePlayer = tribeManager.tribePlayerManager.getTribePlayer(player);
                        if (tribePlayer.hasEnoughCoins(amount)) {
                            tribeManager.tribePlayerManager.addCoins(tribePlayer, -amount, "Deposit to Tribe Bank");
                            String msg = C.aqua + player.getName() + C.white + " deposited " + C.green + amount + " coins" + C.white + " into the Tribe Bank";
                            tribeManager.depositCoins(tribeData, amount, msg);
                        } else {
                            F.message(player, C.red + "You do not have enough coins for a deposit of " + C.green + amount);
                            F.message(player, C.red + "Check your coins with " + C.aqua + "/coins");
                        }
                    } else {
                        F.message(player, C.red + "You can only deposit an amount greater than 0");
                    }
                } else {
                    F.message(player, C.red + "/tribe deposit 200");
                }
            }
            return true;
        }

        /* ############################################################ */
        else if (args.get(0).equalsIgnoreCase("withdraw")) {
            TribeData tribeData = this.tribes.tribeManager.getTribeData(player);

            if (tribeData == null) {
                F.message(player, "You do not belong to a tribe :(");
            } else {
                if (tribeData.hasElder(player)) {
                    if (args.size() == 2) {
                        Long amount = Long.parseLong(args.get(1));

                        if (amount > 0) {
                            if(tribeData.hasEnoughCoins(amount)) {
                                TribePlayer tribePlayer = this.tribeManager.tribePlayerManager.getTribePlayer(player);

                                String msg = C.aqua + player.getName() + C.white + " withdrew " + C.red + amount + " coins" + C.white + " from the Tribe Bank";
                                tribeManager.depositCoins(tribeData, -amount, msg);
                                tribeManager.tribePlayerManager.addCoins(tribePlayer, amount, "Withdraw from Tribe Bank");

                            } else {
                                F.message(player, C.red + "Your tribe does not have enough coins");
                                F.message(player, C.red + "Check your tribe's coins with " + C.aqua + "/tribe coins");
                            }
                        } else {
                            F.message(player, C.red + "You can only withdraw an amount greater than 0");
                        }
                    } else {
                        F.message(player, C.red + "/tribe withdraw 200");
                    }
                } else {
                    F.message(player, C.red + "You must be a " + C.yellow + "Tribe Elder" + C.red + " to withdraw coins");
                }
            }
            return true;
        }

        /* ############################################################ */
        else if (args.get(0).equalsIgnoreCase("setmember")) {
            TribeData tribeData = this.tribes.tribeManager.getTribeData(player);

            if (tribeData == null) {
                F.warning(player, "You do not belong to a tribe :(");
            } else {
                if (args.size() == 2) {
                    Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                        @Override
                        public void run() {
                            UUID setting = null;
                            Player settingPlayer = Bukkit.getServer().getPlayer(args.get(1));
                            if (settingPlayer == null) {
                                UUIDFetcher uuidFetcher = new UUIDFetcher(Arrays.asList(args.get(1)));
                                try {
                                    setting = uuidFetcher.call().get(args.get(1));
                                } catch (Exception e) {
                                    setting = null;
                                }
                            } else {
                                setting = settingPlayer.getUniqueId();
                            }

                            if (setting != null) {

                                TribalRank settingTribalRank = tribeData.getTribalRank(setting);

                                if (settingTribalRank != TribalRank.OUTSIDER) {
                                    TribalRank playerTribalRank = tribeData.getTribalRank(player.getUniqueId());
                                    if (settingTribalRank.compareTo(playerTribalRank) < 0) {
                                        tribeData.setMember(setting);

                                        tribeManager.updateTribeDataInDatabase(tribeData);

                                        for (Player p : tribeData.getOnlinePlayers()) {
                                            F.message(p, C.purple + args.get(1) + C.yellow + " had his tribal rank set to "
                                                    + C.blue + C.bold + TribalRank.MEMBER.toString(), F.BroadcastType.FULL_BORDER);
                                            S.plingHigh(p);
                                        }
                                    } else {
                                        F.warning(player, "You do not have permission to derank " + C.purple + args.get(1));
                                    }
                                } else {
                                    F.warning(player, C.purple + args.get(1) + C.gray + " is not part of your tribe");
                                }
                            } else {
                                F.warning(player, "Couldn't find player " + C.purple + args.get(1));
                            }

                        }
                    });

                } else {
                    F.warning(player, "/t setmember (player)");
                }
            }
            return true;
        }

        /* ############################################################ */
        else if (args.get(0).equalsIgnoreCase("setelder")) {
            TribeData tribeData = this.tribes.tribeManager.getTribeData(player);

            if (tribeData == null) {
                F.warning(player, "You do not belong to a tribe :(");
            } else {
                if (args.size() == 2) {
                    Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                        @Override
                        public void run() {
                            UUID setting = null;
                            Player settingPlayer = Bukkit.getServer().getPlayer(args.get(1));
                            if (settingPlayer == null) {
                                UUIDFetcher uuidFetcher = new UUIDFetcher(Arrays.asList(args.get(1)));
                                try {
                                    setting = uuidFetcher.call().get(args.get(1));
                                } catch (Exception e) {
                                    setting = null;
                                }
                            } else {
                                setting = settingPlayer.getUniqueId();
                            }

                            if (setting != null) {

                                TribalRank settingTribalRank = tribeData.getTribalRank(setting);

                                if (settingTribalRank != TribalRank.OUTSIDER) {
                                    TribalRank playerTribalRank = tribeData.getTribalRank(player.getUniqueId());
                                    if (settingTribalRank.compareTo(playerTribalRank) < 0) {
                                        tribeData.setElder(setting);

                                        tribeManager.updateTribeDataInDatabase(tribeData);

                                        for (Player p : tribeData.getOnlinePlayers()) {
                                            F.message(p, C.purple + args.get(1) + C.yellow + " had his tribal rank set to "
                                                    + C.blue + C.bold + TribalRank.ELDER.toString(), F.BroadcastType.FULL_BORDER);
                                            S.plingHigh(p);
                                        }
                                    } else {
                                        F.warning(player, "You do not have permission to derank " + C.purple + args.get(1));
                                    }
                                } else {
                                    F.warning(player, C.purple + args.get(1) + C.gray + " is not part of your tribe");
                                }
                            } else {
                                F.warning(player, "Couldn't find player " + C.purple + args.get(1));
                            }

                        }
                    });

                } else {
                    F.warning(player, "/t setelder (player)");
                }
            }
            return true;
        }

        /* ############################################################ */
        else if (args.get(0).equalsIgnoreCase("coins")) {
            TribeData tribeData = this.tribes.tribeManager.getTribeData(player);

            if (tribeData == null) {
                F.message(player, "You do not belong to a tribe :(");
            } else {
                F.message(player, "Your tribe has " + C.green + tribeData.coins + " coins");
            }
            return true;
        }

        /* ############################################################ */
        else if (args.get(0).equalsIgnoreCase("list")) {
            TribeData tribeData = this.tribes.tribeManager.getTribeData(player);

            if (tribeData == null) {
                F.message(player, "You do not belong to a tribe :(");
            } else {
                F.message(player, C.dgray + "============== " + C.purple + C.bold + tribeData.name + C.dgray + " ==============");
                F.message(player, C.gray + "Owner: " + C.red + tribeData.getDataPlayersOfTribalRank(TribalRank.OWNER).get(0).name);

                F.message(player, C.dgray + "=== " + C.yellow + "Elders" + C.dgray + " ===");
                int i = 1;
                for (DataPlayer dataPlayer : tribeData.getDataPlayersOfTribalRank(TribalRank.ELDER)) {
                    F.message(player, C.dgray + i + C.yellow + dataPlayer.name);
                    i++;
                }

                F.message(player, C.dgray + "=== " + C.yellow + "Members" + C.dgray + " ===");
                int x = 1;
                for (DataPlayer dataPlayer : tribeData.getDataPlayersOfTribalRank(TribalRank.MEMBER)) {
                    F.message(player, C.dgray + x + C.white + dataPlayer.name);
                    x++;
                }
            }
            return true;
        }

        /* ############################################################ */
        else if (args.get(0).equalsIgnoreCase("kick")) {
            TribeData tribeData = this.tribes.tribeManager.getTribeData(player);

            if (tribeData == null) {
                F.message(player, "You do not belong to a tribe :(");
            } else {
                if (tribeData.hasElder(player)) {
                    if (args.size() == 2) {
                        DataPlayer dataPlayer = tribeData.getDataPlayerWithName(args.get(1));
                        if (dataPlayer != null) {
                            if (dataPlayer.tribalRank.compareTo(tribeData.getTribalRank(player.getUniqueId())) < 0) {

                                tribeManager.removeFromTribe(dataPlayer, tribeData);

                            } else {
                                F.message(player, C.red + "You can only kick a player with a lower tribal rank");
                            }
                        } else {
                            F.message(player, C.yellow + args.get(0) + C.red + " is not a MEMBER of your tribe");
                        }

                    } else {
                        F.message(player, C.red + "/tribe invite Luuke");
                    }
                } else {
                    F.message(player, C.red + "Only " + C.yellow + "Tribal Elders" + C.red + " are allowed to kick");
                }
            }
            return true;
        }

        /* ############################################################ */
        else if (args.get(0).equalsIgnoreCase("invite")) {
            TribeData tribeData = this.tribes.tribeManager.getTribeData(player);

            if (tribeData == null) {
                F.message(player, "You do not belong to a tribe :(");
            } else {
                if (tribeData.hasElder(player)) {
                    if (args.size() == 2) {
                        Player invited = Bukkit.getServer().getPlayer(args.get(1));
                        if (invited != null) {
                            TribePlayer tribePlayer = tribeManager.tribePlayerManager.getTribePlayer(invited);
                            if (tribePlayer.tribeData == null) {
                                this.invites.add(new TribeInvite(tribeData, invited.getUniqueId()));

                                F.message(invited, C.purple + "You have been invited to join " + C.yellow + tribeData.name);
                                F.message(invited, C.purple + "Join them with " + C.aqua + "/tribe join " + tribeData.name);

                                F.message(player, "You have invited " + C.aqua + invited.getName() + C.yellow + " to the tribe");

                            } else {
                                F.message(player, C.yellow + args.get(1) + C.red + " is already a MEMBER of " + C.yellow + tribePlayer.tribeData.name);
                            }
                        } else {
                            F.message(player, C.yellow + args.get(1) + C.red + " is not online");
                        }
                    } else {
                        F.message(player, C.red + "/tribe invite Luuke");
                    }
                } else {
                    F.message(player, C.red + "Only " + C.yellow + "Tribal Elders" + C.red + " are allowed to invite");
                }
            }
            return true;
        }

        else if (args.get(0).equalsIgnoreCase("join")) {
            TribeData playersTribeData = this.tribes.tribeManager.getTribeData(player);

            if (playersTribeData == null) {
                if (args.size() == 2) {
                    TribeData joiningTribeData = tribeManager.getTribeData(args.get(1));

                    if (joiningTribeData != null) {
                        for (TribeInvite tribeInvite : this.invites) {
                            if (tribeInvite.tribeData == joiningTribeData) {
                                if (tribeInvite.invited.equals(player.getUniqueId())) {

                                    TribePlayer tribePlayer = tribeManager.tribePlayerManager.getTribePlayer(player);

                                    joiningTribeData.addDataPlayer(new DataPlayer(player.getName(), player.getUniqueId(), TribalRank.MEMBER));
                                    tribePlayer.tribeData = joiningTribeData;

                                    Tribe tribe = tribeManager.getTribe(player);
                                    if (tribe != null) {
                                        for (Player player1 : tribe.getOnlinePlayers()) {
                                            F.message(player1, C.aqua + player.getName() + C.yellow + " has joined " + C.purple + joiningTribeData.name);
                                            S.playSound(player1, Sound.LEVEL_UP);
                                        }
                                    } else {

                                        if (tribeManager.loading.contains(joiningTribeData)) {
                                            for (Player player1 : joiningTribeData.getOnlinePlayers()) {
                                                F.message(player1, C.aqua + player.getName() + C.yellow + " has joined " + C.purple + joiningTribeData.name);
                                                S.playSound(player1, Sound.LEVEL_UP);
                                            }
                                        } else {

                                            F.message(player, C.aqua + player.getName() + C.yellow + " has joined " + C.purple + joiningTribeData.name);
                                            S.playSound(player, Sound.LEVEL_UP);

                                            Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                                                @Override
                                                public void run() {
                                                    tribeManager.startTribe(joiningTribeData);
                                                }
                                            });
                                        }
                                    }

                                    this.tribeManager.updateTribeDataInDatabase(joiningTribeData);

                                    return true;
                                }
                            }
                        }

                        F.message(player, C.red + "You do not have an invite from " + C.yellow + joiningTribeData.name);

                    } else {
                        F.message(player, C.red + "Unable to find a tribe by the name of " + C.yellow + args.get(1));
                    }
                } else {
                    F.message(player, C.red + "/tribe join Minehut");
                }
            } else {
                F.message(player, C.red + "You are already part of the " + C.purple + playersTribeData.name + " Tribe");
                F.message(player, C.red + "Leave them by typing " + C.aqua + "/tribe leave");
            }
            return true;
        }

        /* ############################################################ */
        else {
            this.listCommands(player);
        }

        return false;
    }

    public void listCommands(Player player) {
        F.message(player, "/tribe create (name)");
        F.message(player, "/tribe home");
        F.message(player, "/tribe deposit (amount)");
        F.message(player, "/tribe coins");
        F.message(player, "/tribe leave");
    }
}
