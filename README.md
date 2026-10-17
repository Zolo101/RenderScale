# RenderScale

RenderScale allows you to change Minecraft's resolution without affecting the UI. You can increase scale for antialiasing, or lower to improve performance. This is heavily recommended for laptops with retina displays!

This is a fork of [ResolutionControl++](https://github.com/ModLabsCC/Resolution-Control), with added support for NeoForge.

![Comparison of FPS for each common render scale](https://cdn.modrinth.com/data/cached_images/682bd81994c56be5aba19d1e04629083aae0b4d3.webp)

![Comparison of antialiasing for 1x and 2x render scales](https://cdn.modrinth.com/data/cached_images/05b00c51dd0c1c07b398160dfa161b67ddc36d16.png)

Check out [Fabrishot](https://modrinth.com/mod/fabrishot) if you also want the large screenshot feature that was in ResolutionControl.

---

# How to use

Press `U` (sometimes `O`) or, use the mod menu config to control the render scale multiplier.

## Other features

- **FSR 1** upscaling! Currently only available on 1.20.1, 1.21.1, and 1.21.11+. There are no plans to support DLSS or FSR 2+.
- **Dynamic Scaling**, tries to keep the game at your targeted FPS by continuously changing the scale while you play. 
- **Optional sharpening** post-processing.

---

# Compatibility

## ⚠️ This mod is NOT compatible with Lunar Client / Feather Client. It will break if you use them.

Aims to be compatible with any mod, including Sodium, Iris, etc. However, there can be mods that break RenderScale. Please report any issues [here](https://github.com/Zolo101/RenderScale/issues), or on my [discord](https://discord.com/invite/YVuuF9KB5j). Make sure to include your latest MC logs!

(26.2+) Works with Vulkan, including Iris Vulkan.

Dynamic scaling is unavailable while shaders are enabled due to issues with stuttering.

---

# Supported Versions

| Minecraft        | Fabric  | NeoForge | Forge    |
|------------------|---------|----------|----------|
| 1.21.2 and above | ✅      | ✅       | 🚫       |
| 1.21.1           | ✅ [^1] | ✅ [^1]  | 🚫       |
| 1.20.1           | ✅ [^1] | 🚫       | ✅  [^1] |

[^1]: Does not support Fabulous graphics

Check out the previous forks, [ResolutionControl++](https://modrinth.com/mod/resolution-control-plus-plus), [ResolutionControl+](https://modrinth.com/mod/resolution-control-plus), [ResolutionControl](https://github.com/juliand665/Resolution-Control) for older versions
