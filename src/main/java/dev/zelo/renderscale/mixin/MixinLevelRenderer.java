package dev.zelo.renderscale.mixin;

//? >= 1.21.4 {
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.minecraft.client.renderer.LevelTargetBundle;
//?}

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinLevelRenderer {
    //? >= 1.21.4 {
    @Shadow private RenderTarget entityOutlineTarget;
    //?} else {
    /*@Shadow private RenderTarget entityTarget;
    *///?}

    //? >= 1.21.4 {
    @Shadow @Final private Minecraft minecraft;
    // Fix for the entity outline shader
    // method is fabric
    // lambda is neoforge
//    @Inject(method = {"method_62215", "lambda$addSkyPass$12"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
//    private static void onLoadEntityOutlineShader(CallbackInfo ci) {
//        RenderScale.getInstance().resizeMinecraftRenderTargetSize();
//    }
    // TODO: Might be unnecessary for 26.1
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;importExternal(Ljava/lang/String;Ljava/lang/Object;)Lcom/mojang/blaze3d/resource/ResourceHandle;"))
    private void onRenderWorldBegin(CallbackInfo callbackInfo) {
        if (this.entityOutlineTarget != null) {
            Minecraft instance = this.minecraft;

            double s = RenderScale.getConfig().getScale();

            entityOutlineTarget.width = (int) (instance.getWindow().getWidth() * s);
            entityOutlineTarget.height = (int) (instance.getWindow().getHeight() * s);
        }
    }
    //?}

    //? forge {
    /*@Inject(method = "initOutline", at = @At(value = "RETURN"))
    private void onLoadEntityOutlineShader(CallbackInfo ci) {
        RenderScale.getInstance().resizeMinecraftRenderTargetSize();
    }

    @Inject(method = "resize", at = @At("RETURN"))
    private void onOnResized(CallbackInfo ci) {
        if (entityTarget == null) return;
        RenderScale.getInstance().resizeMinecraftRenderTargetSize();
    }
    *///?}

    // 549> <564
//    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;execute(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder$Inspector;)V"))
//    private void handBack(CallbackInfo callbackInfo) {
//        RenderScale.getInstance().setShouldScale(false);
//    }
}
