package dev.zelo.renderscale.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import dev.zelo.renderscale.CommonClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Window.class)
public abstract class MixinWindow {
    @ModifyReturnValue(method = "getWidth", at = @At("RETURN"))
    private int renderScale$scaleWidth(int original) {
        return renderScale$scale(original);
    }

    @ModifyReturnValue(method = "getHeight", at = @At("RETURN"))
    private int renderScale$scaleHeight(int original) {
        return renderScale$scale(original);
    }

    @ModifyReturnValue(method = "getGuiScale", at = @At("RETURN"))
    private int renderScale$modifyGuiScale(int original) {
        // It's NeoForges' fault for this null check
        return CommonClass.getInstance() == null ? original : (int) (original * CommonClass.getInstance().getCurrentScaleFactor());
    }

    @Inject(method = "onFramebufferResize", at = @At("RETURN"))
    private void d(long window, int framebufferWidth, int framebufferHeight, CallbackInfo ci) {
        if (CommonClass.getInstance() != null) {
            CommonClass.getInstance().onResolutionChanged();
        }
    }

    @Inject(method = "refreshFramebufferSize", at = @At("RETURN"))
    private void e(CallbackInfo ci) {
        if (CommonClass.getInstance() != null) {
            CommonClass.getInstance().onResolutionChanged();
        }
    }

    @Unique
    private int renderScale$scale(int value) {
        if (CommonClass.getInstance() != null) {
            double scaleFactor = CommonClass.getInstance().getCurrentScaleFactor();
            return Math.max((int) (value * scaleFactor), 1);
        } else {
            return value;
        }
    }
}
