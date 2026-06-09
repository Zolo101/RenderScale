package dev.zelo.renderscale;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import dev.zelo.renderscale.config.RenderScaleConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Set;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CommonClass {
    private static final Minecraft client = Minecraft.getInstance();
    @Nullable
    private RenderTarget renderTarget;

    @Nullable
    private RenderTarget clientRenderTarget;

    private Set<RenderTarget> minecraftRenderTargets;

    private static CommonClass instance;
    private boolean shouldScale = false;
    public boolean hasRun = false;

    public static final ConfigHolder<RenderScaleConfig> CONFIG = RenderScaleConfig.init();

    public static void init() {
        instance = new CommonClass();
    }

    public static CommonClass getInstance() {
        return instance;
    }

    public static RenderScaleConfig getConfig() {
        return CONFIG.getConfig();
    }

    public void initMinecraftRenderTargets() {
        if (minecraftRenderTargets != null) {
            minecraftRenderTargets.clear();
        } else {
            minecraftRenderTargets = new HashSet<>();
        }

        minecraftRenderTargets.add(client.levelRenderer.entityTarget());
        minecraftRenderTargets.remove(null);
    }

    public void onResolutionChanged() {
        if (getWindow() == null) return;
        Constants.LOG.info("Size changed to {}x{} {}x{} {}x{}",
                getWindow().getWidth(), getWindow().getHeight(),
                getWindow().getScreenWidth(), getWindow().getScreenHeight(),
                getWindow().getGuiScaledWidth(), getWindow().getGuiScaledHeight());

        Window window = client.getWindow();
        updateRenderTargetSize();

        // TODO: idk why but we gotta do this to make the glow stay... investigate more...
        client.levelRenderer.resize(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }

    public void updateRenderTargetSize() {
        if (renderTarget == null) return;

        resize(renderTarget);
        resize(client.levelRenderer.entityTarget());
        resizeMinecraftRenderTargetSize();
    }

    public void resizeMinecraftRenderTargetSize() {
        initMinecraftRenderTargets();
        minecraftRenderTargets.forEach(this::resize);
        this.resize(client.levelRenderer.entityEffect);
    }

    public void setShouldScale(boolean shouldScale) {
        if (this.shouldScale == shouldScale) return;

        Window window = client.getWindow();
        if (renderTarget == null) {
            this.shouldScale = true;
            renderTarget = new MainTarget(Math.max(1, window.getWidth()), Math.max(1, window.getHeight()));
        }

        this.shouldScale = shouldScale;

        if (shouldScale) {
            clientRenderTarget = client.getMainRenderTarget();

            setClientRenderTarget(renderTarget);
            renderTarget.bindWrite(true);
        } else {
            setClientRenderTarget(clientRenderTarget);
            client.getMainRenderTarget().bindWrite(true);

            // TODO: Support fabulous graphics. Right now disableBlend = true shows the other passes but messes up the main target
            renderTarget.blitToScreen(Math.max(1, window.getWidth()), Math.max(1, window.getHeight()), false);
        }
    }

    public double getCurrentScaleFactor() {
        return shouldScale ? CommonClass.getConfig().getScale() : 1;
    }

    private Window getWindow() {
        return client.getWindow();
    }

    private void setClientRenderTarget(RenderTarget renderTarget) {
        client.mainRenderTarget = renderTarget;
    }

    public void resize(@Nullable RenderTarget renderTarget) {
        if (renderTarget == null) return;

        boolean prev = shouldScale;
        shouldScale = true;

        Window window = client.getWindow();
        int width = Math.max(1, window.getWidth());
        int height = Math.max(1, window.getHeight());
        renderTarget.resize(width, height, Minecraft.ON_OSX);

        shouldScale = prev;
    }

    public void resize(@Nullable PostChain postChain) {
        if (postChain == null) return;

        boolean prev = shouldScale;
        shouldScale = true;

        Window window = client.getWindow();
        int width = Math.max(1, window.getWidth());
        int height = Math.max(1, window.getHeight());

        for (RenderTarget target : postChain.fullSizedTargets) {
            target.resize(width, height, Minecraft.ON_OSX);
        }

        shouldScale = prev;
    }
}
