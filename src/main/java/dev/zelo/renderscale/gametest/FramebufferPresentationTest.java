//? 1.20.1 && forge {
/*package dev.zelo.renderscale.gametest;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.zelo.renderscale.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

// F2 reads a texture, so screenshot-only tests cannot detect stale window pixels.
final class FramebufferPresentationTest {
    private FramebufferPresentationTest() {}

    static void verify() {
        Minecraft client = Minecraft.getInstance();
        TextureTarget source = new TextureTarget(32, 32, false, Minecraft.ON_OSX);
        TextureTarget destination = new TextureTarget(32, 32, false, Minecraft.ON_OSX);
        try {
            RenderSystem.disableScissor();
            RenderSystem.colorMask(true, true, true, true);
            for (float alpha : new float[]{0.0f, 0.25f, 0.5f, 1.0f}) {
                source.setClearColor(0.125f, 0.5f, 0.875f, alpha);
                source.clear(Minecraft.ON_OSX);
                int expected;
                try (NativeImage screenshot = Screenshot.takeScreenshot(source)) {
                    expected = screenshot.getPixelRGBA(16, 16);
                }

                // Prime vanilla's cached shader blend mode, then simulate a GUI
                // leaving ordinary alpha blending enabled before presentation.
                destination.bindWrite(true);
                source.blitToScreen(32, 32, false);
                GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
                GL11C.glDrawBuffer(GL11C.GL_BACK);
                GL11C.glReadBuffer(GL11C.GL_BACK);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.clearColor(1, 0, 1, 1);
                RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                // This is the overload Minecraft uses to present its main target.
                source.blitToScreen(client.getWindow().getWidth(), client.getWindow().getHeight());
                requirePixel(expected, "Window retained old pixels at source alpha=" + alpha);
            }

            // Explicit false is still used by RenderScale's world-to-UI copy.
            // Preserve that caller's requested blending behavior.
            source.setClearColor(0.125f, 0.5f, 0.875f, 0);
            source.clear(Minecraft.ON_OSX);
            destination.setClearColor(1, 0, 1, 1);
            destination.clear(Minecraft.ON_OSX);
            destination.bindWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            source.blitToScreen(32, 32, false);
            requirePixel(0xFFFF00FF, "Explicit blended copy no longer preserves the destination");
            if (GL11C.glGetError() != GL11C.GL_NO_ERROR) throw new AssertionError("OpenGL error during presentation test");
            Constants.LOG.info("Framebuffer presentation passed: 4 source alpha values, F2/window RGB agreement, explicit blended copy");
        } finally {
            source.destroyBuffers();
            destination.destroyBuffers();
            RenderSystem.disableBlend();
            client.getMainRenderTarget().bindWrite(true);
        }
    }

    private static void requirePixel(int expected, String message) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer pixel = stack.malloc(4);
            GL11C.glReadPixels(16, 16, 1, 1, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, pixel);
            for (int channel = 0; channel < 3; channel++) {
                int actual = Byte.toUnsignedInt(pixel.get(channel));
                int wanted = (expected >>> (channel * 8)) & 255;
                if (Math.abs(actual - wanted) > 2) {
                    throw new AssertionError(message + ": RGB channel " + channel + " expected=" + wanted + " actual=" + actual);
                }
            }
        }
    }
}
*///?}
