package org.example;

import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    public final int programId;

    public Shader(String vertexPath, String fragmentPath) throws IOException {
        String vertexSrc = Files.readString(Paths.get(vertexPath));
        String fragmentSrc = Files.readString(Paths.get(fragmentPath));

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSrc);
        glCompileShader(vertexShader);
        checkCompileErrors(vertexShader, "VERTEX");

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSrc);
        glCompileShader(fragmentShader);
        checkCompileErrors(fragmentShader, "FRAGMENT");

        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);
        checkCompileErrors(programId, "PROGRAM");

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void use() {
        glUseProgram(programId);
    }

    public void setMatrix4(String name, org.joml.Matrix4f matrix) {
        try (var stack = org.lwjgl.system.MemoryStack.stackPush()) {
            glUniformMatrix4fv(glGetUniformLocation(programId, name), false, matrix.get(stack.mallocFloat(16)));
        }
    }

    private void checkCompileErrors(int shader, String type) {
        if (type.equals("PROGRAM")) {
            if (glGetProgrami(shader, GL_LINK_STATUS) == GL_FALSE) {
                throw new RuntimeException("Program link error:\n" + glGetProgramInfoLog(shader));
            }
        } else {
            if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
                throw new RuntimeException(type + " compile error:\n" + glGetShaderInfoLog(shader));
            }
        }
    }
}
