FSR_LOAD FSR_RCAS_LOAD(FSR_POSITION p)
{
ivec2 size = textureSize(InSampler, 0);

// RCAS samples one pixel outside the current location at image edges.
ivec2 position = clamp(ivec2(p), ivec2(0), size - ivec2(1));

return FSR_LOAD(texelFetch(InSampler, position, 0));
}

void FSR_RCAS_INPUT(inout FSR_SCALAR r, inout FSR_SCALAR g, inout FSR_SCALAR b)
{
// Leave empty unless you require an input colour conversion.
}

void main()
{
AU2 outputPixel = AU2(gl_FragCoord.xy);
#ifdef A_HALF
// RCAS H reads the packed half sharpness from con.y, unlike RCAS F's con.x.
AF1 sharpness = exp2(-0.2);
AU4 fsrConst0 = AU4(floatBitsToUint(sharpness), packFloat2x16(AH2(sharpness)), 0u, 0u);
#else
// FsrRcasF only uses the 32-bit sharpness in con.x. Avoid generating the unused
// half-float constants, which require packing functions absent from GLSL 330.
AU4 fsrConst0 = AU4(floatBitsToUint(exp2(-0.2)), 0u, 0u, 0u);
#endif

FSR_COLOUR colour;
FSR_RCAS(
colour.r,
colour.g,
colour.b,
outputPixel,
fsrConst0
);

fragColor = vec4(colour, 1.0);
}
