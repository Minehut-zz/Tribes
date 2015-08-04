package com.minehut.tribes.tribe.player;

import com.minehut.core.Core;
import com.minehut.core.player.PlayerInfo;
import com.minehut.core.util.common.chat.C;
import com.minehut.tribes.tribe.TribeData;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by luke on 6/13/15.
 */
public class TribePlayer {
    public Player player;
    public TribeData tribeData;
    public long coins = 0;
    public UUID lastDamagedFrom;

    public boolean loaded;

    public TribePlayer(Player player, TribeData tribeData) {
        this.player = player;
        this.tribeData = tribeData;
        this.lastDamagedFrom = null;

        this.loaded = false;
    }

    public String getFormattedName() {
        PlayerInfo playerInfo = Core.getInstance().getPlayerInfo(player);

        if (tribeData != null) {
            return C.gray + "(" + tribeData.getShortenedName() + C.gray + ") " + playerInfo.getRank().getTag() + player.getName();
        } else {
            return playerInfo.getRank().getTag() + player.getName();
        }
    }

    public boolean hasEnoughCoins(long coins) {
        return this.coins >= coins;
    }

    public void setTribeData(TribeData tribeData) {
        this.tribeData = tribeData;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }
}
