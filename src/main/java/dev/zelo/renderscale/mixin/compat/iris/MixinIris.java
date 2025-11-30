package dev.zelo.renderscale.mixin.compat.iris;

import dev.zelo.renderscale.RenderScale;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Iris.class)
public abstract class MixinIris {
    @Inject(method = "reload", at = @At("TAIL"), remap = false)
    private static void reload(CallbackInfo ci) {
        RenderScale.getInstance().resizeRenderTarget();
    }
}
