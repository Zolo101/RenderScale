//? sodium {
package dev.zelo.renderscale.compat.sodium;

import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class RenderScaleSodiumConfig implements ConfigEntryPoint {
    private static final Identifier SCALE = id("scale");
    private static final Identifier FORCE_LINEAR = id("force_linear");
    //? >= 1.21.11
    private static final Identifier FSR = id("fsr");
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
                                        .setRange(1, 200, 1)
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
                                //?}
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
                                        .setRange(0, 200, 1)
                                        .setValueFormatter(RenderScaleSodiumConfig::formatIrisScale)
//                                        .setImpact(OptionImpact.VARIES)
                                )
                        )
                );
                        //?}
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("renderscale", path);
    }

    private static RenderScaleConfig config() {
        return RenderScale.getConfig();
    }

    private int getScalePercent() {
        return Math.round(config().scale * 100.0f);
    }

    private void setScalePercent(int value) {
        config().scale = value / 100.0f;
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