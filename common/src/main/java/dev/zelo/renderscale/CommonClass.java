package dev.zelo.renderscale;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.zelo.renderscale.accessors.GICommandEncoderThing;
import dev.zelo.renderscale.config.RenderScaleConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {
    private static Minecraft client = Minecraft.getInstance();

    // This is RenderScale's renderTarget
    @Nullable
    public RenderTarget renderTarget;

    // This is Minecraft's renderTarget
    @Nullable
    public RenderTarget clientRenderTarget;

    private static CommonClass instance;
    private boolean shouldScale = false;
    public boolean hasRun = false;

    public static final ConfigHolder<RenderScaleConfig> CONFIG = RenderScaleConfig.init();

    // Fabric
    public static void init() {
        instance = new CommonClass();
    }

    // NeoForge made it so that the mod loads before Minecraft (but not fabric...), so this is needed to get the "actual" Minecraft instance
    public static void init(Minecraft client) {
        instance = new CommonClass();
        CommonClass.client = client;

        RenderSystem
    }

    public static CommonClass getInstance() {
        return instance;
    }

    public static RenderScaleConfig getConfig() {
        return CONFIG.getConfig();
    }

    public void onResolutionChanged() {
        if (getWindow() == null) return;
        Constants.LOG.info("Size changed to {}x{} {}x{} {}x{}",
                getWindow().getWidth(), getWindow().getHeight(),
                getWindow().getScreenWidth(), getWindow().getScreenHeight(),
                getWindow().getGuiScaledWidth(), getWindow().getGuiScaledHeight());

        resizeRenderTarget();
    }

    public void setClientRenderTarget(RenderTarget renderTarget) {
        client.mainRenderTarget = renderTarget;
    }

    public void setShouldScale(boolean shouldScale) {
        Window window = client.getWindow();
        int width = window.getWidth();
        int height = window.getHeight();

        int scaledWidth = Math.clamp(width, 1, 65536);
        int scaledHeight = Math.clamp(height, 1, 65536);

        if (renderTarget == null) {
            renderTarget = new TextureTarget("RenderScale", scaledWidth, scaledHeight, true);
        }

        if (clientRenderTarget == null) {
            clientRenderTarget = client.getMainRenderTarget();
        }

        if (shouldScale) {
            setClientRenderTarget(renderTarget);
        } else {
            try {
                setClientRenderTarget(clientRenderTarget);

                ((GICommandEncoderThing) RenderSystem.getDevice().createCommandEncoder()).renderScale$copyAndResizeTexture(
                        renderTarget.getColorTexture(), clientRenderTarget.getColorTexture(),
                        0, 0, 0, 0, 0,
                        renderTarget.width, renderTarget.height,
                        width, height, false
                );
                ((GICommandEncoderThing) RenderSystem.getDevice().createCommandEncoder()).renderScale$copyAndResizeTexture(
                        renderTarget.getDepthTexture(), clientRenderTarget.getDepthTexture(),
                        0, 0, 0, 0, 0,
                        renderTarget.width, renderTarget.height,
                        width, height, true
                );
            } catch (Exception e) {
                Constants.LOG.error("Error copying texture", e);
            }
        }
    }

    // Takes into account shouldScale
    public double getCurrentScaleFactor() {
        return shouldScale ? getConfig().getScale() : 1;
    }

    @Nullable
    private Window getWindow() {
        return client.getWindow();
    }

    public void resizeRenderTarget() {
        if (renderTarget == null) return;

        boolean prev = shouldScale;
        shouldScale = true;

        Window window = client.getWindow();
        int width = window.getWidth();
        int height = window.getHeight();

        int scaledWidth = Math.clamp(width, 1, 65536);
        int scaledHeight = Math.clamp(height, 1, 65536);
        renderTarget.resize(scaledWidth, scaledHeight);

        shouldScale = prev;
    }
}
