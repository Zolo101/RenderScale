//? iris || (1.20.1 && forge) {
package dev.zelo.renderscale.mixin.compat.iris;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.Iris", remap = false)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinIris {
    @Inject(method = "reload", at = @At("TAIL"))
    private static void reload(CallbackInfo ci) {
        RenderScale.getInstance().onResolutionChanged();
    }
}
//?}
