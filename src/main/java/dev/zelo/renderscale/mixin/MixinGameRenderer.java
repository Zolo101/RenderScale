package dev.zelo.renderscale.mixin;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;
//? 1.21.1
//import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinGameRenderer {
    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void takeOver(CallbackInfo callbackInfo) {
        RenderScale.getInstance().setShouldScale(true);
    }

    //? 1.21.1 {
    /*/^*
     * neoforge... please...
     ^/
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/Camera;FLorg/joml/Matrix4f;)V"))
    private void renderScale$restoreViewportBeforeHand(CallbackInfo callbackInfo) {
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }
    *///?}

    @Inject(method = "renderLevel", at = @At(value = "RETURN"))
    private void handBack(CallbackInfo callbackInfo) {
        RenderScale.getInstance().setShouldScale(false);
    }
}
