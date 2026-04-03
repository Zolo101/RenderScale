//package dev.zelo.renderscale.mixin;
//
//import com.mojang.blaze3d.pipeline.RenderTarget;
//import dev.kikugie.fletching_table.annotation.MixinEnvironment;
//import dev.zelo.renderscale.RenderScale;
//import net.minecraft.client.Minecraft;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//
//@Mixin(Minecraft.class)
//@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
//public abstract class MixinMinecraft {
//    @Redirect(method = "getMainRenderTarget", at = @At(value = "HEAD"))
//    private RenderTarget redirectGetMainRenderTarget(Minecraft instance) {
//        RenderTarget clientRenderTarget = RenderScale.getInstance().clientRenderTarget;
//        if (clientRenderTarget != null) {
//            return clientRenderTarget;
//        }
//        return instance.getMainRenderTarget();
//    }
//}
