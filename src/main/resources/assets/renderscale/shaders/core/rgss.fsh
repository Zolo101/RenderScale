#version 420

// Rotated Grid resolve filter for ordered-grid supersampling.
//
// The four taps sit on a grid rotated by arctan(1/2) (~26.6 degrees), so
// every tap reads the already-rendered high-resolution image at a
// different horizontal AND vertical offset within the output pixel's
// footprint. Compared to a plain box average of the same 2x-order
// source, angled and axis-crossing edges resolve to more intermediate
// shades instead of a two-step staircase. These taps filter the rendered
// image; they do not add geometry coverage samples.

#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

out vec4 fragColor;

void main()
{
// ScreenSize must match this pass's destination texture size.
// One output pixel expressed in normalized input UV coordinates.
vec2 outputToInput = 1.0 / ScreenSize;

vec2 uv = gl_FragCoord.xy / ScreenSize;

vec3 sum = texture(InSampler, uv + vec2( 0.125,  0.375) * outputToInput).rgb
         + texture(InSampler, uv + vec2( 0.375, -0.125) * outputToInput).rgb
         + texture(InSampler, uv + vec2(-0.125, -0.375) * outputToInput).rgb
         + texture(InSampler, uv + vec2(-0.375,  0.125) * outputToInput).rgb;

fragColor = vec4(sum * 0.25, 1.0);
}
