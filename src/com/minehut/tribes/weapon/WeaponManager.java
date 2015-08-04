package com.minehut.tribes.weapon;

import com.minehut.tribes.Tribes;
import com.minehut.tribes.weapon.commands.WeaponAdminCommand;
import com.minehut.tribes.weapon.weapons.GrenadeWeapon;
import com.minehut.tribes.weapon.weapons.LeapWeapon;
import org.bukkit.event.Listener;

import java.util.ArrayList;

/**
 * Created by luke on 7/30/15.
 */
public class WeaponManager implements Listener {
    public ArrayList<Weapon> weapons;

    public WeaponManager(Tribes tribes) {
        this.weapons = new ArrayList<>();

        this.weapons.add(new LeapWeapon());
        this.weapons.add(new GrenadeWeapon());

        tribes.registerListener(this);

        new WeaponAdminCommand(tribes, this);
    }

    public Weapon getWeapon(String name) {
        for (Weapon weapon : this.weapons) {
            if (weapon.name.equalsIgnoreCase(name)) {
                return weapon;
            }
        }
        return null;
    }
}
