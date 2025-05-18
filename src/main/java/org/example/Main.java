package org.example;

import org.joml.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;

public class Main {
    private long window;
    private int width = 1280;
    private int height = 720;

    private ShaderProgram shaderProgram;
    private Texture grassTexture;
    private World world;
    private Map<Long, ChunkMesh> chunkMeshes = new HashMap<>();

    private Vector3f cameraPos = new Vector3f(8, 20, 20);
    private Vector3f cameraFront = new Vector3f(0, -0.5f, -1).normalize();
    private Vector3f cameraUp = new Vector3f(0, 1, 0);

    private GLFWWindowSizeCallback windowSizeCallback;

    public void run() throws IOException {
        init();
        loop();

        if (windowSizeCallback != null) {
            windowSizeCallback.free();
        }

        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() throws IOException {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Voxel World", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        // Setup resize callback to update viewport and window size
        windowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int newWidth, int newHeight) {
                width = newWidth;
                height = newHeight;
                glViewport(0, 0, width, height);
            }
        };
        glfwSetWindowSizeCallback(window, windowSizeCallback);

        GL.createCapabilities();
        glClearColor(0.5f, 0.7f, 1.0f, 1.0f); // light sky blue background

        shaderProgram = new ShaderProgram("src/main/resources/shaders/vertex.glsl", "src/main/resources/shaders/fragment.glsl");
        grassTexture = new Texture("src/main/resources/textures/grass.png");

        world = new World(16, 8, 16); // Adjusted height to 8 chunks for example

        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                Chunk chunk = world.getChunk(cx + 8, 0, cz + 8); // offset to center if needed
                if (chunk != null) {
                    ChunkMesh mesh = new ChunkMesh(chunk);
                    chunkMeshes.put(getChunkKey(chunk.chunkX, chunk.chunkY, chunk.chunkZ), mesh);
                }
            }
        }

        // Set spawn point on terrain at chunk (0,0)
        int spawnChunkX = 0;
        int spawnChunkZ = 0;
        int spawnHeight = world.generateHeight(spawnChunkX, spawnChunkZ);
        cameraPos.set(8, spawnHeight + 2, 8); // spawn slightly above ground
        cameraFront.set(0, -0.5f, -1).normalize();
        cameraUp.set(0, 1, 0);

        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, width, height);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Matrix4f projection = new Matrix4f().perspective(org.joml.Math.toRadians(70.0f), (float) width / height, 0.1f, 1000.0f);
            Matrix4f view = new Matrix4f().lookAt(cameraPos, new Vector3f(cameraPos).add(cameraFront), cameraUp);

            shaderProgram.use();
            shaderProgram.setUniform1i("textureSampler", 0);

            for (Map.Entry<Long, ChunkMesh> entry : chunkMeshes.entrySet()) {
                ChunkMesh mesh = entry.getValue();
                Chunk chunk = mesh.getChunk();

                Matrix4f model = new Matrix4f().translate(chunk.chunkX * chunk.width, chunk.chunkY * chunk.height, chunk.chunkZ * chunk.depth);
                Matrix4f mvp = new Matrix4f(projection).mul(view).mul(model);

                shaderProgram.setUniformMat4("uMVP", mvp);
                shaderProgram.setUniformMat4("uModel", model);

                shaderProgram.setUniformVec3("lightDir", new Vector3f(-0.2f, -1.0f, -0.3f).normalize());
                shaderProgram.setUniformVec3("lightColor", new Vector3f(1.0f, 1.0f, 1.0f));
                shaderProgram.setUniformVec3("ambientColor", new Vector3f(0.3f, 0.3f, 0.3f));

                glActiveTexture(GL_TEXTURE0);
                grassTexture.bind();

                mesh.render();
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private long getChunkKey(int x, int y, int z) {
        return (((long) x & 0xFFFFFL) << 40) | (((long) y & 0xFFFFFL) << 20) | ((long) z & 0xFFFFFL);
    }

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
