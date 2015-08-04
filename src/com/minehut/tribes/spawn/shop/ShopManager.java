package com.minehut.tribes.spawn.shop;

import com.google.gson.Gson;
import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.inventory.InvUtils;
import com.minehut.core.util.common.items.ItemStackFactory;
import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.damage.event.CustomDamageEvent;
import com.minehut.tribes.spawn.shop.command.ShopAdminCommand;
import com.minehut.tribes.tribe.player.TribePlayer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

/**
 * Created by luke on 7/27/15.
 */
public class ShopManager implements Listener {
    public ArrayList<ShopItem> shopItems;
    public DBCollection shopItemsCollection;
    public World world;


    public Inventory buyInventory;
    public Inventory sellInventory;

    public int keepMobStationaryRunnable;

    public Villager buyNPC;
    public Location buyLocation;

    public Villager sellNPC;
    public Location sellLocation;

    public ShopManager(Tribes tribes) {
        this.shopItemsCollection = tribes.db.getCollection("shopItems");
        this.world = tribes.spawnHandler.world;

        this.buyInventory = Bukkit.getServer().createInventory(null, 45, C.underline + "Buy Items");
        this.sellInventory = Bukkit.getServer().createInventory(null, 45, C.underline + "Sell Items");

        Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
            @Override
            public void run() {
                loadShopItems();
            }
        });

        this.makeNPC();

        Tribes.instance.registerListener(this);

        new ShopAdminCommand(tribes, this);
    }

    public void makeNPC() {

        /* SELL NPC */
        this.sellLocation = new Location(world, 50.5, 72.5, 26.5);
        this.sellLocation.getChunk().load();
        this.sellNPC = (Villager) world.spawnEntity(sellLocation, EntityType.VILLAGER);
        this.sellNPC.setProfession(Villager.Profession.BUTCHER);

        this.sellNPC.setRemoveWhenFarAway(false);
        this.sellNPC.setCustomName(C.yellow + "Vendor " + C.green + "[SELL]");
        this.sellNPC.setCustomNameVisible(true);

        this.sellNPC.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 6, false, false));

        /* Buy NPC */
        this.buyLocation = new Location(world, 62, 72.5, 28.5);
        this.buyLocation.getChunk().load();
        this.buyNPC = (Villager) world.spawnEntity(buyLocation, EntityType.VILLAGER);
        this.buyNPC.setProfession(Villager.Profession.BUTCHER);

        this.buyNPC.setRemoveWhenFarAway(false);
        this.buyNPC.setCustomName(C.yellow + "Trader " + C.red + "[BUY]");
        this.buyNPC.setCustomNameVisible(true);

        this.buyNPC.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 6, false, false));


        this.keepMobStationaryRunnable = this.keepMobStationary();
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() == this.buyNPC) {
            event.setCancelled(true);
            this.openBuyInventory(event.getPlayer());
        } else if (event.getRightClicked() == this.sellNPC) {
            event.setCancelled(true);
            this.openSellInventory(event.getPlayer());
        }
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if(event.getDamagerPlayer() != null) {
            if (event.getHurtEntity() == this.buyNPC) {
                event.setCancelled(true);
                this.openBuyInventory(event.getDamagerPlayer());
            } else if (event.getHurtEntity() == this.sellNPC) {
                event.setCancelled(true);
                this.openSellInventory(event.getDamagerPlayer());
            }
        }
    }

    public int keepMobStationary() {
        return Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Tribes.instance, new Runnable() {
            @Override
            public void run() {
                sellNPC.teleport(sellLocation);
                buyNPC.teleport(buyLocation);
            }
        }, 10L, 10L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equals(C.underline + "Buy Items")
                || event.getInventory().getName().equals(C.underline + "Sell Items")) {
            event.setCancelled(true);

            ShopItem shopItem = this.getShopItem(event.getCurrentItem());
            if (shopItem != null) {
                Player player = (Player) event.getWhoClicked();
                TribePlayer tribePlayer = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(player);

                if (shopItem.buyPrice > 0) {
                    /* Buy */
                    if (tribePlayer.hasEnoughCoins(shopItem.buyPrice)) {
                        if (player.getInventory().firstEmpty() != -1) {

                            Tribes.instance.tribeManager.tribePlayerManager.addCoins(tribePlayer, -shopItem.buyPrice, "Purchased " + C.purple + shopItem.material.toString());
                            player.getInventory().addItem(ItemStackFactory.createItem(shopItem.material));
                            S.pop(player);

                        } else {
                            F.warning(player, C.red + "Inventory Full! Empty to buy!");
                        }
                    } else {
                        F.warning(player, C.red + "You do not have enough coins!");
                    }
                } else {
                    /* Sell */
                    if (player.getInventory().contains(shopItem.material)) {

                        InvUtils.removeMaterialAmount(player.getInventory(), shopItem.material, 1);
                        Tribes.instance.tribeManager.tribePlayerManager.addCoins(tribePlayer, shopItem.sellPrice, "Sold " + C.purple + shopItem.material.toString());
                        S.pop(player);

                    } else {
                        F.warning(player, C.red + "You do not have a " + C.yellow + shopItem.material.toString());
                    }
                }
            }
        }
    }

    public void openBuyInventory(Player player) {
        player.openInventory(this.buyInventory);
        S.playSound(player, Sound.ENDERDRAGON_WINGS);
    }

    public void openSellInventory(Player player) {
        player.openInventory(this.sellInventory);
        S.playSound(player, Sound.ENDERDRAGON_WINGS);
    }

    public ShopItem getShopItem(ItemStack itemStack) {
        String name = itemStack.getItemMeta().getDisplayName();

        for (ShopItem shopItem : this.shopItems) {
            if (shopItem.material == itemStack.getType()) {
                return shopItem;
            }
        }

        return null;
    }

    public void loadShopItems() {
        Gson gson = new Gson();
        this.shopItems = new ArrayList<>();

        DBCursor dbCursor = this.shopItemsCollection.find();

        while (dbCursor.hasNext()) {
            DBObject found = dbCursor.next();

            long buyPrice = (long) found.get("buyPrice");
            long sellPrice = (long) found.get("sellPrice");

            String materialJson = (String) found.get("material");
            Material material = gson.fromJson(materialJson, Material.class);

            this.shopItems.add(new ShopItem(material, buyPrice, sellPrice));
        }

        this.buyInventory.clear();
        this.sellInventory.clear();

        int buyI = 10;
        int sellI = 10;
        for (ShopItem shopItem : this.shopItems) {
            if(shopItem.buyPrice > 0) {
                this.buyInventory.setItem(buyI, shopItem.getIcon());
                buyI += 2;
            } else {
                this.sellInventory.setItem(sellI, shopItem.getIcon());
                sellI += 2;
            }
        }
    }

    public void uploadShopItem(ShopItem shopItem) {
        Gson gson = new Gson();
        String materialJson = gson.toJson(shopItem.material);

        DBObject obj = new BasicDBObject("material", materialJson);
        obj.put("buyPrice", shopItem.buyPrice);
        obj.put("sellPrice", shopItem.sellPrice);

        DBObject query = new BasicDBObject("material", materialJson);
        DBObject found = this.shopItemsCollection.findOne(query);

        if (found == null) {
            this.shopItemsCollection.insert(obj);
        } else {
            this.shopItemsCollection.findAndModify(query, obj);
        }
    }

    public void removeShopItem(Material material) {
        Gson gson = new Gson();
        String materialJson = gson.toJson(material);

        DBObject query = new BasicDBObject("material", materialJson);

        this.shopItemsCollection.remove(query);
    }
}
