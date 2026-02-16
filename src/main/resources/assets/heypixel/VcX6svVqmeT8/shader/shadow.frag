#version 330 core

out vec4 color;

in vec2 v_TexCoord;
in vec2 v_OneTexel;

uniform sampler2D u_Texture;
uniform vec2 u_Direction;
uniform float u_Radius;      // 模糊半径
uniform float u_Intensity;   // Bloom 强度

void main() {
    vec4 blurColor = vec4(0.0);
    
    // 动态计算采样半径（限制在合理范围内）
    int radius = min(int(ceil(u_Radius)), 12);
    
    // 简化的高斯模糊 - 使用距离权重
    float totalWeight = 0.0;
    for (int i = -radius; i <= radius; i++) {
        // 计算权重：距离中心越远权重越小
        float offset = float(i);
        float weight = exp(-0.5 * (offset * offset) / (u_Radius * u_Radius));
        
        vec2 samplePos = v_TexCoord + v_OneTexel * offset * u_Direction;
        blurColor += texture(u_Texture, samplePos) * weight;
        totalWeight += weight;
    }
    
    // 归一化
    blurColor /= totalWeight;
    
    // 应用强度控制 - 使用加法混合而不是乘法，保持亮度
    // u_Intensity 控制 bloom 效果的可见度
    color = blurColor * u_Intensity;
}
