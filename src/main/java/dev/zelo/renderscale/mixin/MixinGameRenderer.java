package dev.zelo.renderscale.mixin;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;
//? >=26.3 {
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
//?}
//? 1.21.1
//import net.minecraft.client.Camera;
//? 1.21.1
//import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
//? 1.21.1
//import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinGameRenderer {
    //? >=26.3 {
    @Shadow @Final private GameRenderState gameRenderState;
    @Shadow @Final private RenderTarget hud3DTarget;
    @Unique private int renderScale$windowWidth;
    @Unique private int renderScale$windowHeight;
    @Unique private int renderScale$guiScale;
    //?}

    @Inject(method = "render", at = @At("HEAD"))
    private void renderScale$updateDynamicScale(CallbackInfo callbackInfo) {
        if (RenderScale.getInstance() != null) {
            RenderScale.getInstance().updateDynamicScale();
        }
    }

    //? <1.21.4 {
    /*@Inject(method = "render", at = @At("RETURN"))
    private void renderScale$autotestFrameRendered(CallbackInfo callbackInfo) {
        dev.zelo.renderscale.gametest.RenderScaleAutoTest.INSTANCE.frameRendered();
    }
    *///?}

    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void takeOver(CallbackInfo callbackInfo) {
        RenderScale.getInstance().setShouldScale(true);
        //? >=26.3 {
        // Window dimensions are extracted before rendering in 26.3. Scale the
        // world state too, and restore the exact native values before drawing UI.
        var windowState = gameRenderState.windowRenderState;
        renderScale$windowWidth = windowState.width;
        renderScale$windowHeight = windowState.height;
        renderScale$guiScale = windowState.guiScale;
        var target = RenderScale.getInstance().renderTarget;
        windowState.width = target.width;
        windowState.height = target.height;
        windowState.guiScale = (int) (renderScale$guiScale * RenderScale.getInstance().getCurrentScaleFactor());
        if (hud3DTarget.width != target.width || hud3DTarget.height != target.height) {
            hud3DTarget.resize(target.width, target.height);
        }
        //?}
    }

    //? 1.21.1 {
    /*/^*
     * neoforge... please...
     ^/
    @Inject(method = "renderItemInHand", at = @At("HEAD"))
    private void renderScale$restoreViewportBeforeHand(Camera camera, float tickDelta, Matrix4f matrix,
            CallbackInfo callbackInfo) {
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }
    *///?}

    @Inject(method = "renderLevel", at = @At(value = "RETURN"))
    private void handBack(CallbackInfo callbackInfo) {
        //? >=26.3 {
        var windowState = gameRenderState.windowRenderState;
        windowState.width = renderScale$windowWidth;
        windowState.height = renderScale$windowHeight;
        windowState.guiScale = renderScale$guiScale;
        //?}
        RenderScale.getInstance().setShouldScale(false);
        dev.zelo.renderscale.compat.skyboxify.SkyboxifyCompat.clearStaleSkySubmits();
    }
}
