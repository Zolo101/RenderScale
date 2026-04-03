//? !forge {
package dev.zelo.renderscale.mixin.compat.iris;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? <= 1.20.1 {
/*import net.coderbot.iris.apiimpl.IrisApiV0ConfigImpl;

@Mixin(IrisApiV0ConfigImpl.class)
@MixinEnvironment(type = MixinEnvironment.Env.MAIN) // should this be MAIN?
public abstract class MixinIris {
    @Inject(method = "setShadersEnabledAndApply", at = @At("TAIL"), remap = false)
    private static void setShadersEnabledAndApply(CallbackInfo ci) {
        RenderScale.getInstance().resizeMinecraftRenderTargetSize();
    }
}
*///?} else {
import net.irisshaders.iris.Iris;

@Mixin(Iris.class)
@MixinEnvironment(type = MixinEnvironment.Env.MAIN) // should this be MAIN?
public abstract class MixinIris {
    @Inject(method = "reload", at = @At("TAIL"), remap = false)
    private static void reload(CallbackInfo ci) {
        RenderScale.getInstance().resizeRenderTarget();
    }
}
 //?}


//?}