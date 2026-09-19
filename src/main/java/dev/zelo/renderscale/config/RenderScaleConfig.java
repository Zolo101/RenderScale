package dev.zelo.renderscale.config;

import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.compat.iris.IrisCompatibility;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

//? if stutter {
/*import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.network.chat.Component;
import java.util.Optional;
*///?}

//? iris || (=1.20.1 && forge)
import net.irisshaders.iris.api.v0.IrisApi;

@Config(name = "renderscale")
public class RenderScaleConfig implements ConfigData {
    public float scale = 1.0f;
    public boolean forceLinear = false;

    @ConfigEntry.Category("dynamic")
    // Tooltips on affected shader versions come from the compatibility transformer below.
    //? !stutter
    @ConfigEntry.Gui.Tooltip()
//    @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
    public int targetFrameRate = 0;

    @ConfigEntry.Category("dynamic")
    //? !stutter
    @ConfigEntry.Gui.Tooltip()
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Aggression aggressionLevel = Aggression.NORMAL;

    public enum Aggression implements me.shedaniel.clothconfig2.gui.entries.SelectionListEntry.Translatable {
        CALM(25), NORMAL(50), AGGRESSIVE(80), EXTREME(100);

        public final int strength;

        Aggression(int strength) {
            this.strength = strength;
        }

        @Override
        public String getKey() {
            return "text.autoconfig.renderscale.option.aggressionLevel." + name();
        }
    }

    @Override
    public void validatePostLoad() {
        if (aggressionLevel == null) aggressionLevel = Aggression.NORMAL;
    }

    @ConfigEntry.Category("dynamic")
    //? !stutter
    @ConfigEntry.Gui.Tooltip()
//    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    public float minimumScale = 0.1f;

    //? >= 1.21.11 {
    @ConfigEntry.Gui.Tooltip()
    public boolean fsr = false;
    //?}

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

        //? if stutter {
        /*AutoConfig.getGuiRegistry(RenderScaleConfig.class).registerPredicateTransformer(
                (entries, key, field, config, defaults, registry) -> {
                    boolean available = isDynamicScaleAvailable();
                    for (var entry : entries) {
                        entry.setEditable(available);
                        if (entry instanceof TooltipListEntry<?> tooltipEntry) {
                            tooltipEntry.setTooltipSupplier(() -> Optional.of(new Component[] {
                                    Component.translatable(available ? key + ".@Tooltip"
                                            : "text.autoconfig.renderscale.category.dynamic.unavailable")
                            }));
                        }
                    }
                    return entries;
                }, field -> {
                    ConfigEntry.Category category = field.getAnnotation(ConfigEntry.Category.class);
                    return category != null && category.value().equals("dynamic");
                });
        *///?}

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
        float safeScale = Float.isFinite(scale) ? Math.max(0.01f, scale) : 1.0f;

        //? iris {
        if (RenderScale.PLATFORM.isModLoaded("iris")) {
            if (IrisApi.getInstance().isShaderPackInUse() && Float.isFinite(irisScale) && irisScale > 0.0f) {
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

    public boolean isDynamicScaleEnabled() {
        return targetFrameRate > 0 && isDynamicScaleAvailable();
    }

    public static boolean isDynamicScaleAvailable() {
        // Iris on 1.21.1 and Oculus on Forge 1.20.1 stutter when shader targets repeatedly resize.
        // Keep the saved settings so scaling can resume when shaders are disabled.
        //? if =1.21.1 && iris {
        /*if (RenderScale.PLATFORM.isModLoaded("iris") && IrisApi.getInstance().isShaderPackInUse()) {
            return false;
        }
        *///?}
        //? if =1.20.1 && forge {
        /*if (RenderScale.PLATFORM.isModLoaded("oculus") && IrisApi.getInstance().isShaderPackInUse()) {
            return false;
        }
        *///?}
        return true;
    }

    public int getTargetFrameRate() {
        return targetFrameRate;
    }

    public double getMinimumScale() {
        return Math.min(getScale(), Math.max(0.0, minimumScale));
    }

    // true -> linear, false -> nearest
    public boolean getFilter() {
        double effectiveScale = RenderScale.getInstance() == null ? getScale()
                : RenderScale.getInstance().getRenderScaleFactor();
        //? >= 1.21.11 {
        return fsr || forceLinear || effectiveScale > 1.0;
        //?} else
        //return forceLinear || effectiveScale > 1.0;
    }
}
