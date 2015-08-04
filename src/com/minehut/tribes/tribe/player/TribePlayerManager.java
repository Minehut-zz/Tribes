package com.minehut.tribes.tribe.player;

import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.tribe.Tribe;
import com.minehut.tribes.tribe.TribeData;
import com.minehut.tribes.tribe.TribeManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

/**
 * Created by luke on 7/21/15.
 */
public class TribePlayerManager implements Listener {
    public TribeManager tribeManager;
    public ArrayList<TribePlayer> tribePlayers;
    public DBCollection tribePlayersCollection;

    public TribePlayerManager(TribeManager tribeManager) {
        this.tribePlayers = new ArrayList<>();
        this.tribeManager = tribeManager;
        this.tribePlayersCollection = Tribes.instance.db.getCollection("tribePlayers");

        Tribes.instance.registerListener(this);
    }

    public TribePlayer getTribePlayer(Player player) {
        for (TribePlayer tribePlayer : this.tribePlayers) {
            if (tribePlayer.player == player) {
                return tribePlayer;
            }
        }
        return null;
    }

    public long addCoins(Player player, long amount, String reason) {
        return this.addCoins(this.getTribePlayer(player), amount, reason);
    }

    public long addCoins(TribePlayer tribePlayer, long amount, String reason) {

        DBObject query = new BasicDBObject("uuid", tribePlayer.player.getUniqueId());
        DBObject found = tribePlayersCollection.findOne(query);

        long oldCoins = (long) found.get("coins");
        long updatedCoins = oldCoins + amount;

        found.put("coins", updatedCoins);
        tribePlayersCollection.findAndModify(query, found);

        tribePlayer.setCoins(updatedCoins);

        if (reason != null) {
            if (tribePlayer.player != null) {
                if (amount > 0) {
                    F.message(tribePlayer.player, C.green + "+" + amount + " coins " + C.gold + "| " + C.white + reason);
                } else {
                    F.message(tribePlayer.player, C.red + amount + " coins " + C.gold + "| " + C.white + reason);
                }
            }
        }

        return updatedCoins;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        /* Initialize TribePlayer object */
        TribeData tribeData = this.tribeManager.getTribeData(event.getPlayer());
        TribePlayer tribePlayer = new TribePlayer(event.getPlayer(), tribeData);
        this.tribeManager.tribePlayerManager.tribePlayers.add(tribePlayer);

        /* Startup Tribe if unloaded */
        Tribe tribe = this.tribeManager.getTribe(event.getPlayer());
        if (tribe == null) {
            if (tribeData != null) {
                if(!tribeManager.loading.contains(tribeData)) {
                    Bukkit.getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                        @Override
                        public void run() {
                            tribeManager.startTribe(tribeData);
                        }
                    });
                }
            }
        }

        Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
            @Override
            public void run() {
                if (loadDatabaseInfo(tribePlayer)) {
                    F.broadcast(tribePlayer.getFormattedName() + C.purple + " has joined for the first time!");
                } else {
                    F.broadcast(tribePlayer.getFormattedName() + C.gray + " joined the game");
                }
                tribePlayer.loaded = true;
            }
        });

        event.setJoinMessage("");
    }

    /* Returns true if player is new */
    private boolean loadDatabaseInfo(TribePlayer tribePlayer) {
        if(tribePlayer.player != null) {
            DBObject query = new BasicDBObject("uuid", tribePlayer.player.getUniqueId());
            DBObject found = this.tribePlayersCollection.findOne(query);

            if (found == null) {
                /* New Player */
                DBObject obj = new BasicDBObject("uuid", tribePlayer.player.getUniqueId());
                obj.put("name", tribePlayer.player.getName());

                /* coins */
                obj.put("coins", (long) 0);

                /* Push to Database */
                this.tribePlayersCollection.insert(obj);

                return true;
            } else {

                long coins = (long) found.get("coins");
                tribePlayer.coins = coins;

                /* Update player name (name changes) */
                if(!tribePlayer.player.getName().equals(found.get("name"))) {
                    found.put("name", tribePlayer.player.getName());
                    this.tribePlayersCollection.findAndModify(query, found);
                }

                return false;
            }
        }
        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.tribePlayers.remove(this.getTribePlayer(event.getPlayer()));
        event.setQuitMessage("");
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        this.tribePlayers.remove(this.getTribePlayer(event.getPlayer()));
    }
}
