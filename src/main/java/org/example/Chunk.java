package org.example;

public class Chunk {
    public final int chunkX, chunkY, chunkZ;
    public final int width, height, depth;

    private byte[][][] blocks;

    public Chunk(int chunkX, int chunkY, int chunkZ, int width, int height, int depth) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.blocks = new byte[width][height][depth];
    }

    public void setBlocks(byte[][][] blocks) {
        this.blocks = blocks;
    }

    public byte[][][] getBlocks() {
        return blocks;
    }

    // Optionally, get block at local coords within chunk
    public byte getBlock(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) {
            return Block.AIR; // or some default block
        }
        return blocks[x][y][z];
    }
}
