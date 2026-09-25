//? sodium {
package dev.zelo.renderscale.compat.sodium;

import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class RenderScaleSodiumConfig implements ConfigEntryPoint {
    private static final Identifier SCALE = id("scale");
    private static final Identifier FORCE_LINEAR = id("force_linear");
    private static final Identifier TARGET_FRAME_RATE = id("target_frame_rate");
    private static final Identifier AGGRESSION = id("aggression");
    private static final Identifier MINIMUM_SCALE = id("minimum_scale");
    //? >= 1.21.11
    private static final Identifier FSR = id("fsr");
    //? >= 1.21.11
    private static final Identifier DOWNSCALE_FILTER = id("downscale_filter");
    //? >= 1.21.11
    private static final Identifier SHARPENING_MODE = id("sharpening_mode");
    //? >= 1.21.11
    private static final Identifier SHARPENING_STRENGTH = id("sharpening_strength");
    //? iris
    private static final Identifier IRIS_SCALE = id("iris_scale");

    public static final Identifier MONO = Identifier.fromNamespaceAndPath("renderscale", "textures/gui/config-icon-mono.png");
    public static final Identifier COLOUR = Identifier.fromNamespaceAndPath("renderscale", "textures/gui/config-icon.png");

    private final StorageEventHandler storageHandler = RenderScale.CONFIG::save;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .setIcon(MONO)
                .setNonTintedIcon(COLOUR)
                .setColorTheme(builder.createColorTheme().setBaseThemeRGB(0x02c934))
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("text.autoconfig.renderscale.category.default"))
                        .addOptionGroup(builder.createOptionGroup()
//                                .setName(Component.translatable("text.autoconfig.renderscale.category.default"))
                                .addOption(builder.createIntegerOption(SCALE)
                                        .setName(Component.translatable("text.autoconfig.renderscale.option.scale"))
                                        .setTooltip(Component.translatable("text.autoconfig.renderscale.option.scale.@Tooltip"))
                                        .setStorageHandler(this.storageHandler)
                                        .setBinding(this::setScalePercent, this::getScalePercent)
                                        .setDefaultValue(100)
                                        // Upper end of the slider must include the 300-400% scales
                                        // where the Sparse Grid filter activates. Those filters only
                                        // exist on 1.21.11+, so older versions keep the original range.
                                        //? >= 1.21.11 {
                                        .setRange(1, 400, 1)
                                        //?} else
                                        //.setRange(1, 200, 1)
                                        .setValueFormatter(RenderScaleSodiumConfig::formatPercent)
//                                        .setImpact(OptionImpact.VARIES)
                                )
                                .addOption(builder.createBooleanOption(FORCE_LINEAR)
                                        .setName(Component.translatable("text.autoconfig.renderscale.option.forceLinear"))
                                        .setTooltip(Component.translatable("text.autoconfig.renderscale.option.forceLinear.@Tooltip"))
                                        .setStorageHandler(this.storageHandler)
                                        .setBinding(v -> config().forceLinear = v, () -> config().forceLinear)
                                        .setDefaultValue(false)
                                        .setImpact(OptionImpact.LOW)
                                )
                                //? >= 1.21.11 {
                                .addOption(builder.createBooleanOption(FSR)
                                        .setName(Component.translatable("text.autoconfig.renderscale.option.fsr"))
                                        .setTooltip(Component.translatable("text.autoconfig.renderscale.option.fsr.@Tooltip.sodium"))
                                        .setStorageHandler(this.storageHandler)
                                        .setBinding(v -> config().fsr = v, () -> config().fsr)
                                        .setDefaultValue(false)
                                        .setImpact(OptionImpact.MEDIUM)
                                )
                                .addOption(builder.createIntegerOption(DOWNSCALE_FILTER)
                                        .setName(Component.translatable("text.autoconfig.renderscale.option.downscaleFilter"))
                                        .setTooltip(Component.translatable("text.autoconfig.renderscale.option.downscaleFilter.@Tooltip"))
                                        .setStorageHandler(this.storageHandler)
                                        .setBinding(v -> config().downscaleFilter = RenderScaleConfig.DownscaleFilter.values()[v],
                                                () -> config().getDownscaleFilter().ordinal())
                                        .setDefaultValue(0)
                                        .setRange(0, RenderScaleConfig.DownscaleFilter.values().length - 1, 1)
                                        .setValueFormatter(value -> Component.translatable(
                                                "text.autoconfig.renderscale.option.downscaleFilter."
                                                        + RenderScaleConfig.DownscaleFilter.values()[value].name()))
                                )
                                .addOption(builder.createIntegerOption(SHARPENING_MODE)
                                        .setName(Component.translatable("text.autoconfig.renderscale.option.sharpeningMode"))
                                        .setTooltip(Component.translatable("text.autoconfig.renderscale.option.sharpeningMode.@Tooltip"))
                                        .setStorageHandler(this.storageHandler)
                                        .setBinding(v -> config().sharpeningMode = RenderScaleConfig.SharpeningMode.values()[v],
                                                () -> config().getSharpeningMode().ordinal())
                                        .setDefaultValue(0)
                                        .setRange(0, RenderScaleConfig.SharpeningMode.values().length - 1, 1)
                                        .setValueFormatter(value -> Component.translatable(
                                                "text.autoconfig.renderscale.option.sharpeningMode."
                                                        + RenderScaleConfig.SharpeningMode.values()[value].name()))
                                )
                                .addOption(builder.createIntegerOption(SHARPENING_STRENGTH)
                                        .setName(Component.translatable("text.autoconfig.renderscale.option.sharpeningStrength"))
                                        .setTooltip(Component.translatable("text.autoconfig.renderscale.option.sharpeningStrength.@Tooltip"))
                                        .setStorageHandler(this.storageHandler)
                                        .setBinding(v -> config().sharpeningStrength = RenderScaleConfig.normalizeSharpeningStrength(v),
                                                () -> config().sharpeningStrength)
                                        .setDefaultValue(35)
                                        .setRange(0, 100, 5)
                                        .setValueFormatter(RenderScaleSodiumConfig::formatPercent)
                                        .setImpact(OptionImpact.LOW)
                                )
                                //?}
                        )
                )
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("text.autoconfig.renderscale.category.dynamic"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createIntegerOption(TARGET_FRAME_RATE)
                                        .setName(Component.translatable("text.autoconfig.renderscale.option.targetFrameRate"))
                                        .setTooltip(value -> dynamicScaleTooltip("targetFrameRate"))
                                        // Shader state changes outside Sodium's option dependency graph.
                                        .setEnabledProvider(state -> RenderScaleConfig.isDynamicScaleAvailable(), ConfigState.UPDATE_ON_REBUILD)
                                        .setControlHiddenWhenDisabled(false)
                                        .setStorageHandler(this.storageHandler)
                                        .setBinding(v -> config().targetFrameRate = v, () -> config().getTargetFrameRate())
                                        .setDefaultValue(0)
                                        .setRange(0, 540, 10)
                                        .setValueFormatter(value -> value == 0
                                                ? Component.translatable("text.autoconfig.renderscale.option.targetFrameRate.off")
                                                : Component.literal(value + " FPS"))
                                )
                                .addOption(builder.createIntegerOption(AGGRESSION)
                                        .setName(Component.translatable("text.autoconfig.renderscale.option.aggression"))
                                        .setTooltip(value -> dynamicScaleTooltip("aggression"))
                                        .setEnabledProvider(state -> RenderScaleConfig.isDynamicScaleAvailable(), ConfigState.UPDATE_ON_REBUILD)
                                        .setControlHiddenWhenDisabled(false)
                                        .setStorageHandler(this.storageHandler)
                                        .setBinding(v -> config().aggressionLevel = RenderScaleConfig.Aggression.values()[v],
                                                () -> config().aggressionLevel.ordinal())
                                        .setDefaultValue(1)
                                        .setRange(0, 3, 1)
                                        .setValueFormatter(value -> Component.translatable(
                                                "text.autoconfig.renderscale.option.aggressionLevel."
                                                        + RenderScaleConfig.Aggression.values()[value].name()))
                                )
                                .addOption(builder.createIntegerOption(MINIMUM_SCALE)
                                        .setName(Component.translatable("text.autoconfig.renderscale.option.minimumScale"))
                                        .setTooltip(value -> dynamicScaleTooltip("minimumScale"))
                                        .setEnabledProvider(state -> RenderScaleConfig.isDynamicScaleAvailable(), ConfigState.UPDATE_ON_REBUILD)
                                        .setControlHiddenWhenDisabled(false)
                                        .setStorageHandler(this.storageHandler)
                                        .setBinding(this::setMinimumScalePercent, this::getMinimumScalePercent)
                                        .setDefaultValue(10)
                                        .setRange(10, 100, 1)
                                        .setValueFormatter(RenderScaleSodiumConfig::formatPercent)
                                )
                        )
                )
                        //? iris {
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("text.autoconfig.renderscale.category.iris"))
                                .addOptionGroup(builder.createOptionGroup()
//                                .setName(Component.translatable("text.autoconfig.renderscale.category.iris"))
                                .addOption(builder.createIntegerOption(IRIS_SCALE)
                                        .setName(Component.translatable("text.autoconfig.renderscale.option.irisScale.sodium"))
                                        .setTooltip(Component.translatable("text.autoconfig.renderscale.option.irisScale.@Tooltip.sodium"))
                                        .setStorageHandler(this.storageHandler)
                                        .setBinding(this::setIrisScalePercent, this::getIrisScalePercent)
                                        .setDefaultValue(0)
                                        //? >= 1.21.11 {
                                        .setRange(0, 400, 1)
                                        //?} else
                                        //.setRange(0, 200, 1)
                                        .setValueFormatter(RenderScaleSodiumConfig::formatIrisScale)
//                                        .setImpact(OptionImpact.VARIES)
                                )
                        )
                )
                        //?}
                ;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("renderscale", path);
    }

    private static RenderScaleConfig config() {
        return RenderScale.getConfig();
    }

    private static Component dynamicScaleTooltip(String option) {
        if (!RenderScaleConfig.isDynamicScaleAvailable()) {
            return Component.translatable("text.autoconfig.renderscale.category.dynamic.unavailable");
        }
        return Component.translatable("text.autoconfig.renderscale.option." + option + ".@Tooltip");
    }

    private int getScalePercent() {
        return Math.round(config().scale * 100.0f);
    }

    private void setScalePercent(int value) {
        config().scale = value / 100.0f;
    }

    private int getMinimumScalePercent() {
        return Math.round(config().minimumScale * 100.0f);
    }

    private void setMinimumScalePercent(int value) {
        config().minimumScale = value / 100.0f;
    }


    private int getIrisScalePercent() {
        if (config().irisScale <= 0.0f) {
            return 0;
        }

        return Math.round(config().irisScale * 100.0f);
    }

    private void setIrisScalePercent(int value) {
        config().irisScale = value <= 0 ? -1.0f : value / 100.0f;
    }

    private static Component formatPercent(int value) {
        return Component.literal(value + "%");
    }

    private static Component formatIrisScale(int value) {
        if (value <= 0) {
            return Component.translatable("text.autoconfig.renderscale.option.irisScale.same");
        }

        return formatPercent(value);
    }
}
//?}
