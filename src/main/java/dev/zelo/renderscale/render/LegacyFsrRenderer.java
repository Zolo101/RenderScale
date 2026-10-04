//? 1.20.1 || 1.21.1 {
/*package dev.zelo.renderscale.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.zelo.renderscale.Constants;
import dev.zelo.renderscale.config.RenderScaleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33C.*;

/^** FSR presentation for the framebuffer renderer used by 1.20.1 and 1.21.1. ^/
public final class LegacyFsrRenderer implements AutoCloseable {
    private static final String VERTEX = """
            #version 330
            void main() {
                vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
                gl_Position = vec4(p * 2.0 - 1.0, 0.0, 1.0);
            }
            """;
    private static final String BLIT = """
            #version 330
            uniform sampler2D InSampler;
            uniform vec2 ScreenSize;
            out vec4 fragColor;
            void main() { fragColor = vec4(texture(InSampler, gl_FragCoord.xy / ScreenSize).rgb, 1.0); }
            """;

    private final Map<String, Integer> programs = new HashMap<>();
    private int vertexArray, sampler, framebuffer, texture, width, height;
    private int fp16Extension;
    private boolean initialized, failed;

    public String precision() {
        return fp16Extension == 0 ? "FP32" : "FP16";
    }

    public boolean isFailed() {
        return failed;
    }

    // False asks the caller to use vanilla's blit, including when all effects are off.
    public boolean blit(RenderTarget input, RenderTarget output, RenderScaleConfig config) {
        boolean fsr = config.fsr && input.width <= output.width && input.height <= output.height;
        boolean sharpen = config.getSharpeningMode() != RenderScaleConfig.SharpeningMode.OFF
                && config.getSharpeningBlend() > 0.0f;
        if (failed || (!fsr && !sharpen)) return false;
        RenderSystem.assertOnRenderThread();

        // All state changes inside this scope use raw GL and are restored before
        // returning, so Minecraft's and shader mods' cached state stays valid.
        try (State ignored = new State()) {
            initialize();
            ensureIntermediate(output.width, output.height);
            glDisable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_CULL_FACE);
            glDisable(GL_SCISSOR_TEST);
            glDisable(GL_STENCIL_TEST);
            glDisable(GL_FRAMEBUFFER_SRGB);
            glDepthMask(false);
            glColorMask(true, true, true, true);
            glBindVertexArray(vertexArray);
            glBindSampler(0, sampler);
            if (fsr) {
                pass(program("easu", false, false), input.getColorTextureId(), framebuffer, GL_NEAREST, 1.0f);
                pass(program("rcas", false, false), texture, output.frameBufferId, GL_NEAREST, 1.0f);
            } else {
                int blit = programs.computeIfAbsent("blit", key -> link(BLIT));
                pass(blit, input.getColorTextureId(), framebuffer, config.getFilter() ? GL_LINEAR : GL_NEAREST, 1.0f);
                boolean denoise = config.getSharpeningMode() == RenderScaleConfig.SharpeningMode.RCAS_DENOISE;
                pass(program("rcas", true, denoise), texture, output.frameBufferId, GL_NEAREST, config.getSharpeningBlend());
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            close();
            failed = true;
            Constants.LOG.error("FSR1/sharpening unavailable; using the standard blit until shaders reload", exception);
            return false;
        }
    }

    private void initialize() throws IOException {
        if (initialized) return;
        vertexArray = glGenVertexArrays();
        sampler = glGenSamplers();
        glSamplerParameteri(sampler, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glSamplerParameteri(sampler, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        var capabilities = GL.getCapabilities();
        if (capabilities.GL_AMD_gpu_shader_half_float) fp16Extension = 2;
        else if (capabilities.GL_NV_gpu_shader5) fp16Extension = 3;
        else {
            for (int i = 0; i < glGetInteger(GL_NUM_EXTENSIONS); i++) {
                String extension = glGetStringi(GL_EXTENSIONS, i);
                if ("GL_EXT_shader_explicit_arithmetic_types_float16".equals(extension)
                        || "GL_EXT_shader_explicit_arithmetic_types".equals(extension)) {
                    fp16Extension = 1;
                    break;
                }
            }
        }
        if (fp16Extension != 0) {
            try {
                program("easu", false, false);
                program("rcas", false, false);
            } catch (IOException | RuntimeException exception) {
                programs.values().forEach(LegacyFsrRenderer::deleteProgram);
                programs.clear();
                fp16Extension = 0;
                Constants.LOG.warn("FSR1: FP16 shader compilation failed; trying FP32", exception);
            }
        }
        initialized = true;
        Constants.LOG.info("FSR1: using {}", precision());
    }

    private int program(String name, boolean sharpen, boolean denoise) throws IOException {
        boolean half = !sharpen && fp16Extension != 0;
        String key = name + (half ? "_fp16" : "") + (sharpen ? "_sharpen" : "") + (denoise ? "_denoise" : "");
        Integer cached = programs.get(key);
        if (cached != null) return cached;
        String source = read("core/" + name + (half ? "_fp16" : "") + ".fsh");
        int firstLine = source.indexOf('\n');
        String header = half ? "#version 450\n#define RENDERSCALE_FP16 " + fp16Extension + "\n"
                : "#version 420\n";
        if (sharpen) header += "#define RENDERSCALE_SHARPNESS SharpeningStrength\n";
        if (denoise) header += "#define FSR_RCAS_DENOISE\n";
        source = header + source.substring(firstLine + 1);
        if (sharpen) source = source.replace("out vec4 fragColor;", "uniform float SharpeningStrength;\nout vec4 fragColor;");
        int program = link(source);
        programs.put(key, program);
        return program;
    }

    // Expand only the same imports used by the shared FSR shaders. The old
    // renderer has no Globals buffer; its output size is an ordinary uniform.
    private static String read(String path) throws IOException {
        var resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(
                Identifier.tryParse("renderscale:shaders/" + path));
        StringBuilder source = new StringBuilder();
        try (var reader = resource.openAsReader()) {
            for (String line; (line = reader.readLine()) != null;) {
                if (line.startsWith("#moj_import <renderscale:")) {
                    source.append(read("include/" + line.substring("#moj_import <renderscale:".length(), line.indexOf('>'))));
                } else if (line.equals("#moj_import <minecraft:globals.glsl>")) {
                    source.append("uniform vec2 ScreenSize;\n");
                } else {
                    source.append(line).append('\n');
                }
            }
        }
        return source.toString();
    }

    private static int compile(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException(log);
        }
        return shader;
    }

    private static int link(String source) {
        int vertex = compile(GL_VERTEX_SHADER, VERTEX);
        int fragment = 0;
        int program = glCreateProgram();
        try {
            fragment = compile(GL_FRAGMENT_SHADER, source);
            glAttachShader(program, vertex);
            glAttachShader(program, fragment);
            glLinkProgram(program);
            if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
                throw new IllegalStateException(glGetProgramInfoLog(program));
            }
            return program;
        } catch (RuntimeException exception) {
            glDeleteProgram(program);
            throw exception;
        } finally {
            glDeleteShader(vertex);
            if (fragment != 0) glDeleteShader(fragment);
        }
    }

    private void ensureIntermediate(int targetWidth, int targetHeight) {
        if (texture == 0) texture = glGenTextures();
        if (framebuffer == 0) framebuffer = glGenFramebuffers();
        if (width == targetWidth && height == targetHeight) return;
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, targetWidth, targetHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebuffer);
        glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
        if (glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Incomplete FSR intermediate framebuffer");
        }
        width = targetWidth;
        height = targetHeight;
    }

    private void pass(int program, int input, int output, int filter, float strength) {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, output);
        glViewport(0, 0, width, height);
        glUseProgram(program);
        glBindTexture(GL_TEXTURE_2D, input);
        glSamplerParameteri(sampler, GL_TEXTURE_MIN_FILTER, filter);
        glSamplerParameteri(sampler, GL_TEXTURE_MAG_FILTER, filter);
        glUniform1i(glGetUniformLocation(program, "InSampler"), 0);
        glUniform2f(glGetUniformLocation(program, "ScreenSize"), width, height);
        glUniform1f(glGetUniformLocation(program, "SharpeningStrength"), strength);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private static void deleteProgram(int program) {
        glDeleteProgram(program);
    }

    @Override
    public void close() {
        programs.values().forEach(LegacyFsrRenderer::deleteProgram);
        programs.clear();
        if (vertexArray != 0) glDeleteVertexArrays(vertexArray);
        if (sampler != 0) glDeleteSamplers(sampler);
        if (framebuffer != 0) glDeleteFramebuffers(framebuffer);
        if (texture != 0) glDeleteTextures(texture);
        vertexArray = sampler = framebuffer = texture = width = height = fp16Extension = 0;
        initialized = failed = false;
    }

    private static final class State implements AutoCloseable {
        private static final int[] CAPABILITIES = {GL_BLEND, GL_DEPTH_TEST, GL_CULL_FACE, GL_SCISSOR_TEST, GL_STENCIL_TEST, GL_FRAMEBUFFER_SRGB};
        private final boolean[] enabled = new boolean[CAPABILITIES.length];
        private final int program = glGetInteger(GL_CURRENT_PROGRAM);
        private final int vertexArray = glGetInteger(GL_VERTEX_ARRAY_BINDING);
        private final int framebuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        private final int activeTexture = glGetInteger(GL_ACTIVE_TEXTURE);
        private final boolean depthMask = glGetBoolean(GL_DEPTH_WRITEMASK);
        private final int[] viewport = new int[4];
        private final boolean[] colorMask = new boolean[4];
        private final int texture, sampler;

        State() {
            glActiveTexture(GL_TEXTURE0);
            texture = glGetInteger(GL_TEXTURE_BINDING_2D);
            sampler = glGetInteger(GL_SAMPLER_BINDING);
            for (int i = 0; i < enabled.length; i++) enabled[i] = glIsEnabled(CAPABILITIES[i]);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer values = stack.mallocInt(4);
                glGetIntegerv(GL_VIEWPORT, values);
                values.get(viewport);
                ByteBuffer mask = stack.malloc(4);
                glGetBooleanv(GL_COLOR_WRITEMASK, mask);
                for (int i = 0; i < 4; i++) colorMask[i] = mask.get(i) != 0;
            }
        }

        @Override
        public void close() {
            glUseProgram(program);
            glBindVertexArray(vertexArray);
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebuffer);
            glBindTexture(GL_TEXTURE_2D, texture);
            glBindSampler(0, sampler);
            glActiveTexture(activeTexture);
            glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            glDepthMask(depthMask);
            glColorMask(colorMask[0], colorMask[1], colorMask[2], colorMask[3]);
            for (int i = 0; i < enabled.length; i++) {
                if (enabled[i]) glEnable(CAPABILITIES[i]);
                else glDisable(CAPABILITIES[i]);
            }
        }
    }
}
*///?}
