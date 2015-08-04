package com.minehut.tribes.tribe.building;

import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.items.ItemStackFactory;
import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.damage.event.CustomDamageEvent;
import com.minehut.tribes.module.Module;
import com.minehut.tribes.schematic.Schematic;
import com.minehut.tribes.schematic.SchematicUtils;
import com.minehut.tribes.tribe.Tribe;
import com.minehut.tribes.tribe.chunk.TribeChunk;
import com.minehut.tribes.util.HashUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by luke on 7/20/15.
 */
public abstract class Building implements Module {
    public int level;
    public BuildingType buildingType;

    public TribeChunk tribeChunk;

    /* Will be null until spawn() is called */
    public Tribe tribe;
    public int inventorySize = 54;

    private int keepMobStationaryRunnable;
    public transient Villager npc;
    public Inventory inventory;

    public boolean openInventoryOnClick = true;

    public Building(BuildingType buildingType, int level, TribeChunk tribeChunk) {
        this.buildingType = buildingType;
        this.level = level;
        this.tribeChunk = tribeChunk;
    }

    public Schematic getSchematic() {
        return SchematicUtils.loadSchematic(new File("schematics/" + buildingType.schematicFormat(level) + ".schematic"));
    }

    public String getInventoryName(Tribe tribe) {
        return C.underline + tribe.tribeData.name + " " + this.buildingType.name;
    }

    public class UpgradePrices extends HashMap<Integer, Long> {
        public UpgradePrices(List<Long> prices) {
            super(HashUtils.upgradeSortment(prices));
        }
    }

    public void upgrade() {

        this.level += 1;
        this.tribeChunk.buildingLevel = this.level;

        Tribes.instance.tribeManager.updateTribeDataInDatabase(tribe.tribeData);

        F.broadcast(C.purple + tribe.tribeData.name + C.yellow + " has upgraded their " + C.green + buildingType.name + C.yellow + " to " + C.aqua + "level " + this.level);

        for (Player player : tribe.getOnlinePlayers()) {
            S.playSound(player, Sound.ANVIL_USE);
        }

        SchematicUtils.paste(this.getSchematic(), new Location(tribe.world, buildingType.schemX, buildingType.schemY, buildingType.schemZ));

        this.makeInventory();
        this.npc.setCustomName(C.green + this.buildingType.name + C.red + " [Level " + this.level + "]");
    }

    public int keepMobStationary() {
        return Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Tribes.instance, new Runnable() {
            @Override
            public void run() {
                Location location = new Location(tribe.world, buildingType.npcX, buildingType.npcY, buildingType.npcZ);
                npc.teleport(location);
            }
        }, 10L, 10L);
    }

    public ItemStack getUpgradeItem() {
        return ItemStackFactory.createItem(
                Material.NETHER_STAR,
                C.yellow + C.bold + "UPGRADE",
                Arrays.asList(
                        "",
                        C.gray + "Click to " + C.aqua + "upgrade" + C.gray + " your " + C.green + this.buildingType.name + C.gray + " to " + C.aqua + "level " + this.level,
                        ""
                ));
    }

    public void makeInventory() {
        this.inventory = Bukkit.getServer().createInventory(null, this.inventorySize, this.getInventoryName(tribe));

        /* Upgrade */
        if(this.level < 3) {
            this.inventory.setItem(4, getUpgradeItem());
        } else {
            this.inventory.setItem(4, ItemStackFactory.createItem(
                    Material.BARRIER,
                    C.red + C.bold + "MAX LEVEL",
                    Arrays.asList(
                            "",
                            C.purple + this.buildingType.name + C.gray + " is already at the " + C.red + "maximum level!",
                            ""
                    )));
        }

        ItemStack itemStack = ItemStackFactory.createItem(Material.STAINED_GLASS_PANE);
        itemStack.setDurability((short) 15);



        this.inventory.setItem(0, itemStack);
        this.inventory.setItem(1, itemStack);
        this.inventory.setItem(2, itemStack);
        this.inventory.setItem(3, itemStack);
        this.inventory.setItem(5, itemStack);
        this.inventory.setItem(12, itemStack);
        this.inventory.setItem(13, itemStack);
        this.inventory.setItem(14, itemStack);
        this.inventory.setItem(6, itemStack);
        this.inventory.setItem(7, itemStack);
        this.inventory.setItem(8, itemStack);

        this.generateInventory();
    }

    public abstract void onInventoryClick(Player player, ItemStack itemStack);

    public abstract void generateInventory();

    public void spawn(Tribe tribe) {

        /* ####### NPC #######*/
        this.tribe = tribe;

        Location location = new Location(tribe.world, buildingType.npcX, buildingType.npcY, buildingType.npcZ);
        this.npc = (Villager) tribe.world.spawnEntity(location, EntityType.VILLAGER);
        this.npc.setProfession(this.buildingType.profession);

        this.npc.setRemoveWhenFarAway(false);
        this.npc.setCustomName(C.green + this.buildingType.name + C.red + " [Level " + this.level + "]");
        this.npc.setCustomNameVisible(true);

        this.npc.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 6, false, false));

        this.keepMobStationaryRunnable = this.keepMobStationary();



        this.makeInventory();

        Tribes.instance.registerListener(this);
    }

    public long getUpgradeCost() {
        if (this.level == 1) {
            return this.buildingType.upgrade2;
        }
        else if (this.level == 2) {
            return this.buildingType.upgrade3;
        } else {
            return 0;
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClickBuilding(InventoryClickEvent event) {
        if (this.tribe != null) {
            if (event.getInventory().getName().equals(this.getInventoryName(this.tribe))) {
                ItemStack click = event.getCurrentItem();
                Player player = (Player) event.getWhoClicked();
                event.setCancelled(true);

                if (click.getType() == Material.NETHER_STAR) {

                    if (tribe.tribeData.hasElder(player)) {
                        player.closeInventory();

                        if(this.level < this.buildingType.maxLevel) {
                            if (this.tribe.tribeData.hasEnoughCoins(this.getUpgradeCost())) {
                                upgrade();
                            } else {
                                F.warning(player, "Your " + C.purple + "tribe bank" + C.gray + " does not have enough coins");
                                F.warning(player, "Deposit coins with " + C.green + "/t deposit (amount)");
                            }
                        } else {
                            F.warning(player, C.purple + this.buildingType.name + C.gray + " is already at max level");
                        }
                    } else {
                        F.message(player, "Only " + C.yellow + "Tribal Elders" + C.red + " can upgrade buildings");
                    }
                }

                else if (click.getType() == Material.BARRIER) {
                    event.setCancelled(true);
                    F.warning(player, C.purple + this.buildingType.name + C.gray + " is already at max level");
                } else if (click.getType() == Material.STAINED_GLASS_PANE) {
                    event.setCancelled(true);
                } else {
                    if (click.getItemMeta() != null) {
                        if (click.getItemMeta().getDisplayName() != null) {
                            this.onInventoryClick(player, click);
                        }
                    }
                }

            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (this.npc != null && event.getRightClicked() == this.npc) {
            if (this.tribeChunk.tribeData.contains(event.getPlayer())) {
                event.setCancelled(true);

                if (this.openInventoryOnClick) {
                    event.getPlayer().openInventory(this.inventory);
                    S.playSound(event.getPlayer(), Sound.ENDERDRAGON_WINGS);
                }

                this.onClick(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onDamage(CustomDamageEvent event) {
        if(this.npc != null && event.getHurtEntity() == this.npc) {
            if (event.getDamagerPlayer() != null) {
                if (this.tribeChunk.tribeData.contains(event.getDamagerPlayer())) {
                    event.setCancelled(true);

                    if (this.openInventoryOnClick) {
                        event.getDamagerPlayer().openInventory(this.inventory);
                        S.playSound(event.getDamagerPlayer(), Sound.ENDERDRAGON_WINGS);
                    }

                    this.onClick(event.getDamagerPlayer());
                }
            }
        }
    }

    public abstract void onClick(Player player);

    @Override
    public void unload() {
        Bukkit.getServer().getScheduler().cancelTask(this.keepMobStationaryRunnable);
        this.tribe = null;
        HandlerList.unregisterAll(this);
    }

    public abstract void extraUnload();

    //todo: hit detection
    //todo NPC spawning
}
