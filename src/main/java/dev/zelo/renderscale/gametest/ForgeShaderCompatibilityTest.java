//? 1.20.1 && forge {
/*package dev.zelo.renderscale.gametest;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import dev.zelo.renderscale.Constants;
import dev.zelo.renderscale.RenderScale;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.targets.Blaze3dRenderTargetExt;
import net.irisshaders.iris.targets.RenderTargets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

import java.lang.reflect.Field;
import java.nio.file.Files;

// Run in a disposable game directory with Oculus, a shaderpack, and
// -Drenderscale.shaderCompatibilityTest=true. Render ticks continue while paused.
public final class ForgeShaderCompatibilityTest {
    private static int step;
    private static int frames;
    private static long started;
    private static boolean done;

    private ForgeShaderCompatibilityTest() {}

    public static void frame(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || done) return;
        Minecraft client = Minecraft.getInstance();
        if (started == 0) started = System.nanoTime();
        try {
            if (System.nanoTime() - started > 180_000_000_000L) throw new AssertionError("Timed out at step " + step);
            if (step == 0) {
                if (!(client.screen instanceof TitleScreen) || client.getOverlay() != null) return;
                client.options.pauseOnLostFocus = false;
                // Reproduce modpacks that request stencil before entering a world.
                client.getMainRenderTarget().enableStencil();
                setScale(0.25f);
                RenderScaleAutoTest.INSTANCE.createWorld(client);
                step++;
                return;
            }
            if (client.level == null || client.player == null) return;
            if (step == 1 && client.screen != null) return;
            if (++frames < 30) return;
            frames = 0;
            verifyTargets();
            Constants.LOG.info("Shader compatibility test step {} passed (scale={}, paused={})",
                    step, RenderScale.getConfig().scale, client.isPaused());
            switch (step++) {
                case 1 -> setScale(1.0f);
                case 2 -> {
                    // Force the version collision instead of relying on incidental
                    // window resizes or a particular driver's texture-ID allocation.
                    RenderScale scale = RenderScale.getInstance();
                    while (version(scale.clientRenderTarget) < version(scale.renderTarget)) {
                        resize(scale.clientRenderTarget);
                    }
                    while (version(scale.renderTarget) < version(scale.clientRenderTarget)) {
                        resize(scale.renderTarget);
                    }
                    Iris.reload(); // Pipeline is created against the native target.
                    client.getMainRenderTarget().bindWrite(true);
                }
                case 3 -> client.setScreen(new PauseScreen(true));
                case 4 -> {
                    require(client.isPaused(), "Pause screen did not pause the game");
                    client.setScreen(null);
                }
                case 5 -> setScale(0.25f);
                case 6 -> client.setScreen(new PauseScreen(true));
                case 7 -> {
                    require(client.isPaused(), "Pause screen did not pause the game");
                    client.setScreen(null);
                }
                case 8 -> finish(client, null);
                default -> throw new AssertionError("Unexpected step");
            }
        } catch (Throwable error) {
            finish(client, error);
        }
    }

    private static void setScale(float value) {
        RenderScale.getConfig().scale = value;
        RenderScale.getConfig().targetFrameRate = 0;
        RenderScale.CONFIG.save();
    }

    private static int version(RenderTarget target) {
        return ((Blaze3dRenderTargetExt) target).iris$getDepthBufferVersion();
    }

    private static void resize(RenderTarget target) {
        target.resize(target.width, target.height, Minecraft.ON_OSX);
    }

    private static void verifyTargets() throws ReflectiveOperationException {
        RenderScale scale = RenderScale.getInstance();
        require(scale.renderTarget != null && scale.clientRenderTarget != null, "Missing targets");
        require(scale.renderTarget.isStencilEnabled() && scale.clientRenderTarget.isStencilEnabled(), "Stencil lost during target swap");
        require(Minecraft.getInstance().getMainRenderTarget() == scale.clientRenderTarget, "Main target not restored for UI");
        Object pipeline = Iris.getPipelineManager().getPipelineNullable();
        require(pipeline instanceof IrisRenderingPipeline, "Shader pipeline not active");
        RenderTargets targets = (RenderTargets) field(pipeline, "renderTargets");
        int depth = scale.renderTarget.getDepthTextureId();
        require(targets.getDepthTexture() == depth, "Oculus retained another target's depth texture");
        GlFramebuffer source = (GlFramebuffer) field(targets, "depthSourceFb");
        int attachment = GL45C.glGetNamedFramebufferAttachmentParameteri(source.getId(), GL30C.GL_DEPTH_ATTACHMENT,
                GL30C.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
        int stencil = GL45C.glGetNamedFramebufferAttachmentParameteri(source.getId(), GL30C.GL_STENCIL_ATTACHMENT,
                GL30C.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
        require(attachment == depth && stencil == depth, "Oculus depth and stencil are not the scaled texture");
        for (GlFramebuffer framebuffer : (Iterable<GlFramebuffer>) field(targets, "ownedFramebuffers")) {
            require(GL45C.glCheckNamedFramebufferStatus(framebuffer.getId(), GL30C.GL_FRAMEBUFFER)
                    == GL30C.GL_FRAMEBUFFER_COMPLETE, "Incomplete Oculus framebuffer " + framebuffer.getId());
        }
        // Exercise the full-size depth copy used by post-processing/particle mods.
        TextureTarget copy = new TextureTarget(scale.clientRenderTarget.width, scale.clientRenderTarget.height, true, Minecraft.ON_OSX);
        try {
            copy.enableStencil();
            copy.copyDepthFrom(scale.renderTarget);
            require(GL11C.glGetError() == GL11C.GL_NO_ERROR, "OpenGL error during rendering or depth copy");
        } finally {
            copy.destroyBuffers();
            scale.clientRenderTarget.bindWrite(true);
        }
        FramebufferPresentationTest.verify();
    }

    private static Object field(Object owner, String name) throws ReflectiveOperationException {
        Field field = owner.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void finish(Minecraft client, Throwable error) {
        done = true;
        try {
            Files.writeString(client.gameDirectory.toPath().resolve("shader-compatibility-result.txt"),
                    error == null ? "PASS\n" : "FAIL: " + error + "\n");
        } catch (java.io.IOException io) {
            Constants.LOG.error("Could not write shader compatibility result", io);
        }
        if (error == null) Constants.LOG.info("Shader compatibility test PASSED");
        else Constants.LOG.error("Shader compatibility test FAILED at step " + step, error);
        client.stop();
    }
}
*///?}
