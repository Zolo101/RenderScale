#version 420

#define A_GPU 1
#define A_GLSL 1

#define FSR_EASU_F 1
#define FSR_COLOUR AF3
#define FSR_GATHER AF4
#define FSR_EASU_R FsrEasuRF
#define FSR_EASU_G FsrEasuGF
#define FSR_EASU_B FsrEasuBF
#define FSR_EASU FsrEasuF

#moj_import <renderscale:ffx_a.glsl>
#moj_import <renderscale:ffx_fsr1.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
out vec4 fragColor;

#moj_import <renderscale:fsr_easu.glsl>
