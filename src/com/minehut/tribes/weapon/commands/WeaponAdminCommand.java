package com.minehut.tribes.weapon.commands;

import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.weapon.Weapon;
import com.minehut.tribes.weapon.WeaponManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by luke on 7/23/15.
 */
public class WeaponAdminCommand extends Command {
    WeaponManager weaponManager;

    public WeaponAdminCommand(JavaPlugin plugin, WeaponManager weaponManager) {
        super(plugin, "weaponadmin", Arrays.asList("wa"), Rank.Admin);
        this.weaponManager = weaponManager;
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        if (args == null || args.size() == 0) {
            F.warning(player, "/wa (weapon)");
            return true;
        }

        if (args.size() == 1) {
            Weapon weapon = weaponManager.getWeapon(args.get(0));

            if (weapon != null) {
                player.getInventory().addItem(weapon.getItemStack());
                S.pop(player);
                F.message(player, "Successfully spawned " + C.purple + weapon.getName());
            } else {
                F.warning(player, "Could not find a weapon named " + C.purple + args.get(0));
            }
        }

        return false;
    }
}
