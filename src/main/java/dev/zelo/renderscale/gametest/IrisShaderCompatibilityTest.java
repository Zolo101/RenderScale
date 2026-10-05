//? fabric && >=26.3 {
package dev.zelo.renderscale.gametest;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import dev.zelo.renderscale.Constants;
import dev.zelo.renderscale.RenderScale;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.targets.Blaze3dRenderTargetExt;
import net.irisshaders.iris.targets.RenderTargets;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

// Opt in with -Drenderscale.shaderCompatibilityTest=true and Iris plus a
// configured shaderpack in a disposable client-gametest directory.
final class IrisShaderCompatibilityTest {
    private IrisShaderCompatibilityTest() {}

    static void run(ClientGameTestContext context) {
        if (!RenderScale.PLATFORM.isModLoaded("iris")) {
            throw new AssertionError("Shader compatibility test requires Iris and a shaderpack");
        }
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            context.runOnClient(client -> {
                RenderScale.getConfig().scale = 1.0f;
                RenderScale.getConfig().irisScale = -1.0f;
                RenderScale.getConfig().targetFrameRate = 0;
                RenderScale.CONFIG.save();
            });
            context.waitTicks(5);

            // Both targets have equal dimensions and counters. Enabling shaders
            // builds the pipeline against the native target before world rendering.
            context.runOnClient(client -> {
                try {
                    Iris.toggleShaders(client, false);
                    matchDepthVersions();
                    Iris.toggleShaders(client, true);
                } catch (Exception exception) {
                    throw new AssertionError("Could not toggle shaders", exception);
                }
            });
            verifyAfterFrames(context, "enabling shaders at native scale");

            context.runOnClient(client -> {
                RenderScale.getConfig().forceBilinear = true;
                RenderScale.CONFIG.save();
            });
            verifyAfterFrames(context, "saving a filter without changing resolution");

            context.runOnClient(client -> {
                RenderScale.getConfig().scale = 0.5f;
                RenderScale.CONFIG.save();
                matchDepthVersions();
                reloadShaders();
            });
            verifyAfterFrames(context, "reloading shaders with equal counters at half scale");

            context.runOnClient(client -> {
                RenderScale.getConfig().scale = 1.5f;
                RenderScale.CONFIG.save();
            });
            verifyAfterFrames(context, "changing to supersampling");
            Constants.LOG.info("Iris shader depth attachment regression test PASSED");
        }
    }

    private static void reloadShaders() {
        try {
            Iris.reload();
        } catch (Exception exception) {
            throw new AssertionError("Could not reload shaders", exception);
        }
    }

    private static void matchDepthVersions() {
        RenderScale renderer = RenderScale.getInstance();
        while (version(renderer.renderTarget) < version(renderer.clientRenderTarget)) {
            renderer.renderTarget.resize(renderer.renderTarget.width, renderer.renderTarget.height);
        }
        while (version(renderer.clientRenderTarget) < version(renderer.renderTarget)) {
            renderer.clientRenderTarget.resize(renderer.clientRenderTarget.width, renderer.clientRenderTarget.height);
        }
    }

    private static int version(RenderTarget target) {
        return ((Blaze3dRenderTargetExt) target).iris$getDepthBufferVersion();
    }

    private static void verifyAfterFrames(ClientGameTestContext context, String checkpoint) {
        context.waitTicks(5);
        context.runOnClient(client -> {
            try {
                RenderScale renderer = RenderScale.getInstance();
                Object pipeline = Iris.getPipelineManager().getPipelineNullable();
                if (!(pipeline instanceof IrisRenderingPipeline)) {
                    throw new AssertionError("Shader pipeline is not active");
                }
                var field = pipeline.getClass().getDeclaredField("renderTargets");
                field.setAccessible(true);
                RenderTargets targets = (RenderTargets) field.get(pipeline);
                if (targets.getDepthTexture() != renderer.renderTarget.getDepthTexture()) {
                    throw new AssertionError("Iris retained the native target's depth texture: " + checkpoint);
                }
                if (client.gameRenderer.mainRenderTarget() != renderer.clientRenderTarget) {
                    throw new AssertionError("Native target was not restored for the UI");
                }
                field = targets.getClass().getDeclaredField("depthSourceFb");
                field.setAccessible(true);
                GlFramebuffer source = (GlFramebuffer) field.get(targets);
                int previous = GL11C.glGetInteger(GL30C.GL_READ_FRAMEBUFFER_BINDING);
                try {
                    source.bindAsReadBuffer();
                    int attachment = GL30C.glGetFramebufferAttachmentParameteri(GL30C.GL_READ_FRAMEBUFFER,
                            GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
                    int expected = ((GlTexture) renderer.renderTarget.getDepthTexture()).glId();
                    if (attachment != expected) {
                        throw new AssertionError("Iris framebuffer uses the wrong depth attachment: " + checkpoint);
                    }
                    if (GL30C.glCheckFramebufferStatus(GL30C.GL_READ_FRAMEBUFFER) != GL30C.GL_FRAMEBUFFER_COMPLETE) {
                        throw new AssertionError("Incomplete Iris depth framebuffer: " + checkpoint);
                    }
                } finally {
                    GlStateManager._glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, previous);
                }
                Constants.LOG.info("Iris depth attachment passed: {}", checkpoint);
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Could not inspect Iris depth attachments", exception);
            }
        });
    }
}
//?}
