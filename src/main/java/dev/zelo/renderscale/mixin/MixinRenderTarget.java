//? 1.20.1 {
/*package dev.zelo.renderscale.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

// TODO: Change priority
@Mixin(value = RenderTarget.class, priority = 99999)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinRenderTarget {
    @Redirect(method = "setFilterMode", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texParameter(III)V"))
    private void onSetTexFilter(int target, int pname, int param) {
        GlStateManager._texParameter(target, pname, RenderScale.getConfig().getFilter() ? GL11.GL_LINEAR : GL11.GL_NEAREST);
    }

//    @ModifyArgs(method = "blitToScreen(IIZ)V", at = @At("HEAD"))
//    private void doubleResolution(Args args) {
//        int width = args.get(0);
//        int height = args.get(1);
//        args.set(0, width * 2);
//        args.set(1, height * 2);
//    }

    // Sodium fix
    @ModifyVariable(method = "blitToScreen(IIZ)V", at = @At("HEAD"), index = 3)
    private boolean x(boolean y) {
        return false;
    }
}
*///?}