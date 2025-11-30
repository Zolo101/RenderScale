package dev.zelo.renderscale.mixin;

import dev.zelo.renderscale.RenderScale;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void onRenderWorldBegin(CallbackInfo callbackInfo) {
        RenderScale.getInstance().setShouldScale(true);
    }

    @Inject(method = "renderLevel", at = @At(value = "TAIL"))
    private void onRenderWorldEnd(CallbackInfo callbackInfo) {
        RenderScale.getInstance().setShouldScale(false);
    }
}
