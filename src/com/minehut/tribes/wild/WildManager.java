package com.minehut.tribes.wild;

import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.wild.commands.WildCommand;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Created by luke on 7/21/15.
 */
public class WildManager implements Listener {
    public RegionManager regionManager;
    public World world;

    public WildManager(Tribes tribes) {
        WorldCreator worldCreator = new WorldCreator("wild");
        this.world = worldCreator.createWorld();

        this.world.getWorldBorder().setSize(1000);
        this.world.setSpawnLocation(-29, 66, 19);

        tribes.registerListener(this);

        new WildCommand(tribes);
        this.regionManager = tribes.worldGuardPlugin.getRegionManager(world);
    }

    public boolean insideWorld(Player player) {
        return player.getWorld() == this.world;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (this.insideWorld(event.getPlayer())) {
            Player player = event.getPlayer();
            Material material = event.getBlockPlaced().getType();

            if (material == Material.CHEST) {
                F.warning(player, "Chests can only be placed in Tribe Worlds");
                event.setCancelled(true);
            } else if (material == Material.ANVIL) {
                F.warning(player, "Ender Chests can only be placed in Tribe Worlds");
                event.setCancelled(true);
            } else if (material == Material.ENDER_CHEST) {
                F.warning(player, "Ender Chests can only be placed in Tribe Worlds");
                event.setCancelled(true);
            } else if (material == Material.TRAPPED_CHEST) {
                F.warning(player, "Chests can only be placed in Tribe Worlds");
                event.setCancelled(true);
            }
        }
    }
}
