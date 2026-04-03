package dev.zelo.renderscale.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinGameRenderer {
    // does not scale
    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Lightmap;render(Lnet/minecraft/client/renderer/state/LightmapRenderState;)V"))
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V"))
    // nothing
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isGameLoadFinished()Z"))
//@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;shouldCreateWorldFog()Z"))
//@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFog(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
//@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V"))

    // works -- black skydrop
//@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V"))

// nothing, hand didn't show
//    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V"))

// only hand works
//    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Projection;setupPerspective(FFFFF)V"))
//    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Projection;setupPerspective(FFFFF)V"))
//    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V"))
    private void takeOver(CallbackInfo callbackInfo) {
        RenderScale.getInstance().setShouldScale(true);
    }

//    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V"))
//    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V"))
    // works when in inventory
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;endFrame()V"))
    // does NOT work when in inventory
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))

    // scales everything
//    @Inject(method = "render", at = @At(value = "RETURN"))
    @Inject(method = "renderLevel", at = @At(value = "RETURN"))

//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;tryTakeScreenshotIfNeeded()V"))

    // downscales the whole game
//    @Inject(method = "render", at = @At(value = "RETURN"))

//        @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V"))
    // <756, >740
//    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;shouldCreateWorldFog()Z"))
    private void handBack(CallbackInfo callbackInfo) {
        RenderScale.getInstance().setShouldScale(false);
    }
}
