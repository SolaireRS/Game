package org.example;

public class World {
    private int width, height, depth;
    private Chunk[][][] chunks;

    public World(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        chunks = new Chunk[width][height][depth];
        generateChunks();
    }

    private void generateChunks() {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                // Get the terrain height at this (x,z) chunk column
                int heightAtXZ = generateHeight(x, z);

                for (int y = 0; y < height; y++) {
                    Chunk chunk = new Chunk(x, y, z, 16, 16, 16);
                    byte[][][] blocks = new byte[16][16][16];

                    for (int bx = 0; bx < 16; bx++) {
                        for (int bz = 0; bz < 16; bz++) {
                            int worldX = x * 16 + bx;
                            int worldZ = z * 16 + bz;

                            for (int by = 0; by < 16; by++) {
                                int worldY = y * 16 + by;

                                if (worldY <= heightAtXZ) {
                                    if (worldY == heightAtXZ) {
                                        blocks[bx][by][bz] = Block.GRASS;
                                    } else if (worldY > heightAtXZ - 5) {
                                        blocks[bx][by][bz] = Block.DIRT;
                                    } else {
                                        blocks[bx][by][bz] = Block.STONE;
                                    }
                                } else {
                                    blocks[bx][by][bz] = Block.AIR;
                                }
                            }
                        }
                    }

                    chunk.setBlocks(blocks);
                    chunks[x][y][z] = chunk;
                }
            }
        }
    }

    /**
     * Generate terrain height at chunk column (chunkX, chunkZ).
     * @param chunkX chunk x-coordinate
     * @param chunkZ chunk z-coordinate
     * @return world height at this chunk column
     */
    public int generateHeight(int chunkX, int chunkZ) {
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;
        double noiseVal = 0;

        // Sample noise over 16x16 blocks to smooth height
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                noiseVal += Noise.noise((worldX + i) / 50.0, (worldZ + j) / 50.0);
            }
        }
        noiseVal /= (16 * 16);

        int maxHeight = height * 16 - 1;
        int minHeight = 20;

        // Map noise from [-1,1] to [minHeight, maxHeight]
        return (int) (((noiseVal + 1) / 2) * (maxHeight - minHeight)) + minHeight;
    }

    public Chunk getChunk(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) return null;
        return chunks[x][y][z];
    }
}
