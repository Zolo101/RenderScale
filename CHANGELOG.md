# 2.0.0-beta.1

1.20.1 and 1.21.1 now support FSR 1 upscaling and Rotated Grid and Sparse Grid downsampling!

Also added Russian translations, thanks to mpustovoi!

## Fixes

- (26.3) Fixed issue with Iris shaders causing clouds to render in front of terrain [#111](https://github.com/Zolo101/RenderScale/issues/111)
- (1.21.1) Fixed scales above 100% not being downscaled properly

## Changes
 
- Improve FSR 1 performance by 30% for GPUs that support FP16 shaders. 
- More supersampling filters (RGSS, Sparse Grid). By default, RenderScale automatically chooses the best one for your scale. 
- Added optional image sharpening with RCAS.
- (Parity) 1.20.1 Forge now allows setting a different scale for when shaders are enabled.

## Known Issues

- You cannot use Dynamic Scaling with shaders due to issues with stuttering.  
- Please [open an issue](https://github.com/Zolo101/RenderScale/issues) if RenderScale has FSR1 listed as FP32 but your GPU supports FP16. Use F3 to check  
- I've changed the "Force Linear" setting internally, if you had it enabled in a previous version you'll have to enable it again.
