//? 1.20.1 {
/*package dev.zelo.renderscale.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// TODO: Change priority
@Mixin(value = RenderTarget.class, priority = 99999)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinRenderTarget {
    @Redirect(method = "setFilterMode", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texParameter(III)V"))
    private void onSetTexFilter(int target, int pname, int param) {
        GlStateManager._texParameter(target, pname, RenderScale.getConfig().getFilter() ? GL11.GL_LINEAR : GL11.GL_NEAREST);
    }

}
*///?}
