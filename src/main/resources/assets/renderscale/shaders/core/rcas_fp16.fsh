#version 450

#define A_GPU 1
#define A_GLSL 1

#moj_import <renderscale:fsr_precision.glsl>

#define FSR_RCAS_H 1
#define FSR_COLOUR AH3
#define FSR_SCALAR AH1
#define FSR_LOAD AH4
#define FSR_POSITION ASW2
#define FSR_RCAS_LOAD FsrRcasLoadH
#define FSR_RCAS_INPUT FsrRcasInputH
#define FSR_RCAS FsrRcasH

#moj_import <renderscale:ffx_a.glsl>
#moj_import <renderscale:ffx_fsr1.glsl>

uniform sampler2D InSampler;
out vec4 fragColor;

#moj_import <renderscale:fsr_rcas.glsl>
