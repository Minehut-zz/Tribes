package com.minehut.tribes.spawn.shop;

import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.items.ItemStackFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Created by luke on 7/27/15.
 */
public class ShopItem {
    public Material material;
    public long buyPrice;
    public long sellPrice;

    public transient ItemStack icon;

    public ShopItem(Material material, long buyPrice, long sellPrice) {
        this.material = material;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;

        if (sellPrice > 0 && buyPrice == 0) {
            this.icon = ItemStackFactory.createItem(material, C.yellow + material.toString(),
                    Arrays.asList(
                            "",
                            C.green + C.bold + "SELL " + C.gold + sellPrice + " coins",
                            ""
                    ));
        } else {
            this.icon = ItemStackFactory.createItem(material, C.yellow + material.toString(),
                    Arrays.asList(
                            "",
                            C.red + C.bold + "BUY " + C.gold + buyPrice + " coins",
                            ""
                    ));
        }
    }

    public long getBuyPrice() {
        return buyPrice;
    }

    public long getSellPrice() {
        return sellPrice;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public Material getMaterial() {
        return material;
    }
}
