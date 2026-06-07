//? <= 1.21.1 {
/*package dev.zelo.renderscale.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;
import net.minecraft.client.renderer.PostChain;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PostChain.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinPostChain {
    @Shadow
    private Matrix4f shaderOrthoMatrix;
    @Shadow @Final private String name;

    @Unique
    private double renderScale$inverseScale() {
        return name.equals("minecraft:shaders/post/entity_outline.json") ? 1 / RenderScale.getConfig().getScale() : 1;
//        return 1 / RenderScale.getConfig().getScale();
    }

    @Redirect(method = "resize", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;resize(IIZ)V"))
    private void resize(RenderTarget instance, int width, int height, boolean clearError) {
        instance.resize((int) (width / renderScale$inverseScale()), (int) (height / renderScale$inverseScale()), clearError);
    }

    @Inject(method = "updateOrthoMatrix", at = @At("TAIL"))
    private void onUpdateOrthoMatrix(CallbackInfo ci) {
        this.shaderOrthoMatrix = this.shaderOrthoMatrix.scale((float) renderScale$inverseScale(), (float) renderScale$inverseScale(), 1.0F);
    }
}
*///?}