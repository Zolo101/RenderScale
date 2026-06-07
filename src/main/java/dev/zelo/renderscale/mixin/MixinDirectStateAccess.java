//? >= 1.21.5 && < 1.21.11 {

/*package dev.zelo.renderscale.mixin;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DirectStateAccess.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public interface MixinDirectStateAccess {
    @Invoker("blitFrameBuffers")
    void invokeBlitFrameBuffers(
            int readFrameBuffer,
            int drawFrameBuffer,
            int srcX0,
            int srcY0,
            int srcX1,
            int srcY1,
            int destX0,
            int destY0,
            int destX1,
            int destY1,
            int mask,
            int filter
    );

//    @Invoker("bindFrameBufferTextures")
//    void invokeBindFrameBufferTextures(
//            int frameBuffer,
//            int colorTexture,
//            int depthTexture,
//            int level,
//            int target,
//            boolean useStencil
//    );
}
*///?}