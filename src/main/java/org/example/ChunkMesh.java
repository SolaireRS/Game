package org.example;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryUtil;

public class ChunkMesh {
    private Chunk chunk;

    private int vaoId;
    private int vboId;
    private int eboId;
    private int vertexCount;

    public ChunkMesh(Chunk chunk) {
        this.chunk = chunk;
        buildMesh();
    }

    public Chunk getChunk() {
        return chunk;
    }

    private void buildMesh() {
        // Generate mesh data (vertices, indices) from chunk blocks here
        // For simplicity, let's say we build a basic cube mesh per block face visible

        // TODO: Replace with your mesh generation logic, e.g. greedy meshing or naive meshing

        // For example purposes, here's how you might set up a minimal VBO/VAO:

        float[] vertices = {
            // positions        // texture coords
            -0.5f, -0.5f, -0.5f, 0f, 0f,
             0.5f, -0.5f, -0.5f, 1f, 0f,
             0.5f,  0.5f, -0.5f, 1f, 1f,
            -0.5f,  0.5f, -0.5f, 0f, 1f,
            // ... (rest of cube vertices)
        };

        int[] indices = {
            0, 1, 2, 2, 3, 0,
            // ... (rest of cube indices)
        };

        vertexCount = indices.length;

        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Vertex buffer
        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        // Position attribute (location = 0)
        GL20.glVertexAttribPointer(0, 3, GL15.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // Texture coord attribute (location = 1)
        GL20.glVertexAttribPointer(1, 2, GL15.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        MemoryUtil.memFree(vertexBuffer);

        // Element buffer
        eboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
        MemoryUtil.memFree(indicesBuffer);

        GL30.glBindVertexArray(0);
    }

    public void render() {
        GL30.glBindVertexArray(vaoId);
        GL15.glDrawElements(GL15.GL_TRIANGLES, vertexCount, GL15.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    public void cleanup() {
        GL15.glDeleteBuffers(vboId);
        GL15.glDeleteBuffers(eboId);
        GL30.glDeleteVertexArrays(vaoId);
    }
}
