package com.minehut.tribes;

import com.minehut.core.command.commands.ChunkCommand;
import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.chat.ChatManager;
import com.minehut.tribes.command.CoinsCommand;
import com.minehut.tribes.teleport.commands.SpawnCommand;
import com.minehut.tribes.command.TribeComand;
import com.minehut.tribes.connection.ConnectionHandler;
import com.minehut.tribes.damage.DamageManager;
import com.minehut.tribes.spawn.SpawnHandler;
import com.minehut.tribes.spawn.shop.ShopManager;
import com.minehut.tribes.teleport.TeleportManager;
import com.minehut.tribes.tribe.TribeManager;
import com.minehut.tribes.tribe.chunk.commands.TribeChunkCommand;
import com.minehut.tribes.weapon.WeaponManager;
import com.minehut.tribes.wild.WildManager;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by luke on 7/19/15.
 */
public class Tribes extends JavaPlugin {
    public static Tribes instance;

    /* Database */
    public MongoClient mongo;
    public DB db;

    public SpawnHandler spawnHandler;
    public ConnectionHandler connectionHandler;
    public TribeManager tribeManager;
    public DamageManager damageManager;
    public ShopManager shopManager;
    public TeleportManager teleportManager;
    public WildManager wildManager;
    public WeaponManager weaponManager;
    public ChatManager chatManager;

    public WorldGuardPlugin worldGuardPlugin;


    @Override
    public void onEnable() {
        instance = this;

        this.worldGuardPlugin = this.getWorldGuard();

        this.connect();

        this.spawnHandler = new SpawnHandler();
        this.connectionHandler = new ConnectionHandler();
        this.tribeManager = new TribeManager();
        this.damageManager = new DamageManager(this);
        this.shopManager = new ShopManager(this);
        this.teleportManager = new TeleportManager(this);
        this.wildManager = new WildManager(this);
        this.weaponManager = new WeaponManager(this);
        this.chatManager = new ChatManager(this);

        /* Commands */
        new TribeComand(this);
        new CoinsCommand(this);
        new TribeChunkCommand(this);
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            F.log("Couldn't find WorldGuardPlugin");
            return null;
        }

        return (WorldGuardPlugin) plugin;
    }

    private void connect() {
        try {
            this.mongo = new MongoClient("mc.minehut.com", 27017);
            this.db = mongo.getDB("minehut");
            db.addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerListener(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }
}
