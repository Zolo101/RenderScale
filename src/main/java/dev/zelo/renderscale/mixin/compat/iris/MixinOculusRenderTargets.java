//? 1.20.1 && forge {
/*package dev.zelo.renderscale.mixin.compat.iris;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.targets.RenderTargets", remap = false)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinOculusRenderTargets {
    @Shadow private int currentDepthTexture;
    @Shadow private int cachedDepthBufferVersion;

    @Inject(method = "resizeIfNeeded", at = @At("HEAD"))
    private void renderScale$invalidateReplacedDepthTarget(int version, int depth, int width, int height,
            DepthBufferFormat format, PackDirectives directives, CallbackInfoReturnable<Boolean> ci) {
        // Oculus checks only a per-RenderTarget counter. Different targets can
        // have equal counters, so replacing the target must invalidate it too.
        // Let the original method reattach depth and store the incoming version.
        if (currentDepthTexture != depth && cachedDepthBufferVersion == version) {
            cachedDepthBufferVersion = version ^ 1;
        }
    }
}
*///?}
