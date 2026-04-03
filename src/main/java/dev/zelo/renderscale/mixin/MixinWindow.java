package dev.zelo.renderscale.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Window.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinWindow {
    @ModifyReturnValue(method = "getWidth", at = @At("RETURN"))
    private int renderScale$scaleWidth(int original) {
        return renderScale$scale(original);
    }

    @ModifyReturnValue(method = "getHeight", at = @At("RETURN"))
    private int renderScale$scaleHeight(int original) {
        return renderScale$scale(original);
    }

    // TODO: Is this neoforge only?
    @ModifyReturnValue(method = "getGuiScale", at = @At("RETURN"))
    //? >= 1.21.6 {
    private int renderScale$modifyGuiScale(int original) {
    //?} else {
    /*private double renderScale$modifyGuiScale(double original) {
    *///?}
        // It's NeoForges' fault for this null check
        return RenderScale.getInstance() == null ? original : (int) (original * RenderScale.getInstance().getCurrentScaleFactor());
    }

    @Inject(method = "onFramebufferResize", at = @At("RETURN"))
    private void renderScale$onFramebufferResize(long window, int framebufferWidth, int framebufferHeight, CallbackInfo ci) {
        if (RenderScale.getInstance() != null) {
            RenderScale.getInstance().onResolutionChanged();
        }
    }

    @Inject(method = "refreshFramebufferSize", at = @At("RETURN"))
    private void renderScale$refreshFramebufferSize(CallbackInfo ci) {
        if (RenderScale.getInstance() != null) {
            RenderScale.getInstance().onResolutionChanged();
        }
    }

    @Unique
    private int renderScale$scale(int value) {
        if (RenderScale.getInstance() != null) {
            double scaleFactor = RenderScale.getInstance().getCurrentScaleFactor();
            return Math.max((int) (value * scaleFactor), 1);
        } else {
            return value;
        }
    }
}
