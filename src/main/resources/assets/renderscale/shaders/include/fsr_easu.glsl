#ifdef RENDERSCALE_EXPLICIT_GATHER
// RenderPearl translates OpenGL shaders to GLSL 330, where SPIRV-Cross cannot
// emit textureGather with a nonzero component. Match its four texels explicitly.
vec4 renderScaleGather(vec2 p, int component)
{
    ivec2 size = textureSize(InSampler, 0);
    ivec2 base = ivec2(floor(p * vec2(size) - 0.5));
    ivec2 maximum = size - ivec2(1);
    return vec4(
        texelFetch(InSampler, clamp(base + ivec2(0, 1), ivec2(0), maximum), 0)[component],
        texelFetch(InSampler, clamp(base + ivec2(1, 1), ivec2(0), maximum), 0)[component],
        texelFetch(InSampler, clamp(base + ivec2(1, 0), ivec2(0), maximum), 0)[component],
        texelFetch(InSampler, clamp(base, ivec2(0), maximum), 0)[component]
    );
}
#define RENDERSCALE_GATHER(p, component) renderScaleGather(p, component)
#else
#define RENDERSCALE_GATHER(p, component) textureGather(InSampler, p, component)
#endif

/*
 * These callbacks implement the prototypes declared by ffx_fsr1.glsl.
 *
 * Minecraft samplers are normally combined sampler2D objects, unlike
 * AMD's Vulkan example, which declares separate texture and sampler objects.
 */
FSR_GATHER FSR_EASU_R(AF2 p)
{
return FSR_GATHER(RENDERSCALE_GATHER(p, 0));
}

FSR_GATHER FSR_EASU_G(AF2 p)
{
return FSR_GATHER(RENDERSCALE_GATHER(p, 1));
}

FSR_GATHER FSR_EASU_B(AF2 p)
{
return FSR_GATHER(RENDERSCALE_GATHER(p, 2));
}

void main()
{
AU2 outputPixel = AU2(gl_FragCoord.xy);
vec2 inputSize = vec2(textureSize(InSampler, 0));
AU4 fsrConst0;
AU4 fsrConst1;
AU4 fsrConst2;
AU4 fsrConst3;

FsrEasuCon(
fsrConst0,
fsrConst1,
fsrConst2,
fsrConst3,
inputSize.x,
inputSize.y,
inputSize.x,
inputSize.y,
ScreenSize.x,
ScreenSize.y
);

FSR_COLOUR colour;
FSR_EASU(
colour,
outputPixel,
fsrConst0,
fsrConst1,
fsrConst2,
fsrConst3
);

fragColor = vec4(colour, 1.0);
}
