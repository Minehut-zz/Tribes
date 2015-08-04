package com.minehut.tribes.tribe.building.buildings;

import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.items.EnchantGlow;
import com.minehut.core.util.common.items.ItemStackFactory;
import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.tribe.building.Building;
import com.minehut.tribes.tribe.building.BuildingType;
import com.minehut.tribes.tribe.chunk.TribeChunk;
import com.minehut.tribes.tribe.player.TribePlayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by luke on 7/22/15.
 */
public class BlackSmithBuilding extends Building {

    public BlackSmithBuilding(TribeChunk tribeChunk, int level) {
        super(BuildingType.blacksmith, level,  tribeChunk);
    }

    @Override
    public void onInventoryClick(Player player, ItemStack itemStack) {
        if (itemStack.getItemMeta().getDisplayName().contains(C.purple + C.bold + "Enchantment Crate")) {
            TribePlayer tribePlayer = Tribes.instance.tribeManager.tribePlayerManager.getTribePlayer(player);

            if (tribePlayer.hasEnoughCoins(0)) {
                if(player.getInventory().firstEmpty() != -1) {

                    int level = 0;
                    String s = itemStack.getItemMeta().getDisplayName();
                    if (s.contains("Level 1")) {
                        level = 1;
                    } else if (s.contains("Level 2")) {
                        level = 2;
                    } else if (s.contains("Level 3")) {
                        level = 3;
                    }


                    Tribes.instance.tribeManager.tribePlayerManager.addCoins(tribePlayer, 0, "Opened Level " + level + " Enchantment Crate");

                    player.closeInventory();
                    S.playSound(player, Sound.ANVIL_USE);

                    this.giveRandomEnchantBook(player, level);
                } else {
                    F.message(player, C.red + "Your inventory is full!");
                    F.message(player, C.red + "Leave a slot open to use the " + C.purple + C.bold + "Enchantment Crate");
                }
            } else {
                F.message(player, C.red + "Not enough coins");
                S.playSound(player, Sound.GLASS);
            }
        }
    }

    public void giveRandomEnchantBook(Player player, int level) {
        HashMap<Enchantment, Integer> enchants = new HashMap<>();
        ArrayList<ItemStack> books = new ArrayList<>();

        if (level >= 1) {
            enchants.put(Enchantment.DAMAGE_ALL, 1);
            enchants.put(Enchantment.ARROW_FIRE, 1);
            enchants.put(Enchantment.DIG_SPEED, 1);
        }

        if (level >= 2) {
            enchants.put(Enchantment.DIG_SPEED, 2);
        }


        for (Enchantment enchantment : enchants.keySet()) {
            ItemStack book = ItemStackFactory.createItem(Material.ENCHANTED_BOOK);

            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            meta.addStoredEnchant(enchantment, enchants.get(enchantment), true);
            book.setItemMeta(meta);

            books.add(book);
        }

        Collections.shuffle(books);


        player.getInventory().addItem(books.get(0));
        S.pop(player);
    }

    @Override
    public void generateInventory() {

        for (int i = 1; i <= this.level; i++) {
            int index = 29 + (i * 9);

            ItemStack enchantBox = ItemStackFactory.createItem(Material.ENCHANTMENT_TABLE, C.purple + C.bold + "Enchantment Crate" + C.red + " [Level " + i + "]",
                    Arrays.asList(
                            "",
                            C.yellow + "Purchase a random " + C.purple + C.bold + "Enchantment Book",
                            C.red + C.strike + "     [" + C.green + C.bold + "  Level " + this.level + "  " + C.red + C.strike + "]     ",
                            "",
                            C.gray + "Price: " + C.gold + this.getEnchantPrice(i) + " coins",
                            ""
                    ));
            EnchantGlow.addGlow(enchantBox);
            super.inventory.setItem(index, enchantBox);
        }

    }

    public long getEnchantPrice(int level) {
        if (level == 1) {
            return 500;
        } else if (level == 2) {
            return 1000;
        } else if (level == 3) {
            return 2000;
        }
        return 1000;
    }

    @Override
    public void onClick(Player player) {

    }

    @Override
    public void extraUnload() {

    }
}
