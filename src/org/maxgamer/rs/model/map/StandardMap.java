package org.maxgamer.rs.model.map;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.maxgamer.rs.cache.EncryptedException;
import org.maxgamer.rs.cache.MapCache;
import org.maxgamer.rs.cache.format.Landscape;
import org.maxgamer.rs.events.world.ChunkLoadEvent;

/**
 * Represents the primary world for the server, compared to secondary worlds
 * such as Dungeons and Player Houses.
 * @author netherfoam
 */
public class StandardMap extends WorldMap {
	public StandardMap(String name, int sizeX, int sizeY) throws IOException {
		super(name, sizeX, sizeY);
	}
	
	public static final int FLAG_CLIP = 0x1;
	public static final int FLAG_BRIDGE = 0x2;
	
	/**
	 * Flag given if there is a roof over this piece of terrain
	 */
	public static final int FLAG_ROOF = 0x4;
	
	/**
	 * Steep cliff flag? Elevation flag?
	 */
	public static final int FLAG_UNKNOWN = 0x8;
	
	/**
	 * Wall flag?
	 */
	public static final int FLAG_UNKNOWN2 = 0x10;
	
	@Override
	protected void fetch(int x, int y, int z) throws EncryptedException {
		//Region x,y coordinates. These are tile# >> 6
		int rx = x >> 3;
		int ry = y >> 3;
		
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				for (int k = 0; k < 4; k++) {
					//Chunk x,y coordinates. Thes are tile# >> 3
					int cx = (rx << 3) + i;
					int cy = (ry << 3) + j;
					Chunk c = getChunk(cx, cy, k);
					c.setLoaded(true);
				}
			}
		}
		
		ByteBuffer map;
		ByteBuffer objects;
		
		try {
			map = MapCache.getMap(rx, ry);
			objects = MapCache.getObjects(rx, ry);
			
			//Tile x,y coordinates. Each coordinate is unique for tiles.
			int tx = rx << (3 + WorldMap.CHUNK_BITS);
			int ty = ry << (3 + WorldMap.CHUNK_BITS);
			
			Landscape l = Landscape.parse(map, objects);
			l.apply(this, tx, ty, 0, 0, 64, 64, 0, 0, 3);
			
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					for (int k = 0; k < 4; k++) {
						int cx = (rx << 3) + i;
						int cy = (ry << 3) + j;
						Chunk c = getChunk(cx, cy, k);
						ChunkLoadEvent e = new ChunkLoadEvent(this, c, cx, cy, k);
						e.call();
					}
				}
			}
		}
		catch (FileNotFoundException e) {
			//That map literally doesn't exist.
			return;
		}
		catch (IOException e) {
			throw new EncryptedException("Map " + rx + "_" + ry + " failed: file is encrypted", e);
		}
	}
	
	@Override
	protected Chunk constructChunk(int chunkX, int chunkY, int z) {
		return new Chunk(chunkX, chunkY, z);
	}
}