//? if forge {
/*package dev.zelo.renderscale.compat.embeddium;

import com.google.common.collect.ImmutableList;
import dev.zelo.renderscale.Constants;
import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.network.chat.Component;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import org.embeddedt.embeddium.client.gui.options.OptionIdentifier;

/^* Loaded only when Embeddium is installed. Options remain staged until Apply is pressed. ^/
public final class RenderScaleEmbeddiumConfig {
    private static final OptionStorage<RenderScaleConfig> STORAGE = new OptionStorage<>() {
        @Override
        public RenderScaleConfig getData() {
            return RenderScale.getConfig();
        }

        @Override
        public void save() {
            // Use the existing save listener to resize the render targets as well.
            RenderScale.CONFIG.save();
        }
    };

    private RenderScaleEmbeddiumConfig() {}

    public static void register() {
        OptionGUIConstructionEvent.BUS.addListener(event -> {
            event.addPage(new OptionPage(id("general"),
                    Component.translatable("text.autoconfig.renderscale.category.default"),
                    ImmutableList.of(OptionGroup.createBuilder()
                            .setId(id("general_options"))
                            .add(option("scale", Integer.class)
                                    .setControl(opt -> new SliderControl(opt, 1, 200, 1, RenderScaleEmbeddiumConfig::percent))
                                    .setBinding((config, value) -> config.scale = value / 100.0f,
                                            config -> Math.round(config.scale * 100.0f))
                                    .build())
                            .add(option("forceLinear", Boolean.class)
                                    .setControl(TickBoxControl::new)
                                    .setBinding((config, value) -> config.forceLinear = value, config -> config.forceLinear)
                                    .build())
                            .build())));
            event.addPage(new OptionPage(id("dynamic"),
                    Component.translatable("text.autoconfig.renderscale.category.dynamic"),
                    ImmutableList.of(OptionGroup.createBuilder()
                            .setId(id("dynamic_options"))
                            .add(dynamicOption("targetFrameRate")
                                    .setControl(opt -> new SliderControl(opt, 0, 540, 10, value -> value == 0
                                            ? Component.translatable("text.autoconfig.renderscale.option.targetFrameRate.off")
                                            : Component.literal(value + " FPS")))
                                    .setBinding((config, value) -> config.targetFrameRate = value, RenderScaleConfig::getTargetFrameRate)
                                    .build())
                            .add(dynamicOption("aggression")
                                    .setControl(opt -> new SliderControl(opt, 0, 3, 1, value -> Component.translatable(
                                            RenderScaleConfig.Aggression.values()[value].getKey())))
                                    .setBinding((config, value) -> config.aggressionLevel = RenderScaleConfig.Aggression.values()[value],
                                            config -> config.aggressionLevel.ordinal())
                                    .build())
                            .add(dynamicOption("minimumScale")
                                    .setControl(opt -> new SliderControl(opt, 10, 100, 1, RenderScaleEmbeddiumConfig::percent))
                                    .setBinding((config, value) -> config.minimumScale = value / 100.0f,
                                            config -> Math.round(config.minimumScale * 100.0f))
                                    .build())
                            .build())));
        });
    }

    private static <T> OptionImpl.Builder<RenderScaleConfig, T> option(String name, Class<T> type) {
        return OptionImpl.createBuilder(type, STORAGE)
                .setId(OptionIdentifier.create(Constants.MOD_ID, name, type))
                .setName(Component.translatable("text.autoconfig.renderscale.option." + name))
                .setTooltip(Component.translatable("text.autoconfig.renderscale.option." + name + ".@Tooltip"));
    }

    private static OptionImpl.Builder<RenderScaleConfig, Integer> dynamicOption(String name) {
        return option(name, Integer.class)
                .setEnabledPredicate(RenderScaleConfig::isDynamicScaleAvailable)
                .setTooltip(Component.translatable(RenderScaleConfig.isDynamicScaleAvailable()
                        ? "text.autoconfig.renderscale.option." + name + ".@Tooltip"
                        : "text.autoconfig.renderscale.category.dynamic.unavailable"));
    }

    private static OptionIdentifier<Void> id(String path) {
        return OptionIdentifier.create(Constants.MOD_ID, path);
    }

    private static Component percent(int value) {
        return Component.literal(value + "%");
    }
}
*///?}
