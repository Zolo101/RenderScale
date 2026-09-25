#version 420

// Sparse Grid resolve filter for ordered-grid supersampling.
//
// Eight bilinear taps: two half-scale rotated-grid quads placed in
// opposite quadrants with opposite orientations. Every tap reads the
// already-rendered high-resolution image at a different horizontal AND
// vertical offset within the output pixel's footprint, so angled edges
// resolve to more intermediate shades than a plain 4x-order box average
// across the tested edge angles, at the cost of slight softening of fine
// textures. (The opposite-quadrant layout is not perfectly isotropic on
// reflected diagonals.) Meant for render scales of 3x and above, where the
// pattern has room to spread; below that the single rotated grid filter is
// a better fit. These taps do not add geometry coverage samples.

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

out vec4 fragColor;

void main()
{
// ScreenSize must match this pass's destination texture size.
// One output pixel expressed in normalized input UV coordinates.
vec2 outputToInput = 1.0 / ScreenSize;

vec2 uv = gl_FragCoord.xy / ScreenSize;

// Two half-scale rotated-grid quads centred at (+0.25, -0.25) and
// (-0.25, +0.25), the second rotated 90 degrees. All coordinates are
// exact multiples of 1/16.
vec3 sum = texture(InSampler, uv + vec2( 0.3125, -0.0625) * outputToInput).rgb
         + texture(InSampler, uv + vec2( 0.4375, -0.3125) * outputToInput).rgb
         + texture(InSampler, uv + vec2( 0.1875, -0.4375) * outputToInput).rgb
         + texture(InSampler, uv + vec2( 0.0625, -0.1875) * outputToInput).rgb
         + texture(InSampler, uv + vec2(-0.0625,  0.3125) * outputToInput).rgb
         + texture(InSampler, uv + vec2(-0.1875,  0.0625) * outputToInput).rgb
         + texture(InSampler, uv + vec2(-0.4375,  0.1875) * outputToInput).rgb
         + texture(InSampler, uv + vec2(-0.3125,  0.4375) * outputToInput).rgb;

fragColor = vec4(sum * 0.125, 1.0);
}
