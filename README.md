# RenderScale

RenderScale allows you to change Minecraft's resolution without affecting the UI.

This is a fork of [ResolutionControl++](https://github.com/ModLabsCC/Resolution-Control), with added support for NeoForge.

![Comparison of FPS for each common render scale](https://cdn.modrinth.com/data/cached_images/682bd81994c56be5aba19d1e04629083aae0b4d3.webp)

![Comparison of anti-aliasing for 1x and 2x render scales](https://cdn.modrinth.com/data/cached_images/05b00c51dd0c1c07b398160dfa161b67ddc36d16.png)

Check out [Fabrishot](https://modrinth.com/mod/fabrishot) if you also want the large screenshot feature that was in ResolutionControl.

---

# How

Press `O` or, use the mod menu config to control the render scale multiplier. You can increase for better antialiasing, or decrease for improved performance. This is heavily recommended for laptops with retina displays!

You can also force "linear" scale algorithm (similar to FXAA) in lower render scales if you want. It's best to leave it as OFF if you're using shaders since they usually have their own antialiasing.

There is experimental support for FSR 1.0 upscaling on 1.21.11 and above! No plans to support DLSS or FSR 2.0+.

There are plans for Dynamic Resolution!

---

# Compatibility

## ⚠️ This mod is NOT compatible with Lunar Client / Feather Client. It will break if you use them.

Aims to be compatible with any mod, including Sodium, Iris, etc. However, there are mods that can break RenderScale. You can report any issues [here](https://github.com/Zolo101/RenderScale/issues), or on my [discord](https://discord.com/invite/YVuuF9KB5j). Make sure to include your latest MC logs!

---

# Supported Versions

| Minecraft        | Fabric | NeoForge | Forge  |
|------------------|--------|----------|--------|
| 1.21.2 and above | ✅      | ✅        | 🚫     |
| 1.21.0 - 1.21.1  | ✅ [1]  | ✅ [1]    | 🚫     |
| 1.20.2 - 1.20.6  | 🚫     | 🚫       | 🚫     |
| 1.20.1           | ✅ [1]  | 🚫       | ✅  [1] |

[1] Does not support Fabulous graphics

Check out the previous forks, [ResolutionControl++](https://modrinth.com/mod/resolution-control-plus-plus), [ResolutionControl+](https://modrinth.com/mod/resolution-control-plus), [ResolutionControl](https://github.com/juliand665/Resolution-Control) for older versions