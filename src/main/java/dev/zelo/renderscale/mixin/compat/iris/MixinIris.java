//? iris {
package dev.zelo.renderscale.mixin.compat.iris;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;

import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Iris.class)
@MixinEnvironment(type = MixinEnvironment.Env.MAIN) // should this be MAIN?
public abstract class MixinIris {
    @Inject(method = "reload", at = @At("TAIL"))
    private static void reload(CallbackInfo ci) {
        RenderScale.getInstance().onResolutionChanged();
    }
}
//?}