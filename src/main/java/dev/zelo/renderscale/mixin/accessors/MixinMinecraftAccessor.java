//? < 26.2 {
/*package dev.zelo.renderscale.mixin.accessors;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.accessors.MainRenderTargetSetter;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinMinecraftAccessor implements MainRenderTargetSetter {
    @Mutable
    @Shadow
    @Final
    private RenderTarget mainRenderTarget;

    @Override
    public void renderScale$setMainRenderTarget(RenderTarget renderTarget) {
        this.mainRenderTarget = renderTarget;
    }
}
*///?}
