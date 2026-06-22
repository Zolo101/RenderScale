package dev.zelo.renderscale.config;

import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.compat.iris.IrisCompatibility;
import dev.zelo.renderscale.platform.Platform;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

//? iris
import net.irisshaders.iris.api.v0.IrisApi;

@Config(name = "renderscale")
public class RenderScaleConfig implements ConfigData {
    public float scale = 1.0f;
    public boolean forceLinear = false;

    //? >= 1.21.11
    @ConfigEntry.Gui.Tooltip()
    public boolean fsr = false;

//  double UltraQuality = 1.3; // (0.77)
//  double Quality = 1.5;      // (0.67)
//  double Balanced = 1.7;     // (0.59)
//  double Performance = 2.0;  // (0.5)

    // TODO: Support oculus?
    //? iris {
    @ConfigEntry.Category("iris")
    @ConfigEntry.Gui.Tooltip()
    public float irisScale = -1.0f;
    //?}

    public static ConfigHolder<RenderScaleConfig> init() {
        // Register config
        ConfigHolder<RenderScaleConfig> holder = AutoConfig.register(RenderScaleConfig.class, JanksonConfigSerializer::new);

        // Change resolution upon save!
        holder.registerSaveListener((manager, data) -> {
            RenderScale.getInstance().onResolutionChanged();
            IrisCompatibility.reloadShaders();
            return null;
        });

        return holder;
    }

    public float getScale() {
        // To avoid 0x0 crashes if the user FOR SOME REASON puts 0 as the scale
        float safeScale = Math.max(0.01f, scale);

        //? iris {
        if (RenderScale.PLATFORM.isModLoaded("iris")) {
            if (IrisApi.getInstance().isShaderPackInUse() && irisScale > 0.0f) {
                return irisScale;
            } else {
                return safeScale;
            }
        } else {
            return safeScale;
        }
        //?} else {
         /*return safeScale;
        *///?}
    }

    // true -> linear, false -> nearest
    public boolean getFilter() {
        //? >= 1.21.11 {
        return fsr || forceLinear || getScale() > 1.0;
        //?} else
        //return forceLinear || getScale() > 1.0;
    }
}
