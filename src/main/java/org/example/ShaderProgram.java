package org.example;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ShaderProgram {
    private final int programId;
    private final Map<String, Integer> uniformLocations = new HashMap<>();

    public ShaderProgram(String vertexPath, String fragmentPath) throws IOException {
        int vertexShader = loadShader(vertexPath, GL_VERTEX_SHADER);
        int fragmentShader = loadShader(fragmentPath, GL_FRAGMENT_SHADER);

        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program linking failed: " + glGetProgramInfoLog(programId));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int loadShader(String filePath, int type) throws IOException {
        String source = new String(Files.readAllBytes(Paths.get(filePath)));
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compilation failed: " + glGetShaderInfoLog(shader));
        }

        return shader;
    }

    public void use() {
        glUseProgram(programId);
    }

    private int getUniformLocation(String name) {
        if (uniformLocations.containsKey(name)) {
            return uniformLocations.get(name);
        }
        int location = glGetUniformLocation(programId, name);
        if (location < 0) {
            System.err.println("Warning: uniform '" + name + "' not found!");
        }
        uniformLocations.put(name, location);
        return location;
    }

    public void setUniform1i(String name, int value) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            glUniform1i(location, value);
        }
    }
	public void setUniformVec3(String name, Vector3f vec) {
    int loc = getUniformLocation(name);
    if (loc < 0) {
        System.err.println("Warning: uniform '" + name + "' not found!");
        return;
    }
    glUniform3f(loc, vec.x, vec.y, vec.z);
	}

    public void setUniformMat4(String name, Matrix4f matrix) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            float[] matArray = new float[16];
            matrix.get(matArray);
            glUniformMatrix4fv(location, false, matArray);
        }
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }
}
