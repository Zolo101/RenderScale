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

// TODO: Have a "iris" variable
//? if fabric || neoforge
import net.irisshaders.iris.api.v0.IrisApi;

@Config(name = "renderscale")
public class RenderScaleConfig implements ConfigData {
    public float scale = 1.0f;
    public boolean forceLinear = false;

    // TODO: Support oculus?
    //? if fabric || neoforge {
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
        //? if fabric || neoforge {
        if (RenderScale.PLATFORM.isModLoaded("iris")) {
            if (IrisApi.getInstance().isShaderPackInUse() && irisScale > 0.0f) {
                return irisScale;
            } else {
                return scale;
            }
        } else {
            return scale;
        }
        //?} else {
         /*return scale;
        *///?}
    }

    // yes -> linear, no -> nearest
    public boolean getFilter() {
        return forceLinear || getScale() > 1.0;
    }
}