package com.minehut.tribes.chat;

import com.minehut.core.Core;
import com.minehut.core.player.PlayerInfo;
import com.minehut.core.player.Rank;
import com.minehut.core.util.common.chat.C;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.tribe.TribeManager;
import com.minehut.tribes.tribe.player.TribePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by luke on 7/30/15.
 */
public class ChatManager implements Listener {
    public TribeManager tribeManager;

    public ChatManager(Tribes tribes) {
        this.tribeManager = tribes.tribeManager;
        tribes.registerListener(this);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if(event.isCancelled()) return;

        Player player = event.getPlayer();
        PlayerInfo playerInfo = Core.getInstance().getPlayerInfo(player);
        TribePlayer tribePlayer = tribeManager.tribePlayerManager.getTribePlayer(player);

        if(playerInfo.getRank().has(null, Rank.Admin, false)) {
            event.setFormat(
                    tribePlayer.getFormattedName()
                    + C.dgray + " » "
                    + C.green + "%2$s");
        }
        else if(playerInfo.getRank().has(null, Rank.Mod, false)) {
            event.setFormat(
                    tribePlayer.getFormattedName()
                    + C.dgray + " » "
                    + C.yellow + "%2$s");
        }
        else {
            event.setFormat(
                    tribePlayer.getFormattedName()
                    + C.dgray + " » "
                    + C.white + "%2$s");
        }
    }
}
