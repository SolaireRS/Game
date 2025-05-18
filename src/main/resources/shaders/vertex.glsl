#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec3 aNormal;  // NEW: normal vector input

out vec2 TexCoord;
out vec3 Normal;
out vec3 FragPos;  // Position in world space

uniform mat4 uMVP;
uniform mat4 uModel;  // NEW: model matrix (for transforming position & normal)

void main() {
    FragPos = vec3(uModel * vec4(aPos, 1.0));
    Normal = mat3(transpose(inverse(uModel))) * aNormal;
    TexCoord = aTexCoord;
    gl_Position = uMVP * vec4(aPos, 1.0);
}
