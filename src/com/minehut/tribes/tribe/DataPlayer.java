package com.minehut.tribes.tribe;

import com.minehut.tribes.tribe.player.TribalRank;

import java.util.UUID;

/**
 * Created by luke on 7/23/15.
 */
public class DataPlayer {
    public String name;
    public UUID uuid;
    public TribalRank tribalRank;

    public DataPlayer(String name, UUID uuid, TribalRank tribalRank) {
        this.name = name;
        this.uuid = uuid;
        this.tribalRank = tribalRank;
    }

    public void setTribalRank(TribalRank tribalRank) {
        this.tribalRank = tribalRank;
    }
}
