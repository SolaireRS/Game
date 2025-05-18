#version 330 core

out vec4 FragColor;

in vec2 TexCoord;
in vec3 Normal;
in vec3 FragPos;

uniform sampler2D textureSampler;

uniform vec3 lightDir;       // Directional light direction, normalized
uniform vec3 lightColor;     // Light color (white usually)
uniform vec3 ambientColor;   // Ambient light color

void main() {
    // Normalize interpolated normal
    vec3 norm = normalize(Normal);

    // Normalize light direction (make sure it's normalized in your Java code)
    vec3 lightDirection = normalize(-lightDir);

    // Diffuse intensity
    float diff = max(dot(norm, lightDirection), 0.0);

    // Calculate diffuse color
    vec3 diffuse = diff * lightColor;

    // Sample texture color
    vec3 texColor = texture(textureSampler, TexCoord).rgb;

    // Combine lighting with texture color
    vec3 result = (ambientColor + diffuse) * texColor;

    FragColor = vec4(result, 1.0);
}