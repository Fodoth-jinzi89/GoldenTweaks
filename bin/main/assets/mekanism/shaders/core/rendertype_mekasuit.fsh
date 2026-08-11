#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 shadedVertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

// 直接提供完整定义，不使用 forward declaration
bool shouldTint(float red, float green, float blue) {
    // 避免使用 min/max 作为变量名
    float minValue = min(min(red, green), blue);
    float maxValue = max(max(red, green), blue);

    float delta = maxValue - minValue;

    // Calculate Saturation and Value components of HSV
    float saturation = maxValue == 0.0 ? 0.0 : delta / maxValue;
    float value = maxValue;

    return value >= 0.48 && saturation <= 0.15;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);

    if (color.a < 0.5) {
        discard;
    }

    if (shouldTint(color.r, color.g, color.b)) {
        color *= shadedVertexColor * ColorModulator;
    } else {
        color *= vertexColor * ColorModulator;
    }

    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);

    color *= lightMapColor;

    fragColor = linear_fog(
        color,
        vertexDistance,
        FogStart,
        FogEnd,
        FogColor
    );
}