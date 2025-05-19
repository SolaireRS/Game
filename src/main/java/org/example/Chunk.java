package org.example;

public class Chunk {
    public final int chunkX, chunkY, chunkZ;
    public final int width, height, depth;

    private byte[] blocks;

    public Chunk(int chunkX, int chunkY, int chunkZ, int width, int height, int depth) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.blocks = new byte[width * height * depth];
    }

    private int getIndex(int x, int y, int z) {
        return x + (z * width) + (y * width * depth);
    }

    public byte getBlock(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return Block.AIR;
        }
        return blocks[getIndex(x, y, z)];
    }

    public void setBlock(int x, int y, int z, byte blockId) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return;
        }
        blocks[getIndex(x, y, z)] = blockId;
    }

    public void setBlocks(byte[][][] input) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    setBlock(x, y, z, input[x][y][z]);
                }
            }
        }
    }

    public byte[] getBlocks() {
        return blocks;
    }
}
