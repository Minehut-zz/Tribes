package com.minehut.tribes.tribe;

import java.util.UUID;

/**
 * Created by luke on 7/23/15.
 */
public class TribeInvite {
    public TribeData tribeData;
    public UUID invited;

    public TribeInvite(TribeData tribeData, UUID invited) {
        this.tribeData = tribeData;
        this.invited = invited;
    }
}
