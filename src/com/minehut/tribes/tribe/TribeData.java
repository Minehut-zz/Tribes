package com.minehut.tribes.tribe;

import com.minehut.tribes.tribe.chunk.TribeChunk;
import com.minehut.tribes.tribe.player.TribalRank;
import com.minehut.tribes.tribe.troop.Troop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by luke on 7/19/15.
 */
public class TribeData {
    public UUID uuid;
    public String name;
    public long coins;
    public ArrayList<DataPlayer> dataPlayers;

    public ArrayList<TribeChunk> tribeChunks = null;
    public ArrayList<Troop> troops = null;

    public TribeData(UUID uuid, String name, long coins, ArrayList<DataPlayer> dataPlayers) {
        this.uuid = uuid;
        this.name = name;
        this.coins = coins;
        this.dataPlayers = dataPlayers;
    }

    public String getShortenedName() {
        String s = this.name;
        if (s.length() > 4) {
            s.substring(0, 4);
        }

        return s;
    }

    public void addDataPlayer(DataPlayer dataPlayer) {
        this.dataPlayers.add(dataPlayer);
    }

    public boolean hasElder(Player player) {
        if (this.isElder(player.getUniqueId()) || this.isOwner(player.getUniqueId())) {
            return true;
        }
        return false;
    }

    public ArrayList<Player> getOnlinePlayers() {
        ArrayList<Player> players = new ArrayList<>();
        for (DataPlayer dataPlayer : this.dataPlayers) {
            Player player = Bukkit.getServer().getPlayer(dataPlayer.uuid);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    public List<DataPlayer> getDataPlayersOfTribalRank(TribalRank tribalRank) {
        List<DataPlayer> players = new ArrayList<>();
        for (DataPlayer dataPlayer : this.dataPlayers) {
            if (dataPlayer.tribalRank == tribalRank) {
                players.add(dataPlayer);
            }
        }
        return players;
    }

    public boolean contains(Player player) {

        for (DataPlayer dataPlayer : this.dataPlayers) {
            if (dataPlayer.uuid.equals(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public DataPlayer getDataPlayerWithName(String name) {
        for (DataPlayer dataPlayer : this.dataPlayers) {
            if (dataPlayer.name.equalsIgnoreCase(name)) {
                return dataPlayer;
            }
        }
        return null;
    }

    public DataPlayer getDataPlayer(UUID uuid) {
        for (DataPlayer dataPlayer : this.dataPlayers) {
            if (dataPlayer.uuid.equals(uuid)) {
                return dataPlayer;
            }
        }
        return null;
    }

    public TribalRank getTribalRank(UUID uuid) {
        for (DataPlayer dataPlayer : this.dataPlayers) {
            if (dataPlayer.uuid.equals(uuid)) {
                return dataPlayer.tribalRank;
            }
        }
        return TribalRank.OUTSIDER;
    }

    public boolean isOwner(UUID uuid) {
        return this.getTribalRank(uuid) == TribalRank.OWNER;
    }

    public boolean isElder(UUID uuid) {
        return this.getTribalRank(uuid) == TribalRank.ELDER;
    }

    public boolean isMember(UUID uuid) {

        return this.getTribalRank(uuid) == TribalRank.MEMBER;
    }

    public void setOwner(UUID uuid) {
        this.getDataPlayer(uuid).setTribalRank(TribalRank.OWNER);
    }

    public void setElder(UUID uuid) {
        this.getDataPlayer(uuid).setTribalRank(TribalRank.ELDER);
    }

    public void setMember(UUID uuid) {
        this.getDataPlayer(uuid).setTribalRank(TribalRank.MEMBER);
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public boolean hasEnoughCoins(long coins) {
        return this.coins >= coins;
    }
}
