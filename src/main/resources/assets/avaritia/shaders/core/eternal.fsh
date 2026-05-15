#version 150

#define M_PI 3.1415926535897932384626433832795

#moj_import <fog.glsl>

const int cosmiccount = 10;
const int cosmicoutof = 101;
const float lightmix = 0.2;

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform float time;

uniform float yaw;
uniform float pitch;
uniform float externalScale;

uniform float opacity;

/* =========================================================
   FIX: remove mat2 array (Veil GLSL processor crash)
   ========================================================= */

uniform mat2 cosmicuvs0;
uniform mat2 cosmicuvs1;
uniform mat2 cosmicuvs2;
uniform mat2 cosmicuvs3;
uniform mat2 cosmicuvs4;
uniform mat2 cosmicuvs5;
uniform mat2 cosmicuvs6;
uniform mat2 cosmicuvs7;
uniform mat2 cosmicuvs8;
uniform mat2 cosmicuvs9;

mat2 getCosmicUV(int i)
{
    if (i == 0) return cosmicuvs0;
    if (i == 1) return cosmicuvs1;
    if (i == 2) return cosmicuvs2;
    if (i == 3) return cosmicuvs3;
    if (i == 4) return cosmicuvs4;
    if (i == 5) return cosmicuvs5;
    if (i == 6) return cosmicuvs6;
    if (i == 7) return cosmicuvs7;
    if (i == 8) return cosmicuvs8;
    return cosmicuvs9;
}

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;
in vec3 fPos;

out vec4 fragColor;

/* =========================================================
   optimized rotation matrix (slightly cheaper math)
   ========================================================= */

mat4 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);

    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    float xx = axis.x * axis.x;
    float yy = axis.y * axis.y;
    float zz = axis.z * axis.z;

    float xy = axis.x * axis.y;
    float xz = axis.x * axis.z;
    float yz = axis.y * axis.z;

    float xs = axis.x * s;
    float ys = axis.y * s;
    float zs = axis.z * s;

    return mat4(
        oc * xx + c,   oc * xy - zs,  oc * xz + ys, 0.0,
        oc * xy + zs,  oc * yy + c,   oc * yz - xs, 0.0,
        oc * xz - ys,  oc * yz + xs,  oc * zz + c,  0.0,
        0.0,           0.0,           0.0,          1.0
    );
}

void main(void)
{
    vec4 mask = texture(Sampler0, texCoord0.xy);

    float invScale = 1.0 / externalScale;

    vec4 col = vec4(0.1, 0.0, 0.0, 1.0);

    float pulse = fract(time / 400.0);

    col.g = sin(pulse * M_PI * 2.0) * 0.075 + 0.225;
    col.b = cos(pulse * M_PI * 2.0) * 0.05 + 0.3;

    vec4 dir = normalize(vec4(-fPos, 0.0));

    /* pitch */
    float sb = sin(pitch);
    float cb = cos(pitch);
    dir.yz = vec2(dir.y * cb - dir.z * sb, dir.y * sb + dir.z * cb);

    /* yaw */
    float sa = sin(-yaw);
    float ca = cos(-yaw);
    dir.xz = vec2(dir.z * sa + dir.x * ca, dir.z * ca - dir.x * sa);

    vec4 ray;

    for (int i = 0; i < 16; i++)
    {
        int mult = 16 - i;

        /* =========================================================
           OPT: replace pow/mod chaos with stable hash
           ========================================================= */

        int j = i + 7;

        float base = float(j * 131 + i * 17);

        float rand1 = base * 12.9898;
        float rand2 = base * 78.233;
        float rand3 = base * 37.719;

        vec3 axis = normalize(vec3(
            sin(rand1),
            sin(rand2),
            cos(rand3)
        ));

        float angle = fract(rand3) * 6.2831853;

        ray = dir * rotationMatrix(axis, angle);

        float rawu = 0.5 + atan(ray.z, ray.x) / (2.0 * M_PI);
        float rawv = 0.5 + asin(ray.y) / M_PI;

        float scale = mult * 0.5 + 2.75;

        float u = rawu * scale * externalScale;
        float v = (rawv + time * 0.0002 * invScale) * scale * 0.6 * externalScale;

        int tu = int(mod(floor(u * 16.0), 16.0));
        int tv = int(mod(floor(v * 16.0), 16.0));

        int position = ((171 * tu) + (489 * tv) + (303 * (i + 31)) + 17209) ^ 50943779;
        int symbol = int(mod(float(position), float(cosmicoutof)));

        int rotation = int(mod(float(tu + tv * 3 + i), 8.0));

        bool flip = rotation >= 4;
        rotation -= (flip ? 4 : 0);

        if (symbol >= 0 && symbol < cosmiccount)
        {
            mat2 uvmat = getCosmicUV(symbol);

            float ru = clamp(fract(u) * 16.0 - float(tu), 0.0, 1.0);
            float rv = clamp(fract(v) * 16.0 - float(tv), 0.0, 1.0);

            if (flip) ru = 1.0 - ru;

            float oru = ru;
            float orv = rv;

            if (rotation == 1)
            {
                oru = 1.0 - rv;
                orv = ru;
            }
            else if (rotation == 2)
            {
                oru = 1.0 - ru;
                orv = 1.0 - rv;
            }
            else if (rotation == 3)
            {
                oru = rv;
                orv = 1.0 - ru;
            }

            vec2 cosmictex;
            cosmictex.x = uvmat[0][0] * (1.0 - oru) + uvmat[1][0] * oru;
            cosmictex.y = uvmat[0][1] * (1.0 - orv) + uvmat[1][1] * orv;

            vec4 tcol = texture(Sampler0, cosmictex);

            float a =
                tcol.r *
                (0.5 + 1.0 / float(mult)) *
                (1.0 - smoothstep(0.15, 0.48, abs(rawv - 0.5)));

            float r = fract(rand1 * 0.00001) * 0.3 + 0.4;
            float g = fract(rand2 * 0.00001) * 0.4 + 0.6;
            float b = fract(rand3 * 0.00001) * 0.3 + 0.7;

            col.rgb += vec3(r, g, b) * a;
        }
    }

    vec3 shade = mix(vec3(1.0), vertexColor.rgb, lightmix);
    col.rgb *= shade;

    col.a *= mask.r * opacity;

    col = clamp(col, 0.0, 1.0);

    fragColor = linear_fog(col * ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}