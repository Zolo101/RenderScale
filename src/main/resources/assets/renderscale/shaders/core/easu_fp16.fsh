#version 450

#define A_GPU 1
#define A_GLSL 1

#moj_import <renderscale:fsr_precision.glsl>

#define FSR_EASU_H 1
#define FSR_COLOUR AH3
#define FSR_GATHER AH4
#define FSR_EASU_R FsrEasuRH
#define FSR_EASU_G FsrEasuGH
#define FSR_EASU_B FsrEasuBH
#define FSR_EASU FsrEasuH

#moj_import <renderscale:ffx_a.glsl>
#moj_import <renderscale:ffx_fsr1.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;
out vec4 fragColor;

#moj_import <renderscale:fsr_easu.glsl>
