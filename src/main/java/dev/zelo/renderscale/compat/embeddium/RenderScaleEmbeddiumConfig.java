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
                                    .setControl(opt -> new SliderControl(opt, 1, 400, 1, RenderScaleEmbeddiumConfig::percent))
                                    .setBinding((config, value) -> config.scale = value / 100.0f,
                                            config -> Math.round(config.scale * 100.0f))
                                    .build())
                            .add(option("fsr", Boolean.class)
                                    .setControl(TickBoxControl::new)
                                    .setBinding((config, value) -> config.fsr = value, config -> config.fsr)
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
            event.addPage(new OptionPage(id("iris"),
                    Component.translatable("text.autoconfig.renderscale.category.iris"),
                    ImmutableList.of(OptionGroup.createBuilder()
                            .setId(id("iris_options"))
                            .add(option("irisScale", Integer.class)
                                    .setName(Component.translatable("text.autoconfig.renderscale.option.irisScale.sodium"))
                                    .setTooltip(Component.translatable("text.autoconfig.renderscale.option.irisScale.@Tooltip.sodium"))
                                    .setControl(opt -> new SliderControl(opt, 0, 400, 1, value -> value == 0
                                            ? Component.translatable("text.autoconfig.renderscale.option.irisScale.same")
                                            : percent(value)))
                                    .setBinding((config, value) -> config.irisScale = value <= 0 ? -1.0f : value / 100.0f,
                                            config -> Float.isFinite(config.irisScale) && config.irisScale > 0.0f
                                                    ? Math.round(config.irisScale * 100.0f) : 0)
                                    .build())
                            .build())));
            event.addPage(new OptionPage(id("advanced"),
                    Component.translatable("text.autoconfig.renderscale.category.advanced"),
                    ImmutableList.of(OptionGroup.createBuilder()
                            .setId(id("advanced_options"))
                            .add(option("forceBilinear", Boolean.class)
                                    .setControl(TickBoxControl::new)
                                    .setBinding((config, value) -> config.forceBilinear = value, config -> config.forceBilinear)
                                    .build())
                            .add(option("downscaleFilter", Integer.class)
                                    .setControl(opt -> new SliderControl(opt, 0, RenderScaleConfig.DownscaleFilter.values().length - 1, 1,
                                            value -> Component.translatable(RenderScaleConfig.DownscaleFilter.values()[value].getKey())))
                                    .setBinding((config, value) -> config.downscaleFilter = RenderScaleConfig.DownscaleFilter.values()[value],
                                            config -> config.getDownscaleFilter().ordinal())
                                    .build())
                            .add(option("sharpeningMode", Integer.class)
                                    .setControl(opt -> new SliderControl(opt, 0, 2, 1, value -> Component.translatable(
                                            RenderScaleConfig.SharpeningMode.values()[value].getKey())))
                                    .setBinding((config, value) -> config.sharpeningMode = RenderScaleConfig.SharpeningMode.values()[value],
                                            config -> config.getSharpeningMode().ordinal())
                                    .build())
                            .add(option("sharpeningStrength", Integer.class)
                                    .setControl(opt -> new SliderControl(opt, 0, 100, 5, RenderScaleEmbeddiumConfig::percent))
                                    .setBinding((config, value) -> config.sharpeningStrength = RenderScaleConfig.normalizeSharpeningStrength(value),
                                            config -> config.sharpeningStrength)
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
