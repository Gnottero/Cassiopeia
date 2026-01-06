#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    vec4 lightColor = texture(Sampler2, texCoord2);
    
    // Apply color modulator and light
    color *= ColorModulator;
    color.rgb *= lightColor.rgb;
    
    // Apply semi-transparency (50% alpha)
    color.a *= 0.5;
    
    // Discard fully transparent pixels
    if (color.a < 0.01) {
        discard;
    }
    
    fragColor = color;
}
