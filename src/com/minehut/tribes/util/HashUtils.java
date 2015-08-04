package com.minehut.tribes.util;

import java.util.HashMap;
import java.util.List;

/**
 * Created by luke on 7/30/15.
 */
public class HashUtils {
    public static HashMap<Integer, Long> upgradeSortment(List<Long> prices) {
        HashMap<Integer, Long> hash = new HashMap<>();

        int i = 2;
        for (long l : prices) {
            hash.put(i, l);
            i++;
        }

        return hash;
    }
}
