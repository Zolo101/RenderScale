// Set by the renderer only after checking the active device's capabilities.
#ifdef RENDERSCALE_FP16
#if RENDERSCALE_FP16 == 1
#extension GL_EXT_shader_explicit_arithmetic_types_float16 : require
#elif RENDERSCALE_FP16 == 2
#extension GL_AMD_gpu_shader_half_float : require
#elif RENDERSCALE_FP16 == 3
#extension GL_NV_gpu_shader5 : require
#endif

#define A_HALF 1
#define A_SKIP_EXT 1

// FSR needs FP16 arithmetic, but not 16-bit storage or integer arithmetic.
// Keep pixel coordinates and bit manipulation at 32 bits, including on GPUs
// that expose half floats without int16. Packing preserves the half bit pattern.
#define uint16_t uint
#define u16vec2 uvec2
#define u16vec3 uvec3
#define u16vec4 uvec4
#define int16_t int
#define i16vec2 ivec2
#define i16vec3 ivec3
#define i16vec4 ivec4

#define packUint2x16(value) (((value).x & 0xffffu) | (((value).y & 0xffffu) << 16u))
#define unpackUint2x16(value) (uvec2(value, (value) >> 16u) & uvec2(0xffffu))

uint renderScaleHalfBits(float16_t value) {
    return packFloat2x16(f16vec2(value, float16_t(0.0))) & 0xffffu;
}
float16_t renderScaleBitsHalf(uint value) {
    return unpackFloat2x16(value & 0xffffu).x;
}
uvec2 renderScaleHalfBits(f16vec2 value) {
    return uvec2(renderScaleHalfBits(value.x), renderScaleHalfBits(value.y));
}
f16vec2 renderScaleBitsHalf(uvec2 value) {
    return f16vec2(renderScaleBitsHalf(value.x), renderScaleBitsHalf(value.y));
}
uvec3 renderScaleHalfBits(f16vec3 value) {
    return uvec3(renderScaleHalfBits(value.x), renderScaleHalfBits(value.y), renderScaleHalfBits(value.z));
}
f16vec3 renderScaleBitsHalf(uvec3 value) {
    return f16vec3(renderScaleBitsHalf(value.x), renderScaleBitsHalf(value.y), renderScaleBitsHalf(value.z));
}
uvec4 renderScaleHalfBits(f16vec4 value) {
    return uvec4(renderScaleHalfBits(value.x), renderScaleHalfBits(value.y), renderScaleHalfBits(value.z), renderScaleHalfBits(value.w));
}
f16vec4 renderScaleBitsHalf(uvec4 value) {
    return f16vec4(renderScaleBitsHalf(value.x), renderScaleBitsHalf(value.y), renderScaleBitsHalf(value.z), renderScaleBitsHalf(value.w));
}
#define halfBitsToUint16 renderScaleHalfBits
#define uint16BitsToHalf renderScaleBitsHalf
#endif
