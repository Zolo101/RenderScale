package dev.zelo.renderscale.mixin;

import dev.zelo.renderscale.CommonClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Unique
    private boolean waitingForResolutionChange = false;

    @Unique
    private int waitFrameCounter = 0;

    @Inject(at = @At("HEAD"), method = "renderLevel")
    // TODO: Do we need this?
    private void onRenderWorldBegin(CallbackInfo callbackInfo) {
        if (!CommonClass.getInstance().hasRun) {
            CommonClass.getInstance().hasRun = true;
            waitingForResolutionChange = true;
        }

        if (waitingForResolutionChange) {
            waitFrameCounter++;
            if (waitFrameCounter >= 5) {
                waitingForResolutionChange = false;
                // Synthetic full-pipeline resize: covers macOS Retina startup case
                // where Minecraft sized its mainRenderTarget against a stale 0x0 window
                // (because MixinWindow ran before CommonClass.init).
                Minecraft.getInstance().resizeDisplay();
                CommonClass.getInstance().onResolutionChanged();
            }
        }

        CommonClass.getInstance().setShouldScale(true);
    }

    @Inject(at = @At("RETURN"), method = "renderLevel")
    private void onRenderWorldEnd(CallbackInfo callbackInfo) {
        CommonClass.getInstance().setShouldScale(false);
    }
}
