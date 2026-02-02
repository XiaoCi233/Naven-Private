#version 330 core

out vec4 color;

in vec2 v_TexCoord;
in vec2 v_OneTexel;

uniform sampler2D u_Texture;
uniform vec2 u_Direction;
uniform float u_Radius;
uniform float u_Intensity;

void main() {
    vec4 blurColor = vec4(0.0);

    int radius = min(int(ceil(u_Radius)), 12);

    float totalWeight = 0.0;
    for (int i = -radius; i <= radius; i++) {
        float offset = float(i);
        float weight = exp(-0.5 * (offset * offset) / (u_Radius * u_Radius));
        
        vec2 samplePos = v_TexCoord + v_OneTexel * offset * u_Direction;
        blurColor += texture(u_Texture, samplePos) * weight;
        totalWeight += weight;
    }

    blurColor /= totalWeight;

    color = blurColor * u_Intensity;
}
