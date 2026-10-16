# 2.0.0-beta.1

1.20.1 and 1.21.1 now supports FSR 1 upscaling, and there's RGSS support!

## Changes
 
- Improve FSR 1 performance by 30% for GPUs that support FP16 shaders. 
- More supersampling filters (RGSS, Sparse Grid). By default, RenderScale automatically chooses the best one for your scale. 
- Added optional image sharpening with RCAS.
- (Parity) 1.20.1 Forge now allows setting a different scale for when shaders are enabled.

## Known Issues

- You cannot use Dynamic Scaling with shaders due to issues with stuttering.  
- Please [open an issue](https://github.com/Zolo101/RenderScale/issues) if RenderScale has FSR1 listed as FP32 but your GPU supports FP16. Use F3 to check  
- I've changed the "Force Linear" setting internally, if you had it enabled in a previous version you'll have to enable it again.