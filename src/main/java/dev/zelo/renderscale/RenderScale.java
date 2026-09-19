package dev.zelo.renderscale;

//~ if >=26.3 'bindTexture' -> 'setUniform' {

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;

//? >= 1.21.11 {
//? >=26.3 {
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//?} else
//import com.mojang.blaze3d.pipeline.RenderPipeline;

//? 1.21.11
//import com.mojang.blaze3d.platform.DepthTestFunction;

//? <26.3
//import static net.minecraft.client.renderer.RenderPipelines.GLOBALS_SNIPPET;
//?}
import com.mojang.blaze3d.platform.Window;
//? <=26.1 {
/*import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
*///?}
import dev.zelo.renderscale.accessors.MainRenderTargetSetter;
import dev.zelo.renderscale.config.RenderScaleConfig;
import dev.zelo.renderscale.platform.Platform;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

//? > 26.1 {
//? >=26.3 {
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
//?} else
//import com.mojang.blaze3d.PrimitiveTopology;
import net.minecraft.client.renderer.BindGroupLayouts;
//?}

//? >= 1.21.5 && < 1.21.11
//import dev.zelo.renderscale.accessors.GICommandEncoderThing;

//? >= 1.21.5 {
//? >=26.3 {
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.FilterMode;
//?} else {
/*import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.FilterMode;
*///?}
import net.minecraft.client.renderer.RenderPipelines;
//?}

//? > 1.21.1
import net.minecraft.util.profiling.Profiler;

//? fabric {
import dev.zelo.renderscale.platform.fabric.FabricPlatform;
//?} neoforge {
/*import dev.zelo.renderscale.platform.neoforge.NeoforgePlatform;
 *///?} forge {
/*import dev.zelo.renderscale.platform.forge.ForgePlatform;
 *///?}

//? >= 1.21.5 {

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;

//? > 26.1 {
import java.util.Optional;
//? } else
//import java.util.OptionalInt;

//?}

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class RenderScale {
    private static Minecraft client = Minecraft.getInstance();

    // This is RenderScale's renderTarget (scaled)
    @Nullable
    public RenderTarget renderTarget;

    // This is Minecraft's renderTarget (native res)
    @Nullable
    public RenderTarget clientRenderTarget;

    @Nullable
    private RenderTarget fsrIntermediateTarget;

    //? >= 1.21.11 {
    public static RenderPipeline FSR_EASU_PIPELINE =
            //? >=26.3 {
            RenderPipeline.builder().withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withColorTargetState(ColorTargetState.DEFAULT)
                .withShaderDefine("RENDERSCALE_EXPLICIT_GATHER")
            //?} else
            //RenderPipeline.builder(GLOBALS_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("renderscale", "pipeline/fsr_easu"))
                .withVertexShader("core/screenquad")
                .withFragmentShader(Identifier.fromNamespaceAndPath("renderscale", "core/easu"))
                //? <= 26.1 {
                    /*//? 1.21.11 {
                /^.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false)

                    ^///?}
                    .withSampler("InSampler")
                    .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
                    *///?} else {

                .withBindGroupLayout(BindGroupLayouts.IN_SAMPLER)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    //?}

                .build();

    public static RenderPipeline FSR_RCAS_PIPELINE =
            //? >=26.3 {
            RenderPipeline.builder().withBindGroupLayout(BindGroupLayouts.GLOBALS)
                .withColorTargetState(ColorTargetState.DEFAULT)
            //?} else
            //RenderPipeline.builder(GLOBALS_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("renderscale", "pipeline/fsr_rcas"))
                    .withVertexShader("core/screenquad")
                    .withFragmentShader(Identifier.fromNamespaceAndPath("renderscale", "core/rcas"))
                    //? <= 26.1 {
                    /*//? 1.21.11 {
                    /^.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)

                    ^///?}
                    .withSampler("InSampler")
                    .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
                    *///?} else {


                    .withBindGroupLayout(BindGroupLayouts.IN_SAMPLER)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    //?}
                    .build();
    //?}


    private static RenderScale instance;
    private boolean shouldScale = false;
    private final DynamicScaleController dynamicScale = new DynamicScaleController();
    private Object dynamicScaleLevel;
    private Object dynamicScaleCamera;
    private float previousCameraYaw;
    private float previousCameraPitch;
    public boolean hasRun = false;

    public static final ConfigHolder<RenderScaleConfig> CONFIG = RenderScaleConfig.init();

    public static final Platform PLATFORM = createPlatformInstance();

    static Platform xplat() {
        return PLATFORM;
    }

    private static Platform createPlatformInstance() {
        //? fabric {
        return new FabricPlatform();
        //?} neoforge {
        /*return new NeoforgePlatform();
         *///?} forge {
        /*return new ForgePlatform();
         *///?}
    }

    // Fabric
    public static void init() {
        instance = new RenderScale();
    }

    // NeoForge made it so that the mod loads before Minecraft (but not fabric...), so this is needed to get the "actual" Minecraft instance
    public static void init(Minecraft client) {
        RenderScale.client = client;
        if (instance == null) {
            instance = new RenderScale();
        }
    }

    public static RenderScale getInstance() {
        return instance;
    }

    public static RenderScaleConfig getConfig() {
        return CONFIG.getConfig();
    }

    public void onResolutionChanged() {
        if (getWindow() == null) return;

        dynamicScale.reset(getConfig().getScale());

        ProfilerFiller profiler = getProfile();
        profiler.push("renderscale_resize_targets");

        resizeRenderTarget();

        profiler.pop();
    }

    public void setClientRenderTarget(RenderTarget renderTarget) {
        //? < 26.2 {
        /*((MainRenderTargetSetter) client).renderScale$setMainRenderTarget(renderTarget);
        *///? } else
        ((MainRenderTargetSetter) client.gameRenderer).renderScale$setMainRenderTarget(renderTarget);
    }

    public ProfilerFiller getProfile() {
        //? > 1.21.1 {
        return Profiler.get();
        //? } else
        //return RenderScale.client.getProfiler();
    }

    public void setShouldScale(boolean shouldScale) {
        ProfilerFiller profiler = getProfile();
        profiler.push("renderscale_rescaling");
        //? >= 1.21.5 {
        this.shouldScale = shouldScale;

        Window window = client.getWindow();
        int width = window.getWidth();
        int height = window.getHeight();

        int scaledWidth = Math.clamp(width, 1, 65536);
        int scaledHeight = Math.clamp(height, 1, 65536);



        if (renderTarget == null) {
//            renderTarget = new TextureTarget("RenderScale", scaledWidth, scaledHeight, true);
            renderTarget = new MainTarget(scaledWidth, scaledHeight);
        }

        if (clientRenderTarget == null) {
            //? < 26.2 {
            /*clientRenderTarget = client.getMainRenderTarget();
            *///? } else
            clientRenderTarget = client.gameRenderer.mainRenderTarget();
        }

        if (shouldScale) {
            setClientRenderTarget(renderTarget);

//            RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(renderTarget.getDepthTexture(), 1.0);
//            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(renderTarget.getColorTexture(), 0);
        } else {
            try {
                setClientRenderTarget(clientRenderTarget);

//                RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(renderTarget.getColorTexture(), clientRenderTarget.getColorTexture(), 0, 0, 0, 0, 0, scaledWidth, scaledHeight);

                  //? >= 1.21.5 && < 1.21.11 {
                /*((GICommandEncoderThing) RenderSystem.getDevice().createCommandEncoder()).renderScale$copyAndResizeTexture(
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
                  //~ if 1.21.5 && fabric 'getColorTextureView' -> 'getColorTexture'
                  clientRenderTarget.blitAndBlendToTexture(renderTarget.getColorTextureView());
                  *///?} else
                blitAndBlendToTexture(renderTarget, clientRenderTarget, CONFIG.getConfig().getFilter() ? FilterMode.LINEAR : FilterMode.NEAREST);
//                blitAndBlendToTexture(renderTarget, clientRenderTarget, FilterMode.LINEAR);
//                //?}
//                renderTarget.blitAndBlendToTexture(clientRenderTarget.getColorTextureView());
//                clientRenderTarget.copyDepthFrom(renderTarget);
//                renderTarget.blitToScreen();
            } catch (Exception e) {
                Constants.LOG.error("Error copying texture", e);
            }
        }
        //?} else {
        /*if (this.shouldScale == shouldScale) return;

        Window window = client.getWindow();
        if (renderTarget == null) {
            this.shouldScale = true;
            renderTarget = new MainTarget(window.getWidth(), window.getHeight());
        }

        this.shouldScale = shouldScale;

        if (shouldScale) {
            clientRenderTarget = client.getMainRenderTarget();

            //? forge {
            /^// Forge mods may enable a combined depth/stencil buffer on the main
            // target. Preserve that format before Oculus attaches our world target.
            if (clientRenderTarget.isStencilEnabled() && !renderTarget.isStencilEnabled()) {
                renderTarget.enableStencil();
            }
            ^///?}

            setClientRenderTarget(renderTarget);
            //? <= 1.21.4 {
            /^renderTarget.bindWrite(true);
            ^///?}
        } else {
            //? forge {
            /^// A mod can also enable stencil while our target is the main target.
            if (renderTarget.isStencilEnabled() && !clientRenderTarget.isStencilEnabled()) {
                clientRenderTarget.enableStencil();
            }
            ^///?}
            setClientRenderTarget(clientRenderTarget);
            //? <= 1.21.4 {
            /^client.getMainRenderTarget().bindWrite(true);
            ^///?}

            //? <= 1.21.4 {
            /^//? <= 1.21.1 {
            /^¹// TODO: Forced to do this because of sodium + kubejs... does this affect macOS users?
            renderTarget.blitToScreen(window.getWidth(), window.getHeight(), false);
            ¹^///?} else {
            renderTarget.blitToScreen(window.getWidth(), window.getHeight());
            //?}
            ^///?} else {
            renderTarget.blitAndBlendToScreen(window.getWidth(), window.getHeight());
            //?}
        }
        *///?}
        profiler.pop();
    }

    // Takes into account shouldScale
    public double getCurrentScaleFactor() {
        return shouldScale ? getRenderScaleFactor() : 1;
    }

    public double getRenderScaleFactor() {
        return getConfig().isDynamicScaleEnabled()
                ? Math.max(getConfig().getMinimumScale(), Math.min(getConfig().getScale(), dynamicScale.getScale()))
                : getConfig().getScale();
    }

    public void updateDynamicScale() {
        RenderScaleConfig config = getConfig();
        if (dynamicScaleLevel != client.level) {
            dynamicScaleLevel = client.level;
            dynamicScale.reset(config.getScale());
            dynamicScaleCamera = null;
        }
        if (!config.isDynamicScaleEnabled()) {
            dynamicScale.reset(config.getScale());
            return;
        }
        //? >= 26.2 {
        boolean hasScreen = client.gui.screen() != null || client.gui.overlay() != null;
        //?} else
        //boolean hasScreen = client.screen != null || client.getOverlay() != null;
        if (client.level == null || hasScreen
                || client.isPaused() || !client.isWindowActive()) {
            dynamicScale.resetTiming();
            dynamicScaleCamera = null;
            return;
        }
        var camera = client.getCameraEntity();
        if (camera == null) {
            dynamicScale.resetTiming();
            dynamicScaleCamera = null;
            return;
        }
        float yaw = camera.getYRot();
        float pitch = camera.getXRot();
        float yawChange = (yaw - previousCameraYaw) % 360.0f;
        if (yawChange > 180) yawChange -= 360;
        if (yawChange < -180) yawChange += 360;
        boolean cameraMoving = camera == dynamicScaleCamera
                && (Math.abs(yawChange) > 0.01f || Math.abs(pitch - previousCameraPitch) > 0.01f);
        dynamicScaleCamera = camera;
        previousCameraYaw = yaw;
        previousCameraPitch = pitch;
        if (dynamicScale.update(System.nanoTime(), config.getTargetFrameRate(), config.aggressionLevel.strength,
                config.getMinimumScale(), config.getScale(), cameraMoving)) {
            resizeRenderTarget(false);
            // Exclude render-target allocation time from the next sample.
            dynamicScale.resetTiming();
        }
    }

    @Nullable
    private Window getWindow() {
        return client.getWindow();
    }

    public void resizeRenderTarget() {
        resizeRenderTarget(true);
    }

    private void resizeRenderTarget(boolean reloadResources) {
        resize(renderTarget);
        // The FSR intermediate target is output-sized and is resized by the blit pass.
        //? <= 1.21.1 {
        /*resize(client.levelRenderer.entityTarget());

        if (hasRun) {
            if (reloadResources) client.levelRenderer.onResourceManagerReload(client.getResourceManager());
            else client.levelRenderer.resize(client.getWindow().getWidth(), client.getWindow().getHeight());
        }
        *///?}
    }

    public void resizeMinecraftRenderTargetSize() {
//        resize(client.levelRenderer.entityOutlineTarget());
    }

    public int clamp(int number, int min, int max) {
        return Math.max(min, Math.min(number, max));
    }

    private void resize(@Nullable RenderTarget renderTarget) {
        if (renderTarget == null) return;

        boolean prev = shouldScale;
        shouldScale = true;

        Window window = client.getWindow();
        int width = window.getWidth();
        int height = window.getHeight();

        int scaledWidth = clamp(width, 1, 65536);
        int scaledHeight = clamp(height, 1, 65536);

        if (renderTarget.width == scaledWidth && renderTarget.height == scaledHeight) {
            shouldScale = prev;
            return;
        }

        //? >= 1.21.2 {
        renderTarget.resize(scaledWidth, scaledHeight);
        //?} else {
        /*renderTarget.resize(scaledWidth, scaledHeight, true);
        *///?}
//        //? >= 1.21.6 {
//        //?} else {
//        /*renderTarget.resize(scaledWidth, scaledHeight);
//        *///?}

        shouldScale = prev;
    }

    //? >= 1.21.11 {
    public void blitAndBlendToTexture(final RenderTarget input, final RenderTarget output, final FilterMode filter) {
        RenderSystem.assertOnRenderThread();

        //? < 1.21.11 {
        /*try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Blit render target", output.getColorTextureView(), OptionalInt.empty())) {
        *///? } else
        if (getConfig().fsr && input.width <= output.width && input.height <= output.height) {
            if (fsrIntermediateTarget == null) {
                // TODO: maybe use TextureTarget, we're wasting like 32MB of VRAM here
                fsrIntermediateTarget = new MainTarget(output.width, output.height);
//                    fsrIntermediateTarget = new TextureTarget("FSR: Intermediate", output.width, output.height);
            } else if (fsrIntermediateTarget.width != output.width || fsrIntermediateTarget.height != output.height) {
                fsrIntermediateTarget.resize(output.width, output.height);
            }

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "FSR: EASU", fsrIntermediateTarget.getColorTextureView(), /*? > 26.1 {*/ Optional /*?} else {*/ /*OptionalInt *//*?}*/.empty())) {
                setPipeline(renderPass, FSR_EASU_PIPELINE);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("InSampler", input.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(filter));
                //? < 26.2 {
                /*renderPass.draw(0, 3);
                 *///?} else
                renderPass.draw(3, 1, 0, 0);
            }

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "FSR: RCAS", output.getColorTextureView(), /*? > 26.1 {*/ Optional /*?} else {*/ /*OptionalInt *//*?}*/.empty())) {
                setPipeline(renderPass, FSR_RCAS_PIPELINE);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("InSampler", fsrIntermediateTarget.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(filter));
                //? < 26.2 {
                /*renderPass.draw(0, 3);
                 *///?} else
                renderPass.draw(3, 1, 0, 0);
            }
        } else {
            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Blit render target", output.getColorTextureView(), /*? > 26.1 {*/ Optional /*?} else {*/ /*OptionalInt *//*?}*/.empty())) {
                // Tracy blit is weird because I believe it's technically a debug pass.
                // However, it looks exactly the same as vanilla, so I'm assuming it's fine.
                setPipeline(renderPass, RenderPipelines.TRACY_BLIT);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("InSampler", input.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(filter));
                //? < 26.2 {
                /*renderPass.draw(0, 3);
                 *///?} else
                renderPass.draw(3, 1, 0, 0);
            }
        }

        // copying depth doesn't seem to do anything?
//        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Blit render target", output.getDepthTextureView(), OptionalInt.empty())) {
////            renderPass.setPipeline(RenderPipelines.FOG_SNIPPET);
//            RenderSystem.bindDefaultUniforms(renderPass);
//            renderPass.setUniform("InSampler2", input.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
//            renderPass.draw(0, 3);
//        }
    }

    private static void setPipeline(RenderPass renderPass, RenderPipeline pipeline) {
        //? >=26.3 {
        renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
        //?} else
        //renderPass.setPipeline(pipeline);
    }
    //?}
}
//~}
