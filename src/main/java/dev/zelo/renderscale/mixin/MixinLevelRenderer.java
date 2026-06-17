package dev.zelo.renderscale.mixin;

//? < 26.1 {

/*import com.mojang.blaze3d.pipeline.RenderTarget;
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
    //? >= 1.21.2 {
    @Shadow private RenderTarget entityOutlineTarget;
    //?} else {
    /^@Shadow private RenderTarget entityTarget;
    ^///?}

    //? >= 1.21.2 {
    @Shadow @Final private Minecraft minecraft;
    // Fix for the entity outline shader
    // method is fabric
    // lambda is neoforge
//    @Inject(method = {"method_62215", "lambda$addSkyPass$12"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
//    private static void onLoadEntityOutlineShader(CallbackInfo ci) {
//        RenderScale.getInstance().resizeMinecraftRenderTargetSize();
//    }
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;importExternal(Ljava/lang/String;Ljava/lang/Object;)Lcom/mojang/blaze3d/resource/ResourceHandle;"))
    private void onRenderWorldBegin(CallbackInfo callbackInfo) {
        if (this.entityOutlineTarget != null) {
            RenderTarget mainTarget = this.minecraft.getMainRenderTarget();

            if (this.entityOutlineTarget.width != mainTarget.width || this.entityOutlineTarget.height != mainTarget.height) {
                this.entityOutlineTarget.resize(mainTarget.width, mainTarget.height);
            }
        }
    }
    //?}

    //? forge {
    /^@Inject(method = "initOutline", at = @At(value = "RETURN"))
    private void onLoadEntityOutlineShader(CallbackInfo ci) {
        RenderScale.getInstance().resizeMinecraftRenderTargetSize();
    }

    @Inject(method = "resize", at = @At("RETURN"))
    private void onOnResized(CallbackInfo ci) {
        if (entityTarget == null) return;
        RenderScale.getInstance().resizeMinecraftRenderTargetSize();
    }
    ^///?}
}
*///?}
