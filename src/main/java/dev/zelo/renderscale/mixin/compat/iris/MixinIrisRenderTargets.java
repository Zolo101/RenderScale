// So I thought I already fixed this back in the 1.21.5 days but it looks like a new iris version has caused this to start again...
// TODO: Could have something to do with https://github.com/Zolo101/RenderScale/issues/96
//? iris && >=26.3 {
package dev.zelo.renderscale.mixin.compat.iris;

import com.mojang.renderpearl.api.textures.GpuTexture;
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
public abstract class MixinIrisRenderTargets {
    @Shadow private GpuTexture currentDepthTexture;
    @Shadow private int cachedDepthBufferVersion;

    @Inject(method = "resizeIfNeeded", at = @At("HEAD"))
    private void renderScale$invalidateReplacedDepthTarget(int version, GpuTexture depth, int width, int height,
            DepthBufferFormat format, PackDirectives directives, CallbackInfoReturnable<Boolean> ci) {
        // Iris builds a pipeline against the native target during shader reloads.
        // Its depth version is local to each target, so an equal version on our
        // world target must still reattach that target's depth texture.
        if (currentDepthTexture != depth && cachedDepthBufferVersion == version) {
            cachedDepthBufferVersion = version ^ 1;
        }
    }
}
//?}
