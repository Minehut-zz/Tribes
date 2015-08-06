package com.minehut.tribes.schematic;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import com.minehut.core.util.common.chat.F;
import com.minehut.tribes.Tribes;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.ShortTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

public class SchematicUtils {
	private static File baseSchematicsFile;
	
	private static List<Schematic> allSchematics = new ArrayList<Schematic>();
	
	public static void initFile(File baseFile){
		baseSchematicsFile = baseFile;
	}
	
	public static void initSchematics(){
		allSchematics.clear();
		
		for(File schematicFile : baseSchematicsFile.listFiles()){
			if(!(schematicFile.getName().startsWith("."))){
				Schematic schematic = loadSchematic(schematicFile);
				
				if(schematic != null){
					allSchematics.add(schematic);
				}
			}
		}
	}
	
	public static List<Schematic> getAllSchematics(){
		return allSchematics;
	}
	
	public static Schematic loadSchematic(File file){
		try{
			if(file.exists()){
				NBTInputStream nbtStream =  new NBTInputStream(new FileInputStream(file));
				CompoundTag compound = (CompoundTag) nbtStream.readTag();
				Map<String, Tag> tags = compound.getValue();
				Short width = ((ShortTag) tags.get("Width")).getValue();
				Short height = ((ShortTag) tags.get("Height")).getValue();
				Short length = ((ShortTag) tags.get("Length")).getValue();
				
				String materials = ((StringTag) tags.get("Materials")).getValue();
				
				byte[] blocks = ((ByteArrayTag) tags.get("Blocks")).getValue();
				byte[] data = ((ByteArrayTag) tags.get("Data")).getValue();
				
				nbtStream.close();
				
				Schematic schematic = new Schematic(file.getName().replace(".schematic", ""), width, height, length, materials, blocks, data);
				
				return schematic;
			}
		} catch(Exception e){
			F.debug("Failed to load the schematic: " + file.getAbsolutePath());
			e.printStackTrace();
		}
		
		return null;
	}

	public static void paste(Schematic schematic, Location loc){
		HashMap<Block, Integer> blocks = new HashMap<Block, Integer>();
		List<Block> allBlocks = new ArrayList<Block>();

		for(int x = 0; x < schematic.getWidth(); x++){
				for (int z = 0; z < schematic.getLength(); ++z){
					for (int y = 0; y < schematic.getHeight(); y++){
						Location temp = loc.clone().add(x, y, z);
						Block block = temp.getBlock();
						int index = y * schematic.getWidth() * schematic.getLength() + z * schematic.getWidth() + x;

						blocks.put(block, index);
						allBlocks.add(block);
				}
			}
		}

		List<Block> orderedBlocks = new ArrayList<Block>();

		orderedBlocks.addAll(allBlocks);

		Collections.sort(orderedBlocks, new Comparator<Block>() {
			@Override
			public int compare(Block block1, Block block2) {
				return Double.compare(block1.getY(), block2.getY());
			}
		});

		int size = orderedBlocks.size();
		
		long delay = 1L;

		if(size > 0){
			new PasteRunnable(schematic, orderedBlocks, blocks, loc).runTaskTimer(Tribes.instance, 40L, delay);
		}
	}

	public static class PasteRunnable extends BukkitRunnable { //Simple work around to avoid using final

		int size = 0;
		int index = 0;
		int blocksPerTime = 3;
		
		Schematic schematic;
		List<Block> orderedBlocks;
		Location loc;
		HashMap<Block, Integer> blocks;
		
		public PasteRunnable(Schematic schematic, List<Block> orderedBlocks, HashMap<Block, Integer> blocks, Location loc) {
			this.schematic = schematic;
			this.orderedBlocks = orderedBlocks;
			this.size = orderedBlocks.size();
			this.blocks = blocks;
			this.loc = loc;
		}

		
		@Override
		public void run(){
			for(int i = 0; i < blocksPerTime; i++){
				if(index < size){
					Block block = orderedBlocks.get(index);
					int otherIndex = blocks.get(block);
					int typeId = schematic.getBlocks()[otherIndex];
					byte data = schematic.getData()[otherIndex];
					Material material = Material.getMaterial(typeId);

					while (material == Material.AIR && block.getType() == Material.AIR) {
						index += 1;

						if (index >= size) {
							this.cancel();
							return;
						}

						block = orderedBlocks.get(index);
						otherIndex = blocks.get(block);
						typeId = schematic.getBlocks()[otherIndex];
						data = schematic.getData()[otherIndex];
						material = Material.getMaterial(typeId);
					}

					if(!(block.getLocation().equals(loc))){
						regenerateBlock(block, Material.getMaterial(typeId), data);
					}

					index += 1;
				} else {
					this.cancel();
					return;
				}
			}
		}
		
	}
	
	
	public static void regenerateBlocks(Collection<Block> blocks, final Material type, final byte data, final int blocksPerTime, final long delay, Comparator<Block> comparator) {
		final List<Block> orderedBlocks = new ArrayList<Block>();

		orderedBlocks.addAll(blocks);

		if(comparator != null){
			Collections.sort(orderedBlocks, comparator);
		}

		final int size = orderedBlocks.size();

		if(size > 0){
			new BukkitRunnable(){
				int index = size - 1;

				@Override
				public void run(){
					for(int i = 0; i < blocksPerTime; i++){
						if(index >= 0){
							Block block = orderedBlocks.get(index);

							regenerateBlock(block, type, data);

							index -= 1;
						} else {
							this.cancel();
							return;
						}
					}
				}
			}.runTaskTimer(Tribes.instance, 0L, delay);
		}
	}

	public static void regenerateBlock(Block block, final Material type, final byte data){
		final Location loc = block.getLocation();

		if(type != null && block != null) {
			block.setType(type);

			if (type != Material.AIR) {
				block.setData(data);
				loc.getWorld().playEffect(loc, Effect.STEP_SOUND, type);
			}
		}


//		loc.getWorld().playEffect(loc, Effect.STEP_SOUND, (type == Material.AIR ? block.getType().getId() : type.getId()));
	}
}