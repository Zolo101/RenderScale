//? 1.20.1 || 1.21.1 {
/*package dev.zelo.renderscale.gametest;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.zelo.renderscale.Constants;
import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import dev.zelo.renderscale.render.LegacyFsrRenderer;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL33C.*;

/^** Exercises the actual GPU passes, including alpha-zero input and odd sizes. ^/
final class LegacyFsrTest {
    static void verify() {
        RenderScaleConfig config = new RenderScaleConfig();
        config.forceBilinear = true;
        config.downscaleFilter = RenderScaleConfig.DownscaleFilter.BILINEAR;
        TextureTarget input = new TextureTarget(17, 13, false, Minecraft.ON_OSX);
        TextureTarget output = new TextureTarget(35, 27, false, Minecraft.ON_OSX);
        try (LegacyFsrRenderer renderer = new LegacyFsrRenderer()) {
            RenderSystem.disableBlend();
            RenderSystem.disableScissor();
            RenderSystem.colorMask(true, true, true, true);
            for (int[] size : new int[][]{{17, 13, 35, 27}, {35, 27, 35, 27}, {65, 49, 33, 25}}) {
                input.resize(size[0], size[1], Minecraft.ON_OSX);
                output.resize(size[2], size[3], Minecraft.ON_OSX);
                fill(input);
                config.fsr = false;
                config.sharpeningMode = RenderScaleConfig.SharpeningMode.OFF;
                require(!renderer.blit(input, output, config), "Off must skip every pass");
                byte[] baseline = blit(input, output);
                Constants.LOG.info("Legacy FSR test {}x{} -> {}x{}, input green={}/{}, baseline green={}/{}",
                        input.width, input.height, output.width, output.height,
                        Byte.toUnsignedInt(pixels(input)[1]), Byte.toUnsignedInt(pixels(input)[((input.height - 1) * input.width) * 4 + 1]),
                        Byte.toUnsignedInt(baseline[1]), Byte.toUnsignedInt(baseline[((output.height - 1) * output.width) * 4 + 1]));

                config.sharpeningMode = RenderScaleConfig.SharpeningMode.RCAS;
                config.sharpeningStrength = 0;
                require(!renderer.blit(input, output, config), "0% must skip every pass");
                require(Arrays.equals(baseline, pixels(output)), "0% changed the destination");

                for (var mode : new RenderScaleConfig.SharpeningMode[]{RenderScaleConfig.SharpeningMode.RCAS,
                        RenderScaleConfig.SharpeningMode.RCAS_DENOISE}) {
                    config.sharpeningMode = mode;
                    config.sharpeningStrength = 100;
                    require(renderer.blit(input, output, config), "Sharpening failed: " + mode);
                    byte[] strong = pixels(output);
                    verifyImage(strong, output.width, output.height);
                    require(distance(baseline, strong) > 0, "Sharpening did not change the image");
                    config.sharpeningStrength = 35;
                    require(renderer.blit(input, output, config), "35% sharpening failed");
                    byte[] weak = pixels(output);
                    require(!Arrays.equals(weak, strong), "Sharpening strength has no effect");
                    require(distance(baseline, weak) < distance(baseline, strong), "35% should sharpen less than 100%");
                }

                config.sharpeningMode = RenderScaleConfig.SharpeningMode.OFF;
                config.fsr = true;
                if (input.width > output.width) {
                    require(!renderer.blit(input, output, config), "FSR must skip downsampling");
                } else {
                    // Simulate state left behind by world/shader rendering. The
                    // shader must still cover the output and leave this state intact.
                    glEnable(GL_BLEND);
                    glEnable(GL_SCISSOR_TEST);
                    glScissor(0, 0, 1, 1);
                    glColorMask(false, true, false, false);
                    glActiveTexture(GL_TEXTURE1);
                    glViewport(2, 3, 11, 9);
                    int framebuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
                    require(renderer.blit(input, output, config), "FSR failed");
                    require(glGetInteger(GL_ACTIVE_TEXTURE) == GL_TEXTURE1, "Active texture leaked");
                    require(glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING) == framebuffer, "Framebuffer binding leaked");
                    require(glIsEnabled(GL_BLEND) && glIsEnabled(GL_SCISSOR_TEST), "Enable state leaked");
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        var viewport = stack.mallocInt(4);
                        glGetIntegerv(GL_VIEWPORT, viewport);
                        require(viewport.get(0) == 2 && viewport.get(1) == 3 && viewport.get(2) == 11 && viewport.get(3) == 9,
                                "Viewport leaked");
                        var mask = stack.malloc(4);
                        glGetBooleanv(GL_COLOR_WRITEMASK, mask);
                        require(mask.get(0) == 0 && mask.get(1) != 0 && mask.get(2) == 0 && mask.get(3) == 0,
                                "Color mask leaked");
                    }
                    glDisable(GL_BLEND);
                    glDisable(GL_SCISSOR_TEST);
                    glColorMask(true, true, true, true);
                    glActiveTexture(GL_TEXTURE0);
                    byte[] fsr = pixels(output);
                    verifyImage(fsr, output.width, output.height);
                    require(distance(baseline, fsr) > 0, "FSR did not change the image");
                }
                require(glGetError() == GL_NO_ERROR, "OpenGL error during FSR/sharpening test");
            }
            verifyGridFiltering(renderer, input, output, config);
            // Resource reloads must free the old targets/programs and allow a fresh render.
            renderer.close();
            input.resize(51, 39, Minecraft.ON_OSX);
            output.resize(17, 13, Minecraft.ON_OSX);
            fill(input);
            config.fsr = false;
            config.downscaleFilter = RenderScaleConfig.DownscaleFilter.AUTO;
            config.sharpeningMode = RenderScaleConfig.SharpeningMode.OFF;
            require(renderer.blit(input, output, config), "Grid renderer did not recover after resource reload");
            verifyImage(pixels(output), output.width, output.height);
            config.sharpeningMode = RenderScaleConfig.SharpeningMode.RCAS;
            require(renderer.blit(input, output, config), "Renderer did not recover after resource reload");
            require(glGetError() == GL_NO_ERROR, "OpenGL error after resource reload");
            verifyPresentation();
            verifyStandardFiltering();
            Constants.LOG.info("Legacy FSR/grid tests PASSED: upscale/native/downscale, RGSS/SGSS/Auto, RCAS modes and strength, zero/off bypass, GL state, resize, reload and world/UI target swap");
        } finally {
            input.destroyBuffers();
            output.destroyBuffers();
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        }
    }

    private static void verifyGridFiltering(LegacyFsrRenderer renderer, TextureTarget input, TextureTarget output,
                                            RenderScaleConfig config) {
        for (int[] size : new int[][]{{34, 26, 17, 13}, {51, 39, 17, 13}, {68, 52, 17, 13},
                {65, 49, 33, 25}, {51, 26, 17, 13}, {17, 13, 35, 27}, {35, 27, 35, 27}, {51, 13, 17, 13}}) {
            input.resize(size[0], size[1], Minecraft.ON_OSX);
            output.resize(size[2], size[3], Minecraft.ON_OSX);
            fill(input);
            config.fsr = false;
            config.sharpeningMode = RenderScaleConfig.SharpeningMode.OFF;
            config.downscaleFilter = RenderScaleConfig.DownscaleFilter.BILINEAR;
            byte[] baseline = blit(input, output);
            require(!renderer.blit(input, output, config), "Bilinear should use the standard blit");
            double ratio = Math.min((double) input.width / output.width, (double) input.height / output.height);
            byte[] automatic = null;
            byte[] rotated = null;
            for (var filter : new RenderScaleConfig.DownscaleFilter[]{RenderScaleConfig.DownscaleFilter.RGSS,
                    RenderScaleConfig.DownscaleFilter.SGSS, RenderScaleConfig.DownscaleFilter.AUTO}) {
                config.downscaleFilter = filter;
                require(renderer.blit(input, output, config) == (ratio > 1.0), "Grid filter used outside downsampling: " + filter);
                if (ratio <= 1.0) {
                    require(Arrays.equals(baseline, pixels(output)), "Bypassed grid filter changed the destination");
                    continue;
                }
                byte[] resolved = pixels(output);
                verifyImage(resolved, output.width, output.height);
                require(distance(baseline, resolved) > 0, "Grid filter is identical to bilinear: " + filter);
                if (filter == RenderScaleConfig.DownscaleFilter.RGSS) rotated = resolved;
                if (filter == RenderScaleConfig.DownscaleFilter.SGSS) {
                    require(distance(rotated, resolved) > 0, "Sparse Grid is identical to Rotated Grid at ratio " + ratio);
                }
                if (filter == (ratio >= 3.0 ? RenderScaleConfig.DownscaleFilter.SGSS : RenderScaleConfig.DownscaleFilter.RGSS)) {
                    automatic = resolved;
                }
                if (filter == RenderScaleConfig.DownscaleFilter.AUTO) {
                    require(Arrays.equals(automatic, resolved), "Auto chose the wrong grid for ratio " + ratio);
                    config.fsr = true;
                    require(renderer.blit(input, output, config), "FSR toggle bypassed grid downsampling");
                    require(Arrays.equals(resolved, pixels(output)), "FSR altered grid downsampling");
                    config.fsr = false;
                }
                for (var mode : new RenderScaleConfig.SharpeningMode[]{RenderScaleConfig.SharpeningMode.RCAS,
                        RenderScaleConfig.SharpeningMode.RCAS_DENOISE}) {
                    config.sharpeningMode = mode;
                    config.sharpeningStrength = 0;
                    require(renderer.blit(input, output, config), "0% sharpening bypassed grid downsampling");
                    require(Arrays.equals(resolved, pixels(output)), "0% sharpening changed the grid resolve");
                    config.sharpeningStrength = 100;
                    require(renderer.blit(input, output, config), "Grid sharpening failed");
                    byte[] strong = pixels(output);
                    verifyImage(strong, output.width, output.height);
                    require(distance(resolved, strong) > 0, "Grid sharpening did not change the image");
                    config.sharpeningStrength = 35;
                    require(renderer.blit(input, output, config), "35% grid sharpening failed");
                    require(distance(resolved, pixels(output)) < distance(resolved, strong), "Grid sharpening strength has no effect");
                }
                config.sharpeningMode = RenderScaleConfig.SharpeningMode.OFF;
            }
            require(glGetError() == GL_NO_ERROR, "OpenGL error during grid downsampling");
        }
        Constants.LOG.info("Grid filtering PASSED: RGSS/SGSS overrides, Auto at 2x/3x/4x and odd/asymmetric ratios, native/upscale bypass, FSR toggle and sharpening");
    }

    private static void verifyPresentation() {
        Minecraft client = Minecraft.getInstance();
        RenderScale scale = RenderScale.getInstance();
        RenderScaleConfig config = RenderScale.getConfig();
        var nativeTarget = client.getMainRenderTarget();
        int nativeWidth = client.getWindow().getWidth();
        int nativeHeight = client.getWindow().getHeight();
        float oldScale = config.scale;
        boolean oldFsr = config.fsr;
        int oldTarget = config.targetFrameRate;
        var oldFilter = config.downscaleFilter;
        try {
            config.fsr = true;
            config.targetFrameRate = 0;
            config.downscaleFilter = RenderScaleConfig.DownscaleFilter.AUTO;
            for (float factor : new float[]{0.5f, 2.0f, 3.0f}) {
                config.scale = factor;
                scale.onResolutionChanged();
                scale.setShouldScale(true);
                require(client.getMainRenderTarget() == scale.renderTarget, "World target was not installed");
                require(scale.renderTarget.width == (int) (nativeWidth * factor) && scale.renderTarget.height == (int) (nativeHeight * factor),
                        "World target was not scaled");
                RenderSystem.clearColor(0.2f, 0.4f, 0.6f, 0.0f);
                RenderSystem.clear(GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
                scale.setShouldScale(false);
                require(!scale.legacyFsr.isFailed(), "World FSR/grid presentation failed");
                require(client.getMainRenderTarget() == nativeTarget, "Native target was not restored for the HUD");
                require(client.getWindow().getWidth() == nativeWidth && client.getWindow().getHeight() == nativeHeight,
                        "HUD dimensions were not restored");
                String mode = factor < 1.0f ? "FSR1" : factor < 3.0f ? "RGSS" : "SGSS";
                require(scale.getScalingMode().contains(mode), "Debug overlay does not report " + mode);
                int previous = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
                try (PixelStore ignored = new PixelStore(true); MemoryStack stack = MemoryStack.stackPush()) {
                    ByteBuffer pixel = stack.malloc(4);
                    glBindFramebuffer(GL_READ_FRAMEBUFFER, nativeTarget.frameBufferId);
                    glReadPixels(nativeWidth / 2, nativeHeight / 2, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixel);
                    for (int i = 0; i < 3; i++) {
                        require(Math.abs(Byte.toUnsignedInt(pixel.get(i)) - (i + 1) * 51) <= 2,
                                "Scaled world was not copied to the native HUD target");
                    }
                } finally {
                    glBindFramebuffer(GL_READ_FRAMEBUFFER, previous);
                }
            }
        } finally {
            scale.setShouldScale(false);
            config.scale = oldScale;
            config.fsr = oldFsr;
            config.targetFrameRate = oldTarget;
            config.downscaleFilter = oldFilter;
            scale.onResolutionChanged();
            nativeTarget.bindWrite(true);
        }
    }

    private static void verifyStandardFiltering() {
        Minecraft client = Minecraft.getInstance();
        RenderScale scale = RenderScale.getInstance();
        RenderScaleConfig config = RenderScale.getConfig();
        var nativeTarget = client.getMainRenderTarget();
        float oldScale = config.scale;
        boolean oldFsr = config.fsr;
        boolean oldBilinear = config.forceBilinear;
        int oldTarget = config.targetFrameRate;
        var oldSharpening = config.sharpeningMode;
        var oldFilter = config.downscaleFilter;
        try {
            config.targetFrameRate = 0;
            config.sharpeningMode = RenderScaleConfig.SharpeningMode.OFF;
            // Cover FSR's downsampling fallback, target recreation, and filter
            // changes without a resize. Each 2x2 checker must average to grey at 200%.
            for (int test = 0; test < 7; test++) {
                config.scale = test < 2 || test == 5 ? 2.0f : 0.5f;
                config.fsr = test == 1;
                config.forceBilinear = test == 3;
                config.downscaleFilter = config.scale > 1 || test == 6
                        ? RenderScaleConfig.DownscaleFilter.BILINEAR : RenderScaleConfig.DownscaleFilter.AUTO;
                scale.onResolutionChanged();
                scale.setShouldScale(true);
                require(scale.renderTarget.width == (int) (nativeTarget.width * config.scale)
                                && scale.renderTarget.height == (int) (nativeTarget.height * config.scale),
                        "World target dimensions do not match the requested scale");
                int binding = glGetInteger(GL_TEXTURE_BINDING_2D);
                try (PixelStore ignored = new PixelStore(false); MemoryStack stack = MemoryStack.stackPush()) {
                    ByteBuffer checker = stack.malloc(16);
                    for (int value : new int[]{0, 255, 255, 0}) {
                        checker.put((byte) value).put((byte) value).put((byte) value).put((byte) 255);
                    }
                    checker.flip();
                    glBindTexture(GL_TEXTURE_2D, scale.renderTarget.getColorTextureId());
                    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 2, 2, GL_RGBA, GL_UNSIGNED_BYTE, checker);
                } finally {
                    glBindTexture(GL_TEXTURE_2D, binding);
                }
                RenderSystem.disableBlend();
                RenderSystem.disableScissor();
                scale.setShouldScale(false);
                require(client.getMainRenderTarget() == nativeTarget, "Filtering changed the HUD target");
                int coordinate = config.scale > 1 ? 0 : 1;
                int expected = config.scale > 1 ? 128 : config.forceBilinear || test == 6 ? 96 : 0;
                int read = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
                try (PixelStore ignored = new PixelStore(true); MemoryStack stack = MemoryStack.stackPush()) {
                    ByteBuffer pixel = stack.malloc(4);
                    glBindFramebuffer(GL_READ_FRAMEBUFFER, nativeTarget.frameBufferId);
                    glReadPixels(coordinate, coordinate, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixel);
                    for (int channel = 0; channel < 3; channel++) {
                        int actual = Byte.toUnsignedInt(pixel.get(channel));
                        require(Math.abs(actual - expected) <= 2,
                                "Standard filtering test " + test + " at " + config.scale
                                        + "x: expected=" + expected + " actual=" + actual);
                    }
                } finally {
                    glBindFramebuffer(GL_READ_FRAMEBUFFER, read);
                }
                require(glGetError() == GL_NO_ERROR, "OpenGL error during standard filtering test");
            }
            Constants.LOG.info("Standard filtering PASSED: 200% averages all four samples, FSR downsampling fallback, 50% nearest/linear toggle, resize");
        } finally {
            scale.setShouldScale(false);
            config.scale = oldScale;
            config.fsr = oldFsr;
            config.forceBilinear = oldBilinear;
            config.targetFrameRate = oldTarget;
            config.sharpeningMode = oldSharpening;
            config.downscaleFilter = oldFilter;
            scale.onResolutionChanged();
            nativeTarget.bindWrite(true);
        }
    }

    private static void fill(TextureTarget target) {
        int previous = glGetInteger(GL_TEXTURE_BINDING_2D);
        try (PixelStore ignored = new PixelStore(false); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = stack.malloc(target.width * target.height * 4);
            for (int y = 0; y < target.height; y++) {
                for (int x = 0; x < target.width; x++) {
                    // Diagonal detail distinguishes filters whose horizontal and
                    // vertical averages can match on separable stripes at 2x.
                    data.put((byte) (48 + ((x + y * 3) % 7) * 24));
                    data.put((byte) (32 + y * 160 / (target.height - 1)));
                    data.put((byte) (x < target.width / 2 ? 64 : 192));
                    data.put((byte) 0);
                }
            }
            data.flip();
            glBindTexture(GL_TEXTURE_2D, target.getColorTextureId());
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, target.width, target.height, GL_RGBA, GL_UNSIGNED_BYTE, data);
        } finally {
            glBindTexture(GL_TEXTURE_2D, previous);
        }
    }

    private static byte[] blit(TextureTarget input, TextureTarget output) {
        int read = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
        int draw = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, input.frameBufferId);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, output.frameBufferId);
        glBlitFramebuffer(0, 0, input.width, input.height, 0, 0, output.width, output.height, GL_COLOR_BUFFER_BIT, GL_LINEAR);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, read);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, draw);
        return pixels(output);
    }

    private static byte[] pixels(TextureTarget target) {
        int previous = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
        try (PixelStore ignored = new PixelStore(true); MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = stack.malloc(target.width * target.height * 4);
            glBindFramebuffer(GL_READ_FRAMEBUFFER, target.frameBufferId);
            glReadPixels(0, 0, target.width, target.height, GL_RGBA, GL_UNSIGNED_BYTE, data);
            byte[] result = new byte[data.remaining()];
            data.get(result);
            return result;
        } finally {
            glBindFramebuffer(GL_READ_FRAMEBUFFER, previous);
        }
    }

    private static void verifyImage(byte[] pixels, int width, int height) {
        int bottomGreen = Byte.toUnsignedInt(pixels[1]);
        int topGreen = Byte.toUnsignedInt(pixels[((height - 1) * width) * 4 + 1]);
        require(bottomGreen < 60 && topGreen > 160, "Image flipped or output edges were not rendered: green=" + bottomGreen + "/" + topGreen + ", GL error=" + glGetError());
        require(Math.abs(Byte.toUnsignedInt(pixels[2]) - 64) < 5, "Left edge is incorrect");
        require(Math.abs(Byte.toUnsignedInt(pixels[(width - 1) * 4 + 2]) - 192) < 5, "Right edge is incorrect");
        for (int i = 3; i < pixels.length; i += 4) require(Byte.toUnsignedInt(pixels[i]) == 255, "Output must be opaque");
    }

    private static long distance(byte[] a, byte[] b) {
        long result = 0;
        for (int i = 0; i < a.length; i++) {
            if (i % 4 != 3) result += Math.abs(Byte.toUnsignedInt(a[i]) - Byte.toUnsignedInt(b[i]));
        }
        return result;
    }

    // Atlas uploads and screenshots can leave non-default pixel row strides.
    private static final class PixelStore implements AutoCloseable {
        private final int[] parameters;
        private final int[] values = new int[4];

        PixelStore(boolean pack) {
            parameters = pack ? new int[]{GL_PACK_ROW_LENGTH, GL_PACK_SKIP_PIXELS, GL_PACK_SKIP_ROWS, GL_PACK_ALIGNMENT}
                    : new int[]{GL_UNPACK_ROW_LENGTH, GL_UNPACK_SKIP_PIXELS, GL_UNPACK_SKIP_ROWS, GL_UNPACK_ALIGNMENT};
            for (int i = 0; i < parameters.length; i++) {
                values[i] = glGetInteger(parameters[i]);
                glPixelStorei(parameters[i], i == 3 ? 1 : 0);
            }
        }

        @Override
        public void close() {
            for (int i = 0; i < parameters.length; i++) glPixelStorei(parameters[i], values[i]);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
*///?}
