FSR_LOAD FSR_RCAS_LOAD(FSR_POSITION p)
{
ivec2 size = textureSize(InSampler, 0);

// RCAS samples one pixel outside the current location at image edges.
ivec2 position = clamp(ivec2(p), ivec2(0), size - ivec2(1));

return FSR_LOAD(texelFetch(InSampler, position, 0));
}

void FSR_RCAS_INPUT(inout FSR_SCALAR r, inout FSR_SCALAR g, inout FSR_SCALAR b)
{
// The required input is normalized, display-encoded (sRGB-style
// perceptual) RGB, which is what vanilla is expected to render into the
// main target. This comment is not verification of every attachment
// format or shader-pack colour path: do not add a colour conversion here
// without checking the actual format in game.
}

// The fixed sharpness tuning is AMD's shipped FSR1 default (stop parameter
// 0.2, i.e. gain 2^-0.2); the FSR pipeline relies on that default.
#ifndef RENDERSCALE_RCAS_GAIN
#define RENDERSCALE_RCAS_GAIN exp2(-0.2)
#endif

void main()
{
AU2 outputPixel = AU2(gl_FragCoord.xy);
#ifdef A_HALF
// RCAS H reads the packed half sharpness from con.y, unlike RCAS F's con.x.
AF1 sharpness = RENDERSCALE_RCAS_GAIN;
AU4 fsrConst0 = AU4(floatBitsToUint(sharpness), packFloat2x16(AH2(sharpness)), 0u, 0u);
#else
// FsrRcasF only uses the 32-bit sharpness in con.x. Avoid generating the unused
// half-float constants, which require packing functions absent from GLSL 330.
AU4 fsrConst0 = AU4(floatBitsToUint(RENDERSCALE_RCAS_GAIN), 0u, 0u, 0u);
#endif

FSR_COLOUR colour;
FSR_RCAS(
colour.r,
colour.g,
colour.b,
outputPixel,
fsrConst0
);

// Supersampling pipelines define RENDERSCALE_SHARPNESS (0-1) and blend
// towards the unsharpened centre pixel so the strength control is a
// consistent mix; the 0% setting is skipped CPU-side, since RCAS's fast
// normalization is not an exact passthrough. The Noise-protected variant
// additionally sets FSR_RCAS_DENOISE. The FSR upscale pipeline defines
// neither, so its output path is unchanged.
#ifdef RENDERSCALE_SHARPNESS
AF3 centre = texelFetch(InSampler, ivec2(outputPixel), 0).rgb;
fragColor = vec4(mix(centre, AF3(colour), RENDERSCALE_SHARPNESS), 1.0);
#else
fragColor = vec4(colour, 1.0);
#endif
}
