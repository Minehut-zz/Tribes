package com.minehut.tribes.weapon;

import com.minehut.core.util.EventUtils;
import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.items.EnchantGlow;
import com.minehut.core.util.common.items.ItemStackFactory;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.damage.event.CustomDamageEvent;
import com.minehut.tribes.util.RegionUtils;
import com.minehut.tribes.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by luke on 6/14/15.
 */
public abstract class Weapon implements Listener {
    private HashMap<UUID, Integer> cooldowns;
    private int runnableID;

    public ItemStack itemStack;
    public String name;
    public int cooldown;

    public ActivateMode activateMode;
    public WeaponType weaponType;
    public Rarity rarity;

    public boolean usableInSafezone = false;

    public Weapon(String name, Material material, int cooldown, ActivateMode activateMode, WeaponType weaponType, Rarity rarity) {
        this.name = name;
        this.cooldown = cooldown;

        this.activateMode = activateMode;
        this.weaponType = weaponType;
        this.rarity = rarity;

        this.cooldowns = new HashMap<>();
        this.runnableID = cooldownRunnable();

        this.itemStack = ItemStackFactory.createItem(material, weaponType.chatColor + C.bold + name,
                Arrays.asList(
                        "",
                        C.gray + "Frequency: " + rarity.formattedName(),
                        "",
                        C.gray + "Item Type: " + weaponType.formattedName(),
                        ""
                ));
        EnchantGlow.addGlow(this.itemStack);

        Tribes.instance.registerListener(this);
    }

    public enum ActivateMode {
        RIGHT,
        LEFT,
        BOTH,
        PASSIVE;
    }

    public enum Rarity {
        COMMON("Common", ChatColor.YELLOW),
        RARE("Rare", ChatColor.AQUA),
        EPIC("Epic", ChatColor.GOLD),
        LEGENDARY("LEGENDARY", ChatColor.LIGHT_PURPLE);

        public String name;
        public ChatColor chatColor;

        private Rarity(String name, ChatColor chatColor) {
            this.name = name;
            this.chatColor = chatColor;
        }

        public String formattedName() {
            if (this == Rarity.LEGENDARY) {
                return chatColor + C.bold + name;
            } else {
                return chatColor + name;
            }
        }
    }

    public enum WeaponType {
        MELEE("Melee", ChatColor.RED),
        RANGED("Ranged", ChatColor.AQUA),
        ARMOR("Armor", ChatColor.YELLOW),
        UTILITY("Utility", ChatColor.LIGHT_PURPLE),
        HEAL("Heal", ChatColor.GREEN);

        public String name;
        public ChatColor chatColor;

        private WeaponType(String name, ChatColor chatColor) {
            this.name = name;
            this.chatColor = chatColor;
        }

        public String formattedName() {
            return chatColor + name;
        }
    }

    private int cooldownRunnable() {
        return Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Tribes.instance, new Runnable() {
            @Override
            public void run() {
                if(cooldowns.isEmpty()) return;

                ArrayList<UUID> remove = new ArrayList<UUID>();
                for (UUID uuid : cooldowns.keySet()) {
                    cooldowns.put(uuid, cooldowns.get(uuid) - 1);

                    if (cooldowns.get(uuid) <= 0) {
                        remove.add(uuid);
                    }
                }

                for (UUID uuid : remove) {
                    cooldowns.remove(uuid);
                    offCooldown(Bukkit.getServer().getPlayer(uuid));
                }
            }
        }, 0L, 0L);
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {

        if(this.activateMode == ActivateMode.RIGHT) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                return;
            }
        }

        if(this.activateMode == ActivateMode.LEFT) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                return;
            }
        }

        if (EventUtils.isItemClickWithDisplayName(event)) {
            if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.itemStack.getItemMeta().getDisplayName())) {
                if (this.isOffCooldown(event.getPlayer())) {
                    if (!this.usableInSafezone) {
                        if (RegionUtils.isInSafezone(event.getPlayer())) {
                            F.warning(event.getPlayer(), "This item is blocked in safe zones");
                            return;
                        }
                    }

                    /* Air Clicks */
                    if (event.getAction() == Action.LEFT_CLICK_AIR) {
                        this.onLeftClick(event.getPlayer());
                        this.onLeftClickAir(event.getPlayer());
                    } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                        this.onRightClick(event.getPlayer());
                        this.onRightClickAir(event.getPlayer());
                    }

                    /* Block Clicks */
                    else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        this.onLeftClick(event.getPlayer());
                        this.onLeftClickBlock(event.getPlayer(), event.getClickedBlock());
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        this.onRightClick(event.getPlayer());
                        this.onRightClickBlock(event.getPlayer(), event.getClickedBlock());
                    }
                } else {
                    F.message(event.getPlayer(), C.yellow + this.name + C.gray + " is usable in " + C.green + TimeUtils.format(this.getCooldown(event.getPlayer())));
                }
            }
        }
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if(this.activateMode == ActivateMode.LEFT || this.activateMode == ActivateMode.BOTH) {
            if (event.getDamagerPlayer() != null) {
                ItemStack item = event.getDamagerPlayer().getItemInHand();
                if (item != null) {
                    if (item.getItemMeta() != null) {
                        if (item.getItemMeta().getDisplayName() != null) {
                            if (item.getItemMeta().getDisplayName().equalsIgnoreCase(this.itemStack.getItemMeta().getDisplayName())) {
                                if (this.isOffCooldown(event.getDamagerPlayer())) {
                                    if (!this.usableInSafezone) {
                                        if (RegionUtils.isInSafezone(event.getDamagerPlayer())) {
                                            F.warning(event.getDamagerPlayer(), "This item is blocked in safe zones");
                                            return;
                                        }
                                    }
                                    if (event.getHurtPlayer() != null) {
                                        this.onHitPlayer(event.getDamagerPlayer(), event.getHurtPlayer());
                                    } else {
                                        this.onHitEntity(event.getDamagerPlayer(), event.getHurtEntity());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteractWithEntity(PlayerInteractAtEntityEvent event) {
        if(this.activateMode == ActivateMode.RIGHT || this.activateMode == ActivateMode.BOTH) {
            if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand() == this.itemStack) {
                if (event.getRightClicked() instanceof Player) {
                    if(this.isOffCooldown(event.getPlayer())) {
                        if (!this.usableInSafezone) {
                            if (RegionUtils.isInSafezone(event.getPlayer())) {
                                F.warning(event.getPlayer(), "This item is blocked in safe zones");
                                return;
                            }
                        }
                        this.onRightClick(event.getPlayer());
                        this.onRightClickPlayer(event.getPlayer(), (Player) event.getRightClicked());
                    }
                }
            }
        }
    }

    public abstract void onHitPlayer(Player player, Player hurt);

    public abstract void onHitEntity(Player player, LivingEntity hurt);

    public abstract void offCooldown(Player player);

    public abstract void onLeftClickAir(Player player);

    public abstract void onRightClickAir(Player player);

    public abstract void onRightClick(Player player);

    public abstract void onLeftClick(Player player);

    public abstract void onLeftClickBlock(Player player, Block block);

    public abstract void onRightClickBlock(Player player, Block block);

    public abstract void onRightClickPlayer(Player player, Player target);

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void putOnCooldown(Player player) {
        this.cooldowns.put(player.getUniqueId(), this.cooldown);
    }

    public boolean isOffCooldown(Player player) {
        if (this.cooldowns.containsKey(player.getUniqueId())) {
            return false;
        }
        return true;
    }

    public int getCooldown(Player player) {
        if (!this.isOffCooldown(player)) {
           return this.cooldowns.get(player.getUniqueId());
        }
        return 0;
    }

    public String getName() {
        return name;
    }

    public abstract void extraUnload();
}
