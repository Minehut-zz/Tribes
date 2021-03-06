package com.minehut.tribes.world;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;

import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import net.minecraft.server.v1_8_R3.ChunkRegionLoader;
import net.minecraft.server.v1_8_R3.Convertable;
import net.minecraft.server.v1_8_R3.CrashReport;
import net.minecraft.server.v1_8_R3.CrashReportSystemDetails;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EnumDifficulty;
import net.minecraft.server.v1_8_R3.IChunkProvider;
import net.minecraft.server.v1_8_R3.IDataManager;
import net.minecraft.server.v1_8_R3.IProgressUpdate;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.ReportedException;
import net.minecraft.server.v1_8_R3.ServerNBTManager;
import net.minecraft.server.v1_8_R3.WorldData;
import net.minecraft.server.v1_8_R3.WorldLoaderServer;
import net.minecraft.server.v1_8_R3.WorldManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import net.minecraft.server.v1_8_R3.WorldSettings;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldLoader {
	private static World ret = null;
	private static boolean aborted = false;
	private static boolean alreadyLoading = false;
	private static ChunkGenerator generator;
	private static Chunk wait = null;

	public static World createAsyncWorld(WorldCreator creator) {
		while (alreadyLoading) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


		alreadyLoading = true;
		aborted = false;
		wait = null;
		generator = null;
		ret = null;
		Validate.notNull(creator, "Creator may not be null");

		String name = creator.name();
		generator = creator.generator();
		File folder = new File(getWorldContainer(), name);
		World world = getCraftServer().getWorld(name);

		

		if (world != null) {
			return world;
		}

		if ((folder.exists()) && (!folder.isDirectory())) {
			throw new IllegalArgumentException("File exists with the name '"
					+ name + "' and isn't a folder");
		}

		if (generator == null) {
			generator = getGenerator(name);
		}

		Convertable converter = new WorldLoaderServer(getWorldContainer());
		if (converter.isConvertable(name)) {
			Bukkit.getLogger().info("Converting world '" + name + "'");
			converter.convert(name, new IProgressUpdate() {
				private long b = System.currentTimeMillis();

				public void a(String s) {
				}

				public void a(int i) {
					if (System.currentTimeMillis() - this.b >= 1000L) {
						this.b = System.currentTimeMillis();
						MinecraftServer.LOGGER.info("Converting... " + i + "%");
					}
				}

				public void c(String s) {
				}
			});
		}
		new WorldLoaderThreadRunnable(creator).runTask(Tribes.instance);
		while (ret == null && !aborted) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		alreadyLoading = false;

		return ret;
	}
	
	public static class WorldLoaderThreadRunnable extends BukkitRunnable {
		
		private WorldCreator creator;
		
		public WorldLoaderThreadRunnable(WorldCreator creator) {
			this.creator = creator;
		}
		
		@Override
		public void run() {

			int dimension2 = 10 + getServer().worlds.size();
			boolean used = false;
			do
				for (WorldServer server : getServer().worlds) {
					used = server.dimension == dimension2;
					if (used) {
						dimension2++;
						break;
					}
				}
			while (used);
			boolean hardcore = false;
			final int dimension = dimension2;
			new WorldLoaderThread(creator, dimension).start();
			/*new Thread() {
				//TODO
			}.start();*/
		}
		
	}

	public static class WorldLoaderThread extends Thread {
		
		private WorldCreator creator;
		private int dimension;
		
		public WorldLoaderThread(WorldCreator creator, int dimension) {
			this.creator = creator;
			this.dimension = dimension;
		}
		 
		@Override
		public void run() {
			String name = creator.name();
			boolean generateStructures = creator.generateStructures();
			boolean hardcore = false;
			net.minecraft.server.v1_8_R3.WorldType type = net.minecraft.server.v1_8_R3.WorldType
					.getType(creator.type().getName());
			
			Object sdm = new ServerNBTManager(getWorldContainer(),
					name, true);
			WorldData worlddata = ((IDataManager) sdm)
					.getWorldData();
			if (worlddata == null) {
				WorldSettings worldSettings = new WorldSettings(
						creator.seed(),
						WorldSettings.EnumGamemode
								.getById(getCraftServer()
										.getDefaultGameMode()
										.getValue()),
						generateStructures, hardcore, type);
				worldSettings.setGeneratorSettings(creator
						.generatorSettings());
				worlddata = new WorldData(worldSettings, name);
			}
			worlddata.checkName(name);
			WorldServer internal = (WorldServer) new WorldServer(
					getServer(), (IDataManager) sdm, worlddata,
					dimension, getServer().methodProfiler,
					creator.environment(), generator).b();
			new InternalRunnable(internal, name).runTask(Tribes.instance);
		}
	}
	
	private static File getWorldContainer() {
		if (getServer().universe != null) {
			return getServer().universe;
		}
		try {
			Field container = CraftServer.class.getDeclaredField("container");
			container.setAccessible(true);
			Field settings = CraftServer.class.getDeclaredField("configuration");
			settings.setAccessible(true);
			File co = (File) container.get(getCraftServer());
			if (co == null) {
				container.set(
						getCraftServer(),
						new File(((YamlConfiguration) settings
								.get(getCraftServer())).getString(
								"settings.world-container", ".")));
			}	

			return (File) container.get(getCraftServer());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	

	public static class InternalRunnable extends BukkitRunnable {
		
		private String name;
		
		private WorldServer internal;
		
		public InternalRunnable(WorldServer internal, String name) {
			this.internal = internal;
			this.name = name;
		}
		
		
		public void run() {
			try {
				Field w = CraftServer.class
						.getDeclaredField("worlds");
				w.setAccessible(true);
				if (!((Map<String, World>) w
						.get(getCraftServer()))
						.containsKey(name.toLowerCase())) {
					aborted = true;
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				aborted = true;
				return;
			}
			new Thread() {
				public void run() {

					internal.scoreboard = getCraftServer()
							.getScoreboardManager()
							.getMainScoreboard()
							.getHandle();

					internal.tracker = new EntityTracker(
							internal);
					internal.addIWorldAccess(new WorldManager(
							getServer(), internal));
					internal.worldData
							.setDifficulty(EnumDifficulty.EASY);
					internal.setSpawnFlags(true, true);
					getServer().worlds.add(internal);

					if (generator != null) {
						internal.getWorld()
								.getPopulators()
								.addAll(generator
										.getDefaultPopulators(internal
												.getWorld()));
					}

					new BukkitRunnable() {
						public void run() {
							Bukkit.getPluginManager()
									.callEvent(
											new WorldInitEvent(
													internal.getWorld()));
						}
					}.runTask(Tribes.instance);
					System.out
							.print("Preparing start region for level "
									+ (getServer().worlds
											.size() - 1)
									+ " (Seed: "
									+ internal.getSeed()
									+ ")");
					if (internal.getWorld()
							.getKeepSpawnInMemory()) {
						short short1 = 196;
						long i = System.currentTimeMillis();
						for (int j = -short1; j <= short1; j += 16) {
							for (int k = -short1; k <= short1; k += 16) {
								long l = System
										.currentTimeMillis();

								if (l < i) {
									i = l;
								}

								if (l > i + 1000L) {
									int i1 = (short1 * 2 + 1)
											* (short1 * 2 + 1);
									int j1 = (j + short1)
											* (short1 * 2 + 1)
											+ k + 1;

									System.out
											.println("Preparing spawnLocation area for "
													+ name
													+ ", "
													+ j1
													* 100
													/ i1
													+ "%");
									i = l;
								}

								BlockPosition chunkcoordinates = internal
										.getSpawn();
								getChunkAt(internal.chunkProviderServer,
										
												chunkcoordinates
														.getX()
														+ j >> 4,
												chunkcoordinates
														.getZ()
														+ k >> 4);
							}
						}
					}
					new BukkitRunnable() {
						public void run() {
							Bukkit.getPluginManager()
									.callEvent(
											new WorldLoadEvent(
													internal.getWorld()));
						}
					}.runTask(Tribes.instance);
					ret = (World) internal.getWorld();
				}

				private Chunk getChunkAt(
						ChunkProviderServer cps,
						int i, int j) {
					Runnable runnable = null;
					cps.unloadQueue.remove(i, j);
				    Chunk chunk = (Chunk)cps.chunks.get(LongHash.toLong(i, j));
				    ChunkRegionLoader loader = null;
				    try{
				    Field f = ChunkProviderServer.class.getDeclaredField("chunkLoader");
				    f.setAccessible(true);
				    if ((f.get(cps) instanceof ChunkRegionLoader)) {
				      loader = (ChunkRegionLoader)f.get(cps);
				    }
				    }catch(Exception e){
				    	e.printStackTrace();
				    }

				    if ((chunk == null) && (loader != null) && (loader.chunkExists(cps.world, i, j))) {
				    final ChunkRegionLoader loader1 = loader;
				      wait = null;
				      
				      new SetWaitRunnable(cps, loader1, i, j).runTask(Tribes.instance);
				      
				      
				      while(wait==null){
				    	  try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				      }
				      chunk = wait;
				    }
				    else if (chunk == null) {
				      chunk = originalGetChunkAt(cps, i, j);
				    }

				    if (runnable != null) {
				      runnable.run();
				    }

				    return chunk;
				}
				
				class SetWaitRunnable extends BukkitRunnable {

					private ChunkProviderServer cps;
					private ChunkRegionLoader loader1;
					private int i, j;
					
					public SetWaitRunnable(ChunkProviderServer cps, ChunkRegionLoader loader1, int i, int j) {
						this.cps = cps;
						this.loader1 = loader1;
						this.i = i;
						this.j = j;
					}
					
					@Override
					public void run() {
						wait = ChunkIOExecutor.syncChunkLoad(cps.world, loader1, cps, i, j);
					      
					}
					
				}
				public Chunk originalGetChunkAt(ChunkProviderServer cps, int i, int j) {
				    cps.unloadQueue.remove(i, j);
				    Chunk chunk = (Chunk)cps.chunks.get(LongHash.toLong(i, j));
				    boolean newChunk = false;

				    if (chunk == null) {
				    	cps.world.timings.syncChunkLoadTimer.startTiming();
				      chunk = cps.loadChunk(i, j);
				      if (chunk == null) {
				        if (cps.chunkProvider == null)
				          chunk = cps.emptyChunk;
				        else {
				          try {
				            chunk = cps.chunkProvider.getOrCreateChunk(i, j);
				          } catch (Throwable throwable) {
				            CrashReport crashreport = CrashReport.a(throwable, "Exception generating new chunk");
				            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Chunk to be generated");

				            crashreportsystemdetails.a("Location", String.format("%d,%d", new Object[] { Integer.valueOf(i), Integer.valueOf(j) }));
				            crashreportsystemdetails.a("Position hash", Long.valueOf(LongHash.toLong(i, j)));
				            crashreportsystemdetails.a("Generator", cps.chunkProvider.getName());
				            throw new ReportedException(crashreport);
				          }
				        }
				        newChunk = true;
				      }

				      cps.chunks.put(LongHash.toLong(i, j), chunk);
				      final Chunk chunki = chunk;
				      final boolean newChunki = newChunk;
				      
				      
				      
				      
				      
				      new CallEventRunnable(cps, chunki, newChunki).runTask(Tribes.instance);
				      
				      
				      
				      

				      for (int x = -2; x < 3; x++) {
				        for (int z = -2; z < 3; z++) {
				          if ((x == 0) && (z == 0))
				          {
				            continue;
				          }
				          Chunk neighbor = cps.getChunkIfLoaded(chunk.locX + x, chunk.locZ + z);
				          if (neighbor != null) {
				            neighbor.setNeighborLoaded(-x, -z);
				            chunk.setNeighborLoaded(x, z);
				          }
				        }
				      }

				      loadNearby(chunk, cps, cps, i, j);
				      cps.world.timings.syncChunkLoadTimer.stopTiming();
				    }

				    return chunk;
				  }
				
				
				class CallEventRunnable extends BukkitRunnable {
					
					private ChunkProviderServer cps;
					private Chunk chunki;
					private boolean newChunki;
					
					public CallEventRunnable(ChunkProviderServer cps, Chunk chunki, boolean newChunki) {
						this.cps = cps;
						this.chunki = chunki;
						this.newChunki = newChunki;
					}
					
					@Override
					public void run(){
					      chunki.addEntities();
					      Server server = cps.world.getServer();
					      if (server != null) {
					        server.getPluginManager().callEvent(new ChunkLoadEvent(chunki.bukkitChunk, newChunki));
					      }
					      }
				}
				
				public void loadNearby(Chunk c, IChunkProvider ichunkprovider, IChunkProvider ichunkprovider1, int i, int j) {
				    c.world.timings.syncChunkLoadPostTimer.startTiming();
				    boolean flag = ichunkprovider.isChunkLoaded(i, j - 1);
				    boolean flag1 = ichunkprovider.isChunkLoaded(i + 1, j);
				    boolean flag2 = ichunkprovider.isChunkLoaded(i, j + 1);
				    boolean flag3 = ichunkprovider.isChunkLoaded(i - 1, j);
				    boolean flag4 = ichunkprovider.isChunkLoaded(i - 1, j - 1);
				    boolean flag5 = ichunkprovider.isChunkLoaded(i + 1, j + 1);
				    boolean flag6 = ichunkprovider.isChunkLoaded(i - 1, j + 1);
				    boolean flag7 = ichunkprovider.isChunkLoaded(i + 1, j - 1);

				    if ((flag1) && (flag2) && (flag5)) {
				      if (!c.isDone())
				        getChunkAt((ChunkProviderServer) ichunkprovider1, i, j);
				      else {
				        ichunkprovider.a(ichunkprovider1, c, i, j);
				      }

				    }

				    if ((flag3) && (flag2) && (flag6)) {
				      Chunk chunk = getOrCreateChunk((ChunkProviderServer) ichunkprovider, i - 1, j);
				      if (!chunk.isDone())
				        getChunkAt((ChunkProviderServer) ichunkprovider1, i - 1, j);
				      else {
				        ichunkprovider.a(ichunkprovider1, chunk, i - 1, j);
				      }
				    }

				    if ((flag) && (flag1) && (flag7)) {
				      Chunk chunk = getOrCreateChunk((ChunkProviderServer) ichunkprovider, i, j - 1);
				      if (!chunk.isDone())
				        getChunkAt((ChunkProviderServer) ichunkprovider1, i, j - 1);
				      else {
				        ichunkprovider.a(ichunkprovider1, chunk, i, j - 1);
				      }
				    }

				    if ((flag4) && (flag) && (flag3)) {
				      Chunk chunk = getOrCreateChunk((ChunkProviderServer) ichunkprovider, i - 1, j - 1);
				      if (!chunk.isDone())
				        getChunkAt((ChunkProviderServer) ichunkprovider1, i - 1, j - 1);
				      else {
				        ichunkprovider.a(ichunkprovider1, chunk, i - 1, j - 1);
				      }
				    }

				    c.world.timings.syncChunkLoadPostTimer.stopTiming();
				  }
				public boolean a(IChunkProvider ichunkprovider, Chunk chunk, int i, int j)
				  {
				    if ((ichunkprovider != null) && (ichunkprovider.a(ichunkprovider, chunk, i, j))) {
				      Chunk chunk1 = getOrCreateChunk((ChunkProviderServer) ichunkprovider, i, j);
				      
				      chunk1.e();
				      return true;
				    }
				    return false;
				  }

				private Chunk getOrCreateChunk(
						ChunkProviderServer ip,
						int i, int j) {
					
					  
					    Chunk chunk = (Chunk)ip.chunks.get(LongHash.toLong(i, j));

					    chunk = chunk == null ? getChunkAt(ip, i, j) : (!ip.world.ad()) && (!ip.forceChunkLoad) ? ip.emptyChunk : chunk;

					    if (chunk == ip.emptyChunk) return chunk;
					    if ((i != chunk.locX) || (j != chunk.locZ)) {
					      System.err.println("Chunk (" + chunk.locX + ", " + chunk.locZ + ") stored at  (" + i + ", " + j + ") in world '" + ip.world.getWorld().getName() + "'");
					      System.err.println(chunk.getClass().getName());
					      Throwable ex = new Throwable();
					      ex.fillInStackTrace();
					      ex.printStackTrace();
					    }

					    return chunk;
					  
				}

			}.start();
		}
	}
	
	private static MinecraftServer getServer() {
		return getCraftServer().getServer();
	}

	private static CraftServer getCraftServer() {
		return ((CraftServer) Bukkit.getServer());
	}

	private static ChunkGenerator getGenerator(String world) {
		try {
			Field settings = CraftServer.class.getDeclaredField("configuration");
			settings.setAccessible(true);
			ConfigurationSection section = ((YamlConfiguration) settings
					.get(getCraftServer())).getConfigurationSection("worlds");
			ChunkGenerator result = null;

			if (section != null) {
				section = section.getConfigurationSection(world);

				if (section != null) {
					String name = section.getString("generator");

					if ((name != null) && (!name.equals(""))) {
						String[] split = name.split(":", 2);
						String id = split.length > 1 ? split[1] : null;
						Plugin plugin = Bukkit.getPluginManager().getPlugin(
								split[0]);

						if (plugin == null)
							Bukkit.getLogger().severe(
									"Could not set generator for default world '"
											+ world + "': Plugin '" + split[0]
											+ "' does not exist");
						else if (!plugin.isEnabled())
							Bukkit.getLogger()
									.severe("Could not set generator for default world '"
											+ world
											+ "': Plugin '"
											+ plugin.getDescription()
													.getFullName()
											+ "' is not enabled yet (is it load:STARTUP?)");
						else {
							try {
								result = plugin.getDefaultWorldGenerator(world,
										id);
								if (result == null)
									Bukkit.getLogger()
											.severe("Could not set generator for default world '"
													+ world
													+ "': Plugin '"
													+ plugin.getDescription()
															.getFullName()
													+ "' lacks a default world generator");
							} catch (Throwable t) {
								plugin.getLogger().log(
										Level.SEVERE,
										"Could not set generator for default world '"
												+ world
												+ "': Plugin '"
												+ plugin.getDescription()
														.getFullName(), t);
							}
						}
					}
				}
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
