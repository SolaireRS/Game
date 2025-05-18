package org.example;

import org.joml.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;  // for general OpenGL functions and constants
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import org.joml.Math;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private long window;
    private int width = 1280;
    private int height = 720;

    private ShaderProgram shaderProgram;
    private Texture grassTexture;
    private World world;
    private Map<Long, ChunkMesh> chunkMeshes = new HashMap<>();

    private Camera camera;
    private float lastX = width / 2.0f;
    private float lastY = height / 2.0f;
    private boolean firstMouse = true;

    private double lastTime;
    private float deltaTime;

    // Movement keys state
    private boolean forward, backward, left, right;

    public void run() throws IOException {
        init();
        loop();

        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() throws IOException {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Voxel World", 0, 0);
        if (window == 0)
            throw new RuntimeException("Failed to create GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        glEnable(GL11.GL_DEPTH_TEST);
        glClearColor(0.5f, 0.7f, 1.0f, 1.0f);

        // Setup viewport
        glViewport(0, 0, width, height);

        // Setup callbacks
        setupCallbacks();

        shaderProgram = new ShaderProgram("src/main/resources/shaders/vertex.glsl", "src/main/resources/shaders/fragment.glsl");
        grassTexture = new Texture("src/main/resources/textures/grass.png");

        world = new World(16, 128, 16);
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                Chunk chunk = world.getChunk(cx, 0, cz);
                ChunkMesh mesh = new ChunkMesh(chunk);
                chunkMeshes.put(getChunkKey(cx, 0, cz), mesh);
            }
        }

        camera = new Camera(new Vector3f(8, 20, 20));

        lastTime = glfwGetTime();

        // Capture the mouse
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    private void setupCallbacks() {
        // Window resize callback
        glfwSetFramebufferSizeCallback(window, (window, w, h) -> {
            width = w;
            height = h;
            glViewport(0, 0, width, height);
        });

        // Mouse position callback for mouse look
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (firstMouse) {
                lastX = (float) xpos;
                lastY = (float) ypos;
                firstMouse = false;
            }

            float xoffset = (float) xpos - lastX;
            float yoffset = lastY - (float) ypos; // reversed: y ranges bottom to top

            lastX = (float) xpos;
            lastY = (float) ypos;

            float sensitivity = 0.1f;
            xoffset *= sensitivity;
            yoffset *= sensitivity;

            camera.yaw += xoffset;
            camera.pitch += yoffset;

            // Constrain pitch
            if (camera.pitch > 89.0f)
                camera.pitch = 89.0f;
            if (camera.pitch < -89.0f)
                camera.pitch = -89.0f;
        });

        // Keyboard input callback
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            boolean pressed = action != GLFW_RELEASE;
            switch (key) {
                case GLFW_KEY_W -> forward = pressed;
                case GLFW_KEY_S -> backward = pressed;
                case GLFW_KEY_A -> left = pressed;
                case GLFW_KEY_D -> right = pressed;
                case GLFW_KEY_ESCAPE -> {
                    if (pressed)
                        glfwSetWindowShouldClose(window, true);
                }
            }
        });
    }

    private void processInput() {
        Vector3f front = camera.getFront();
        Vector3f rightVec = camera.getRight();

        float cameraSpeed = 10.0f * deltaTime; // adjust speed as needed

        if (forward)
            camera.position.fma(cameraSpeed, front);
        if (backward)
            camera.position.fma(-cameraSpeed, front);
        if (left)
            camera.position.fma(-cameraSpeed, rightVec);
        if (right)
            camera.position.fma(cameraSpeed, rightVec);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            processInput();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(70.0f), (float) width / height, 0.1f, 1000.0f);
            Matrix4f view = camera.getViewMatrix();

            shaderProgram.use();
            shaderProgram.setUniform1i("textureSampler", 0);

            for (ChunkMesh mesh : chunkMeshes.values()) {
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
