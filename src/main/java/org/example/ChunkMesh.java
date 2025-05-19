package org.example;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class ChunkMesh {
    private static final float BLOCK_SIZE = 1.0f;
    private static final int ATLAS_SIZE = 16;       // Number of textures per row/column in atlas
    private static final float UV_SIZE = 1.0f / ATLAS_SIZE;

    private final Chunk chunk;

    private int vaoId;
    private int vboId;
    private int vertexCount;

    public ChunkMesh(Chunk chunk) {
        this.chunk = chunk;
        generateMesh();
    }

    public Chunk getChunk() {
        return chunk;
    }

    private void generateMesh() {
        List<Float> vertices = new ArrayList<>();

        for (int y = 0; y < chunk.height; y++) {
            for (int z = 0; z < chunk.depth; z++) {
                for (int x = 0; x < chunk.width; x++) {
                    byte block = chunk.getBlock(x, y, z);
                    if (block == Block.AIR) continue;

                    // Add faces only if neighbor block is air (or out of bounds)
                    if (isBlockAirSafe(x, y, z + 1)) addFace(vertices, x, y, z, Face.FRONT, block);
                    if (isBlockAirSafe(x, y, z - 1)) addFace(vertices, x, y, z, Face.BACK, block);
                    if (isBlockAirSafe(x - 1, y, z)) addFace(vertices, x, y, z, Face.LEFT, block);
                    if (isBlockAirSafe(x + 1, y, z)) addFace(vertices, x, y, z, Face.RIGHT, block);
                    if (isBlockAirSafe(x, y + 1, z)) addFace(vertices, x, y, z, Face.TOP, block);
                    if (isBlockAirSafe(x, y - 1, z)) addFace(vertices, x, y, z, Face.BOTTOM, block);
                }
            }
        }

        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            vertexArray[i] = vertices.get(i);
        }

        vertexCount = vertexArray.length / 8;  // 8 floats per vertex (pos(3), normal(3), uv(2))

        // Create VAO and VBO
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        // Upload vertex data to GPU
        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(vertexBuffer);

        int stride = 8 * Float.BYTES;

        // Position attribute (location 0): 3 floats, offset 0
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        // Normal attribute (location 2): 3 floats, offset 3 floats
        glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(2);

        // Texture coordinates attribute (location 1): 2 floats, offset 6 floats
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        System.out.printf("Generated mesh with %d vertices for chunk at (%d, %d, %d)%n",
                vertexCount, chunk.chunkX, chunk.chunkY, chunk.chunkZ);
    }

    private boolean isBlockAirSafe(int x, int y, int z) {
        if (x < 0 || x >= chunk.width || y < 0 || y >= chunk.height || z < 0 || z >= chunk.depth) {
            // Outside bounds considered air to show faces
            return true;
        }
        return chunk.getBlock(x, y, z) == Block.AIR;
    }

    private void addFace(List<Float> vertices, int x, int y, int z, Face face, byte blockId) {
        float px = x * BLOCK_SIZE;
        float py = y * BLOCK_SIZE;
        float pz = z * BLOCK_SIZE;

        int texX = blockId % ATLAS_SIZE;
        int texY = blockId / ATLAS_SIZE;

        float uMin = texX * UV_SIZE;
        float vMin = texY * UV_SIZE;
        float uMax = uMin + UV_SIZE;
        float vMax = vMin + UV_SIZE;

        switch (face) {
            case FRONT -> addQuad(vertices,
                    px - 0.5f, py - 0.5f, pz + 0.5f,
                    px + 0.5f, py - 0.5f, pz + 0.5f,
                    px + 0.5f, py + 0.5f, pz + 0.5f,
                    px - 0.5f, py + 0.5f, pz + 0.5f,
                    0, 0, 1, uMin, vMax, uMax, vMin);
            case BACK -> addQuad(vertices,
                    px + 0.5f, py - 0.5f, pz - 0.5f,
                    px - 0.5f, py - 0.5f, pz - 0.5f,
                    px - 0.5f, py + 0.5f, pz - 0.5f,
                    px + 0.5f, py + 0.5f, pz - 0.5f,
                    0, 0, -1, uMin, vMax, uMax, vMin);
            case LEFT -> addQuad(vertices,
                    px - 0.5f, py - 0.5f, pz - 0.5f,
                    px - 0.5f, py - 0.5f, pz + 0.5f,
                    px - 0.5f, py + 0.5f, pz + 0.5f,
                    px - 0.5f, py + 0.5f, pz - 0.5f,
                    -1, 0, 0, uMin, vMax, uMax, vMin);
            case RIGHT -> addQuad(vertices,
                    px + 0.5f, py - 0.5f, pz + 0.5f,
                    px + 0.5f, py - 0.5f, pz - 0.5f,
                    px + 0.5f, py + 0.5f, pz - 0.5f,
                    px + 0.5f, py + 0.5f, pz + 0.5f,
                    1, 0, 0, uMin, vMax, uMax, vMin);
            case TOP -> addQuad(vertices,
                    px - 0.5f, py + 0.5f, pz + 0.5f,
                    px + 0.5f, py + 0.5f, pz + 0.5f,
                    px + 0.5f, py + 0.5f, pz - 0.5f,
                    px - 0.5f, py + 0.5f, pz - 0.5f,
                    0, 1, 0, uMin, vMax, uMax, vMin);
            case BOTTOM -> addQuad(vertices,
                    px - 0.5f, py - 0.5f, pz - 0.5f,
                    px + 0.5f, py - 0.5f, pz - 0.5f,
                    px + 0.5f, py - 0.5f, pz + 0.5f,
                    px - 0.5f, py - 0.5f, pz + 0.5f,
                    0, -1, 0, uMin, vMax, uMax, vMin);
        }
    }

    private void addQuad(List<Float> vertices,
                         float x0, float y0, float z0,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float nx, float ny, float nz,
                         float uMin, float vMax, float uMax, float vMin) {
        // Triangle 1
        addVertex(vertices, x0, y0, z0, nx, ny, nz, uMin, vMax);
        addVertex(vertices, x1, y1, z1, nx, ny, nz, uMax, vMax);
        addVertex(vertices, x2, y2, z2, nx, ny, nz, uMax, vMin);

        // Triangle 2
        addVertex(vertices, x2, y2, z2, nx, ny, nz, uMax, vMin);
        addVertex(vertices, x3, y3, z3, nx, ny, nz, uMin, vMin);
        addVertex(vertices, x0, y0, z0, nx, ny, nz, uMin, vMax);
    }

    private void addVertex(List<Float> vertices,
                           float x, float y, float z,
                           float nx, float ny, float nz,
                           float u, float v) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(nx);
        vertices.add(ny);
        vertices.add(nz);
        vertices.add(u);
        vertices.add(v);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    private enum Face {
        FRONT, BACK, LEFT, RIGHT, TOP, BOTTOM
    }
}
