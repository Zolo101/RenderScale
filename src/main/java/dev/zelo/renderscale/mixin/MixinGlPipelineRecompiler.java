//? >=26.3 {
package dev.zelo.renderscale.mixin;

import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.opengl.GlPipelineRecompiler;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GlPipelineRecompiler.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinGlPipelineRecompiler {
    @ModifyConstant(method = "decompileShader", constant = @Constant(intValue = 330))
    private int renderScale$fp16ShaderVersion(int version, BackendRenderPipeline.CreateInfo.Shader shader) {
        // RenderPearl otherwise lowers even GLSL 450 input to 330, where the
        // AMD half-float built-in overloads are unavailable. These two shaders
        // are only selected after checking OpenGL 4.5 and FP16 support.
        return switch (shader.name()) {
            case "renderscale:core/easu_fp16", "renderscale:core/rcas_fp16" -> 450;
            default -> version;
        };
    }
}
//?}
