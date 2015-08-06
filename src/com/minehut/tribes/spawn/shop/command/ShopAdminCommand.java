package com.minehut.tribes.spawn.shop.command;

import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.spawn.shop.ShopItem;
import com.minehut.tribes.spawn.shop.ShopManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by luke on 7/27/15.
 */
public class ShopAdminCommand extends Command {
    ShopManager shopManager;

    public ShopAdminCommand(JavaPlugin plugin, ShopManager shopManager) {
        super(plugin, "shopadmin", Rank.Admin);
        this.shopManager = shopManager;
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        if (args != null) {
            if(args.size() == 3) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack != null && itemStack.getType() != Material.AIR) {

                    Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new ShopAdminBuyCommandRunnable(player.getUniqueId(), itemStack, args.get(1), args.get(2)));
                } else {
                    F.warning(player, "Please hold the item you are working with.");
                }
            } else if (args.size() == 1) {
                ItemStack itemStack = player.getItemInHand();
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new ShopAdminRemoveItemRunnable(player.getUniqueId(), itemStack));
                } else {
                    F.warning(player, "Could not find item in hand");
                }
            }
        } else {
            F.warning(player, "Hold the item and type...");
            F.warning(player, "/shopadmin add (buy/sell) (price)");
            F.warning(player, "/shopadmin remove");
        }

        return false;
    }
    
    public class ShopAdminRemoveItemRunnable implements Runnable {
    	
    	public UUID playerUUID;
    	public ItemStack itemStack;
  	
    	public ShopAdminRemoveItemRunnable(UUID playerUUID, ItemStack itemStack) {
    		this.playerUUID = playerUUID;
    		this.itemStack = itemStack;
    	}
    	
    	@Override
        public void run() {
    		Player player = Bukkit.getPlayer(this.playerUUID);
            shopManager.removeShopItem(itemStack.getType());
            shopManager.loadShopItems();

            F.message(player, C.purple + itemStack.getType().toString() + C.yellow + " has been " + C.red + "removed");
        }
    }
    
    public class ShopAdminBuyCommandRunnable implements Runnable {
    	
    	public UUID playerUUID;
    	public ItemStack itemStack;
    	public String args1, args2;
    	
    	public ShopAdminBuyCommandRunnable(UUID playerUUID, ItemStack itemStack, String args1, String args2) {
    		this.playerUUID = playerUUID;
    		this.itemStack = itemStack;
    		this.args1 = args1;
    		this.args2 = args2;
    	}
    	
    	@Override
        public void run() {
    		Player player = Bukkit.getPlayer(this.playerUUID);
            long buy = 0;
            long sell = 0;

            if (this.args1.equalsIgnoreCase("buy")) {
                buy = Long.parseLong(this.args2);
            } else {
                sell = Long.parseLong(this.args2);
            }

            if (buy != sell) {
                ShopItem shopItem = new ShopItem(itemStack.getType(), buy, sell);
                shopManager.uploadShopItem(shopItem);
                shopManager.loadShopItems();

                if (buy > 0) {
                    /* Buy */
                    F.message(player, C.purple + shopItem.material.toString() +
                            C.white + " is now " + C.red + "buyable " + C.white + "for "
                            + C.gold + buy + " coins");
                } else {
                    /* Sell */
                    F.message(player, C.purple + shopItem.material.toString() +
                            C.white + " is now " + C.green + "sellable " + C.white + "for "
                            + C.gold + sell + " coins");
                }
            } else {
                F.warning(player, "Price must be greater than 0");
            }
        }
    }
}
