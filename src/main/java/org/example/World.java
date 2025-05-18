package org.example;

import java.util.HashMap;
import java.util.Map;

public class World {
    // Use a simple 2D map keyed by chunkX and chunkZ for surface world (one vertical chunk layer)
    private Map<Long, Chunk> chunks = new HashMap<>();

    private final int chunkWidth;
    private final int chunkHeight;
    private final int chunkDepth;

    public World(int chunkWidth, int chunkHeight, int chunkDepth) {
        this.chunkWidth = chunkWidth;
        this.chunkHeight = chunkHeight;
        this.chunkDepth = chunkDepth;
    }

    // Generates or loads chunk at given chunk coordinates
    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        long key = getChunkKey(chunkX, chunkY, chunkZ);
        if (!chunks.containsKey(key)) {
            Chunk newChunk = new Chunk(chunkX, chunkY, chunkZ, chunkWidth, chunkHeight, chunkDepth);
            newChunk.generateTerrain(); // You need to rename generateExampleTerrain to generateTerrain or similar
            chunks.put(key, newChunk);
        }
        return chunks.get(key);
    }

    private long getChunkKey(int x, int y, int z) {
        // Combines 3 ints into a long key (simple hash)
        return (((long) x & 0xFFFFFL) << 40) | (((long) y & 0xFFFFFL) << 20) | ((long) z & 0xFFFFFL);
    }
}
