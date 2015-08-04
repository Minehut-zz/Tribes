package com.minehut.tribes.tribe;

import com.google.gson.Gson;
import com.minehut.core.util.common.chat.C;
import com.minehut.core.util.common.chat.F;
import com.minehut.core.util.common.sound.S;
import com.minehut.tribes.Tribes;
import com.minehut.tribes.tribe.building.buildings.BlackSmithBuilding;
import com.minehut.tribes.tribe.building.buildings.StablesBuilding;
import com.minehut.tribes.tribe.player.TribePlayer;
import com.minehut.tribes.tribe.player.TribePlayerManager;
import com.minehut.tribes.tribe.building.BuildingType;
import com.minehut.tribes.tribe.building.buildings.BarracksBuilding;
import com.minehut.tribes.tribe.building.buildings.TownHallBuilding;
import com.minehut.tribes.tribe.chunk.TribeChunk;
import com.minehut.tribes.tribe.player.TribalRank;
import com.minehut.tribes.tribe.troop.Troop;
import com.minehut.tribes.tribe.troop.TroopType;
import com.minehut.tribes.util.WorldUtils;
import com.minehut.tribes.world.NullChunkGenerator;
import com.minehut.tribes.world.WorldLoader;
import com.mongodb.*;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by luke on 7/19/15.
 */
public class TribeManager implements Listener {
    public TribePlayerManager tribePlayerManager;

    public DBCollection tribeCollection;

    public ArrayList<Tribe> loadedTribes;
    public ArrayList<TribeData> tribeDatas;
    public ArrayList<TribeData> loading;

    public TribeManager() {
        this.tribeDatas = new ArrayList<>();
        this.loading = new ArrayList<>();
        this.tribeCollection = Tribes.instance.db.getCollection("tribes");
        this.tribePlayerManager = new TribePlayerManager(this);

        DBCursor cursor = this.tribeCollection.find();

        int i = 1;
        while (cursor.hasNext()) {
            DBObject found = cursor.next();

            UUID uuid = (UUID) found.get("uuid");
            String name = (String) found.get("name");

            long coins = (long) found.get("coins");

            Gson gson = new Gson();

            BasicDBList dataPlayersDB = (BasicDBList) found.get("dataPlayers");

            ArrayList<DataPlayer> dataPlayers = new ArrayList<>();
            for (Object dataPlayer : dataPlayersDB) {
                dataPlayers.add(gson.fromJson((String) dataPlayer, DataPlayer.class));
            }

            TribeData tribeData = new TribeData(uuid, name, coins, dataPlayers);

            /* ################################## */

            ArrayList<TribeChunk> tribeChunks = new ArrayList<>();
            BasicDBList tribeChunksDB = (BasicDBList) found.get("tribeChunks");
            for (Object json : tribeChunksDB) {
                TribeChunk tribeChunk = gson.fromJson((String) json, TribeChunk.class);
                tribeChunk.tribeData = tribeData;

                if(tribeChunk.tribeChunkType == TribeChunk.TribeChunkType.core) {
                    BuildingType buildingType = tribeChunk.buildingType;
                    if(buildingType != null) {
                        if (buildingType == BuildingType.townhall) {
                            tribeChunk.building = new TownHallBuilding(tribeChunk, tribeChunk.buildingLevel);
                        } else if (buildingType == BuildingType.barracks) {
                            tribeChunk.building = new BarracksBuilding(tribeChunk, tribeChunk.buildingLevel);
                        } else if (buildingType == BuildingType.stables) {
                            tribeChunk.building = new StablesBuilding(tribeChunk, tribeChunk.buildingLevel);
                        } else if (buildingType == BuildingType.blacksmith) {
                            tribeChunk.building = new BlackSmithBuilding(tribeChunk, tribeChunk.buildingLevel);
                        }
                    } else {
                        //todo: add wall an random stuff here
                    }
                }


                tribeChunks.add(tribeChunk);
            }
            tribeData.tribeChunks = tribeChunks;

            ArrayList<Troop> troops = new ArrayList<>();
            BasicDBList troopsDB = (BasicDBList) found.get("troops");
            for (Object json : troopsDB) {
                Troop troop = gson.fromJson((String) json, Troop.class);
                troop.tribeData = tribeData;
                troops.add(troop);
            }
            tribeData.troops = troops;
            /* ################################## */


            this.tribeDatas.add(tribeData);

            F.log("Loaded Tribe " + i + " " + name);
            i++;
        }


        this.loadedTribes = new ArrayList<>();

        this.onlineChecker();

        Tribes.instance.registerListener(this);

        new TribeListeners();
    }

    /* ###################################################### */

    public void createNewTribe(Player player, String name) {

        F.message(player, "Creating your new tribe...");
        F.message(player, "This may take a few moments...");

        UUID uuid = UUID.randomUUID();

        ArrayList<DataPlayer> dataPlayers = new ArrayList<>();
        dataPlayers.add(new DataPlayer(player.getName(), player.getUniqueId(), TribalRank.OWNER));

        TribeData tribeData = new TribeData(uuid, name, 0, dataPlayers);

        TribePlayer tribePlayer = tribePlayerManager.getTribePlayer(player);
        tribePlayer.setTribeData(tribeData);

        this.loading.add(tribeData);

        ArrayList<Troop> troops = new ArrayList<>();

        troops.add(new Troop(TroopType.zombie, tribeData, 1));
        troops.add(new Troop(TroopType.zombie, tribeData, 1));
        troops.add(new Troop(TroopType.zombie, tribeData, 1));

        troops.add(new Troop(TroopType.archer, tribeData, 1));
        troops.add(new Troop(TroopType.archer, tribeData, 1));

        /* ############################################## */

        ArrayList<TribeChunk> tribeChunks = new ArrayList<>();

        TribeChunk townHall = new TribeChunk(tribeData, 0, 0, TribeChunk.TribeChunkType.core, BuildingType.townhall, 1, null);
        TribeChunk barracks = new TribeChunk(tribeData, 1, 1, TribeChunk.TribeChunkType.core, BuildingType.barracks, 1, null);
        TribeChunk stables = new TribeChunk(tribeData, -1, 1, TribeChunk.TribeChunkType.core, BuildingType.stables, 1, null);
        TribeChunk blacksmith = new TribeChunk(tribeData, -1, -1, TribeChunk.TribeChunkType.core, BuildingType.blacksmith, 1, null);

        tribeChunks.add(townHall);
        tribeChunks.add(barracks);
        tribeChunks.add(stables);
        tribeChunks.add(blacksmith);

        /* Wild Plots */
        tribeChunks.add(new TribeChunk(tribeData, 2, 3, TribeChunk.TribeChunkType.wild, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 1, 3, TribeChunk.TribeChunkType.wild, null, 1, null));
//        tribeChunks.add(new TribeChunk(tribeData, 0, 3, TribeChunk.TribeChunkType.wild, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -1, 3, TribeChunk.TribeChunkType.wild, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -2, 3, TribeChunk.TribeChunkType.wild, null, 1, null));

        tribeChunks.add(new TribeChunk(tribeData, 2, 4, TribeChunk.TribeChunkType.wild, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 1, 4, TribeChunk.TribeChunkType.wild, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 0, 4, TribeChunk.TribeChunkType.wild, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -1, 4, TribeChunk.TribeChunkType.wild, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -2, 4, TribeChunk.TribeChunkType.wild, null, 1, null));

        /* Walls */
        tribeChunks.add(new TribeChunk(tribeData, 0, 2, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -1, 2, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -2, 2, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -2, 1, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -2, 0, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -2, -1, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -2, -2, TribeChunk.TribeChunkType.core, null, 1, null));

        tribeChunks.add(new TribeChunk(tribeData, -1, -2, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 0, -2, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 1, -2, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 2, -2, TribeChunk.TribeChunkType.core, null, 1, null));

        tribeChunks.add(new TribeChunk(tribeData, 2, -1, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 2, 0, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 2, 1, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 2, 2, TribeChunk.TribeChunkType.core, null, 1, null));

        tribeChunks.add(new TribeChunk(tribeData, 1, 2, TribeChunk.TribeChunkType.core, null, 1, null));

        /* Insides */
        tribeChunks.add(new TribeChunk(tribeData, 0, 1, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, -1, 0, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 0, -1, TribeChunk.TribeChunkType.core, null, 1, null));
        tribeChunks.add(new TribeChunk(tribeData, 1, 0, TribeChunk.TribeChunkType.core, null, 1, null));

        /* ############################################## */

        tribeData.troops = troops;
        tribeData.tribeChunks = tribeChunks;

        for (TribeChunk tribeChunk : tribeChunks) {
            if(tribeChunk.tribeChunkType == TribeChunk.TribeChunkType.core) {
                BuildingType buildingType = tribeChunk.buildingType;
                if(buildingType != null) {
                    if (buildingType == BuildingType.townhall) {
                        tribeChunk.building = new TownHallBuilding(tribeChunk, tribeChunk.buildingLevel);
                    } else if (buildingType == BuildingType.barracks) {
                        tribeChunk.building = new BarracksBuilding(tribeChunk, tribeChunk.buildingLevel);
                    } else if (buildingType == BuildingType.stables) {
                        tribeChunk.building = new StablesBuilding(tribeChunk, tribeChunk.buildingLevel);
                    } else if (buildingType == BuildingType.blacksmith) {
                        tribeChunk.building = new BlackSmithBuilding(tribeChunk, tribeChunk.buildingLevel);
                    }
                } else {

                }
            }
        }

        this.tribeDatas.add(tribeData);

        this.tribeCollection.insert(this.createTribeDataDBObject(tribeData));

        try {

            File folder = new File("tribes/" + uuid.toString());
            folder.mkdir();

            File dest = new File("tribes/" + uuid.toString() + "/world/");
            dest.mkdir();

            FileUtils.copyDirectory(new File("template/world/"), dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        World world = WorldLoader.createAsyncWorld(new WorldCreator("tribes/" + uuid.toString() + "/world").generator(new NullChunkGenerator()));
        world.setSpawnLocation(8, 65, 8);

        Tribe tribe = new Tribe(tribeData, world);
        this.loadedTribes.add(tribe);

        this.loading.remove(tribeData);

        if (player != null) {
            F.message(player, "Your tribe has loaded up!" + C.green + " /tribe home", F.BroadcastType.FULL_BORDER);
            S.playSound(player, Sound.FIREWORK_LARGE_BLAST);

        }
    }


    public void updateTribeDataInDatabase(TribeData tribeData) {
        DBObject obj = this.createTribeDataDBObject(tribeData);
        DBObject query = new BasicDBObject("uuid", tribeData.uuid);

        this.tribeCollection.findAndModify(query, obj);
    }

    public void removeFromDatabase(TribeData tribeData) {
        DBObject query = new BasicDBObject("uuid", tribeData.uuid);
        this.tribeCollection.findAndRemove(query);
    }

    public void onlineChecker() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Tribes.instance, new Runnable() {
            @Override
            public void run() {
                ArrayList<Tribe> toRemove = new ArrayList<Tribe>();

                if (!loadedTribes.isEmpty()) {
                    for (Tribe tribe : loadedTribes) {
                        if (tribe.getOnlinePlayers().size() == 0) {
                            tribe.unload();
                            toRemove.add(tribe);
                        }
                    }
                }

                for (Tribe tribe : toRemove) {
                    int index = loadedTribes.indexOf(tribe);
                    F.log("Unloading Tribe " + tribe.tribeData.name + ", index of loadedTribes was " + index);
                    loadedTribes.remove(index);
                }
            }
        }, 60 * 20L, 10 * 20L);
    }

    public TribeData getTribeData(String name) {
        for (TribeData tribeData : this.tribeDatas) {
            if (tribeData.name.equalsIgnoreCase(name)) {
                return tribeData;
            }
        }
        return null;
    }

    public TribeData getTribeData(Player player) {
        for (TribeData tribeData : this.tribeDatas) {
            if (tribeData.contains(player)) {
                return tribeData;
            }
        }
        return null;
    }



    /*
       Only use to see if their tribe is
       online. If you need to check if
       they are in a tribe, try to
       get their TribeData.
     */
    public Tribe getTribe(Player player) {
        for (Tribe tribe : this.loadedTribes) {
            if (tribe.contains(player)) {
                return tribe;
            }
        }

        return null;
    }

    public Tribe getTribe(String name) {
        for (Tribe tribe : this.loadedTribes) {
            if (tribe.tribeData.name.equalsIgnoreCase(name)) {
                return tribe;
            }
        }

        return null;
    }

    private DBObject createTribeDataDBObject(TribeData tribeData) {
        Gson gson = new Gson();

        DBObject obj = new BasicDBObject("uuid", tribeData.uuid);

        obj.put("name", tribeData.name);
        obj.put("coins", tribeData.coins);

        ArrayList<String> dataPlayersJson = new ArrayList<>();
        for (DataPlayer dataPlayer : tribeData.dataPlayers) {
            dataPlayersJson.add(gson.toJson(dataPlayer));
        }
        obj.put("dataPlayers", dataPlayersJson);

        ArrayList<String> troopsJson = new ArrayList<>();
        for (Troop troop : tribeData.troops) {
            troopsJson.add(gson.toJson(troop));
        }
        obj.put("troops", troopsJson);

        ArrayList<String> tribeChunksJson = new ArrayList<>();
        for (TribeChunk tribeChunk : tribeData.tribeChunks) {
            tribeChunksJson.add(gson.toJson(tribeChunk));
        }
        obj.put("tribeChunks", tribeChunksJson);

        return obj;
    }

    public Tribe startTribe(TribeData tribeData) {
        this.loading.add(tribeData);
        World world = WorldLoader.createAsyncWorld(new WorldCreator("tribes/" + tribeData.uuid.toString() + "/world").generator(new NullChunkGenerator()));
        world.setGameRuleValue("doMobSpawning", "false");
        world.setSpawnLocation(8, 65, 8);
        world.getSpawnLocation().setYaw(0);

        WorldUtils.removeHostileEntities(world);

        if(this.loading.contains(tribeData)) {
            Tribe tribe = new Tribe(tribeData, world);
            this.loadedTribes.add(tribe);

            this.loading.remove(tribeData);

            for (Player player : tribe.getOnlinePlayers()) {
                F.message(player, "Your tribe has loaded up!" + C.green + " /tribe home", F.BroadcastType.FULL_BORDER);
                S.playSound(player, Sound.FIREWORK_LARGE_BLAST);
            }

            return tribe;
        } else {
            /* Was deleted while loading up */
            Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                @Override
                public void run() {
                    try {
                        FileUtils.deleteDirectory(new File("tribes/" + tribeData.uuid.toString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    removeFromDatabase(tribeData);
                }
            });

            return null;
        }
    }

    public void removeFromTribe(DataPlayer dataPlayer, TribeData tribeData) {
        tribeData.dataPlayers.remove(dataPlayer);

        Player player = Bukkit.getServer().getPlayer(dataPlayer.uuid);
        if(player != null) {
            TribePlayer tribePlayer = this.tribePlayerManager.getTribePlayer(player);
            tribePlayer.setTribeData(null);
            player.teleport(Tribes.instance.spawnHandler.getSpawnLocation());

            F.message(player, C.red + "You were kicked from the " + C.purple + tribeData.name + C.yellow + " tribe!");
            S.playSound(player, Sound.GLASS);
        }

        Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
            @Override
            public void run() {
                updateTribeDataInDatabase(tribeData);
            }
        });


        for (Player player1 : tribeData.getOnlinePlayers()) {
            F.message(player1, C.purple + dataPlayer.name + C.yellow + " was " + C.red + "kicked" + C.yellow + " from your tribe!");
            S.playSound(player1, Sound.GLASS);
        }
    }

    public void leaveTribe(Player player, TribeData tribeData) {
        if (tribeData.contains(player)) {
            boolean dispanded = false;

            if (tribeData.isOwner(player.getUniqueId())) {
                if (tribeData.getDataPlayersOfTribalRank(TribalRank.ELDER).isEmpty()) {
                    if (tribeData.getDataPlayersOfTribalRank(TribalRank.MEMBER).isEmpty()) {
                        dispanded = true;
                    } else {
                        tribeData.setOwner(tribeData.getDataPlayersOfTribalRank(TribalRank.MEMBER).get(0).uuid);
                    }
                } else {
                    tribeData.setOwner(tribeData.getDataPlayersOfTribalRank(TribalRank.ELDER).get(0).uuid);
                }
            }

            TribePlayer tribePlayer = this.tribePlayerManager.getTribePlayer(player);
            tribeData.dataPlayers.remove(tribeData.getDataPlayer(player.getUniqueId()));
            tribePlayer.setTribeData(null);

            Tribe tribe = this.getTribe(tribeData.name);
            if (tribe != null) {
                if (tribe.isLocatedInsideTribe(player)) {
                    player.teleport(Tribes.instance.spawnHandler.getSpawnLocation());
                }
            }

            for (Player partOfTribe : tribeData.getOnlinePlayers()) {
                if (partOfTribe != player) {
                    F.message(partOfTribe, C.aqua + player.getDisplayName() + C.yellow + " has left your tribe!");
                }
            }

            if (dispanded) {

                if (tribe != null) {
                    tribe.unload();
                }

                Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileUtils.deleteDirectory(new File("tribes/" + tribeData.uuid.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                F.broadcast("The Tribe " + C.aqua + tribeData.name + C.yellow + " was " + C.red + "disbanded", F.BroadcastType.MINIMAL_BORDER);


                int index = loadedTribes.indexOf(tribe);
                F.log("Unloading Tribe " + tribe.tribeData.name + ", index of loadedTribes was " + index);
                loadedTribes.remove(index);

                this.tribeDatas.remove(tribeData);

                Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                    @Override
                    public void run() {
                        removeFromDatabase(tribeData);
                    }
                });


            } else {
                F.message(player, "You have left " + C.purple + tribeData.name);

                Bukkit.getServer().getScheduler().runTaskAsynchronously(Tribes.instance, new Runnable() {
                    @Override
                    public void run() {
                        updateTribeDataInDatabase(tribeData);
                    }
                });
            }

        }
    }

    public boolean isNameAvaliable(String name) {
        for (TribeData tribeData : this.tribeDatas) {
            if (tribeData.name.equals(name)) {
                return false;
            }
        }

        return true;
    }

    public boolean inSameTribe(Player p1, Player p2) {
        TribeData tribeData = this.tribePlayerManager.getTribePlayer(p1).tribeData;

        if (tribeData != null) {
            if (tribeData.contains(p2)) {
                return true;
            }
        }
        return false;
    }

    public long depositCoins(TribeData tribeData, long amount, String reason) {

        DBObject query = new BasicDBObject("uuid",tribeData.uuid);
        DBObject found = tribeCollection.findOne(query);

        long oldCoins = (long) found.get("coins");
        long updatedCoins = oldCoins + amount;

        found.put("coins", updatedCoins);
        tribeCollection.findAndModify(query, found);

        tribeData.setCoins(updatedCoins);

        if (reason != null) {
            Tribe tribe = this.getTribe(tribeData.name);

            if (tribe != null) {
                for (Player player : tribe.getOnlinePlayers()) {
                    S.playSound(player, Sound.LEVEL_UP);

                    String s;
                    if(amount > 0) {
                        s = C.green + C.scramble + "GGG" + C.yellow + " " + reason + C.yellow + " " + C.green + C.scramble + "GGG";
                    } else {
                        s = C.red + C.scramble + "GGG" + C.yellow + " " + reason + C.yellow + " " + C.red + C.scramble + "GGG";
                    }

                    F.message(player, s);
                }
            }
        }

        return updatedCoins;
    }

}
