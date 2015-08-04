package com.minehut.tribes.weapon.weapons;

import com.minehut.core.util.common.items.ItemStackFactory;
import com.minehut.core.util.common.sound.S;
import com.minehut.core.util.common.velocity.UtilAction;
import com.minehut.tribes.util.CheatUtils;
import com.minehut.tribes.weapon.Weapon;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Created by luke on 7/16/15.
 */
public class LeapWeapon extends Weapon {

    public LeapWeapon() {
        super("Leap", Material.STONE_AXE, 80, ActivateMode.RIGHT, WeaponType.UTILITY, Rarity.RARE);
    }

    @Override
    public void onRightClick(Player player) {
        CheatUtils.exemptFlight(player);
        UtilAction.velocity(player, player.getLocation().getDirection(), 1.5, true, .3, 0.1d, 1.5, true);
        S.playSound(player, Sound.ENDERDRAGON_WINGS);
        super.putOnCooldown(player);
        CheatUtils.unexemptFlight(player, 40);
    }

    @Override
    public void onRightClickAir(Player player) {

    }

    @Override
    public void onHitPlayer(Player player, Player hurt) {

    }

    @Override
    public void onHitEntity(Player player, LivingEntity hurt) {

    }

    @Override
    public void offCooldown(Player player) {

    }

    @Override
    public void onLeftClickAir(Player player) {

    }

    @Override
    public void onLeftClick(Player player) {

    }

    @Override
    public void onLeftClickBlock(Player player, Block block) {

    }

    @Override
    public void onRightClickBlock(Player player, Block block) {

    }

    @Override
    public void onRightClickPlayer(Player player, Player target) {

    }

    @Override
    public void extraUnload() {

    }
}
