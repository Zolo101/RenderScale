//? >= 1.21.5 && < 1.21.11 {

/*package dev.zelo.renderscale.mixin;

import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.accessors.GICommandEncoderThing;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GlCommandEncoder.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinGlCommandEncoder implements GICommandEncoderThing {
    @Shadow
    private boolean inRenderPass;
    @Shadow
    @Final
    private int readFbo;

    @Shadow
    @Final
    private int drawFbo;

    @Shadow
    @Final
    private GlDevice device;

    @Unique
    public void renderScale$copyAndResizeTexture(GpuTexture source, GpuTexture destination,
                                                 int mipLevel, int destX, int destY,
                                                 int sourceX, int sourceY,
                                                 int sourceWidth, int sourceHeight,
                                                 int destWidth, int destHeight, boolean isDepth) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        } else if (mipLevel >= 0 && mipLevel < source.getMipLevels() && mipLevel < destination.getMipLevels()) {
            if (destX + destWidth > destination.getWidth(mipLevel) || destY + destHeight > destination.getHeight(mipLevel)) {
                throw new IllegalArgumentException("Destination rectangle exceeds texture bounds");
            } else if (sourceX + sourceWidth > source.getWidth(mipLevel) || sourceY + sourceHeight > source.getHeight(mipLevel)) {
                throw new IllegalArgumentException("Source rectangle exceeds texture bounds");
            } else if (source.isClosed() || destination.isClosed()) {
                throw new IllegalStateException("Source or destination texture is closed");
            } else {
                int sourceId = ((GlTexture) source).glId();
                int destId = ((GlTexture) destination).glId();

                MixinDirectStateAccess dsa = ((MixinDirectStateAccess) device.directStateAccess());

                //  Bind and attach the source textures
                GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, this.readFbo);
                GlStateManager._glFramebufferTexture2D(GL30C.GL_READ_FRAMEBUFFER, isDepth ? GL30C.GL_DEPTH_ATTACHMENT : GL30C.GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D, sourceId, 0);

                // Bind and attach the destination texture
                GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, this.drawFbo);
                GlStateManager._glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, isDepth ? GL30C.GL_DEPTH_ATTACHMENT : GL30C.GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D, destId, 0);

                // Resize
                int mask = isDepth ? GL11C.GL_DEPTH_BUFFER_BIT : GL11C.GL_COLOR_BUFFER_BIT;
                int filter = RenderScale.getConfig().getFilter() ? GL11C.GL_LINEAR : GL11C.GL_NEAREST;

                // Force filter as nearest if this is a depth texture
                if (isDepth) filter = GL11C.GL_NEAREST;

                // Using Minecraft's DSA stops the weird UI artifacts
                // (Ultimately, it would be best if they brought back the actual function, so I don't need to use direct methods)
//                dsa.invokeBindFrameBufferTextures(this.readFbo, sourceId, destId, 0, GL30C.GL_READ_FRAMEBUFFER, isDepth);
                dsa.invokeBlitFrameBuffers(this.readFbo, this.drawFbo, sourceX, sourceY, sourceX + sourceWidth, sourceY + sourceHeight,
                        destX, destY, destX + destWidth, destY + destHeight,
                        mask, filter);
            }
        }
    }
}
*///?}
