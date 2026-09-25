package dev.zelo.renderscale.gametest;

// The Fabric Client Gametest API only exists for Minecraft 1.21.4+
//? fabric && >=1.21.4 {
//~ if >= 26.1 'getClientWorld' -> 'getClientLevel' {

import java.nio.file.Path;

import dev.zelo.renderscale.RenderScale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

// scripts/run-gametests.sh
public class RenderScaleClientGameTest implements FabricClientGameTest {
    private static final float TEST_SCALE = 0.5f;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            //? >=26.3 {
            singleplayer.getConnection().waitForChunksRender();
            //?} else
            //singleplayer.getClientLevel().waitForChunksRender();

            singleplayer.getServer().runCommand("fill -1 148 -1 1 148 1 minecraft:barrier");
            singleplayer.getServer().runCommand("tp @p 0.5 149.0 0.5 0 0");
            singleplayer.getServer().runCommand(
                    "summon minecraft:armor_stand 0.5 149.0 3.5 {Glowing:1b,NoGravity:1b,Rotation:[180f,0f]}");
            singleplayer.getServer().runCommand("time set noon");
            context.waitTicks(10);

            // GUI check
            context.runOnClient(client -> {
                client.options.guiScale().set(1);
                //? >= 26.1 {
                client.resizeGui();
                //?} else
                //client.resizeDisplay();
            });

            setRenderScale(context, 1.0f);
            // TODO: Can I just disable vignette...?
            context.waitTicks(60);
            Path nativeShot = context.takeScreenshot("renderscale_native");

            setRenderScale(context, TEST_SCALE);
            context.waitTicks(5);
            Path scaledShot = context.takeScreenshot("renderscale_scaled");

            ScreenshotVerifier.verifyScaling(nativeShot, scaledShot);
            //? >=26.2
            verifyFilters(context, nativeShot);
            verifyDynamicScale(context);
            //? >=26.3
            verifyWindowResize(context);
        }
    }

    //? >=26.3 {
    private static void verifyWindowResize(ClientGameTestContext context) {
        int oldWidth = context.computeOnClient(client -> client.getWindow().getScreenWidth());
        int oldHeight = context.computeOnClient(client -> client.getWindow().getScreenHeight());
        try {
            context.runOnClient(client -> client.getWindow().setWindowed(960, 540));
            context.waitFor(client -> client.getWindow().getScreenWidth() == 960
                    && client.getWindow().getScreenHeight() == 540);
            setRenderScale(context, 1.0f);
            context.waitTicks(5);
            Path nativeShot = context.takeScreenshot("renderscale_resized_native");
            setRenderScale(context, TEST_SCALE);
            context.waitTicks(5);
            context.runOnClient(client -> {
                var target = RenderScale.getInstance().renderTarget;
                if (target.width != client.getWindow().getWidth() / 2
                        || target.height != client.getWindow().getHeight() / 2) {
                    throw new AssertionError("Resizing the SDL window must resize the scaled render target");
                }
            });
            ScreenshotVerifier.verifyScaling(nativeShot, context.takeScreenshot("renderscale_resized"));
        } finally {
            context.runOnClient(client -> client.getWindow().setWindowed(oldWidth, oldHeight));
            setRenderScale(context, 1.0f);
        }
    }
    //?}

    //? >=26.2 {
    private static void verifyFilters(ClientGameTestContext context, Path nativeShot) {
        try {
            setRenderScale(context, TEST_SCALE);
            context.runOnClient(client -> {
                RenderScale.getConfig().forceLinear = true;
                RenderScale.CONFIG.save();
            });
            context.waitTicks(5);
            ScreenshotVerifier.verifyFilteredScaling(nativeShot, context.takeScreenshot("renderscale_linear"));

            context.runOnClient(client -> {
                // Fail the test directly if ShaderC cannot compile either FSR pass.
                //? >=26.3 {
                com.mojang.blaze3d.systems.RenderSystem.getCompiledPipeline(RenderScale.FSR_EASU_PIPELINE);
                com.mojang.blaze3d.systems.RenderSystem.getCompiledPipeline(RenderScale.FSR_RCAS_PIPELINE);
                //?} else {
                /*var device = com.mojang.blaze3d.systems.RenderSystem.getDevice();
                if (!device.precompilePipeline(RenderScale.FSR_EASU_PIPELINE).isValid()
                        || !device.precompilePipeline(RenderScale.FSR_RCAS_PIPELINE).isValid()) {
                    throw new AssertionError("FSR pipelines must compile");
                }
                *///?}
                RenderScale.getConfig().forceLinear = false;
                RenderScale.getConfig().fsr = true;
                RenderScale.CONFIG.save();
            });
            context.waitTicks(5);
            ScreenshotVerifier.verifyFilteredScaling(nativeShot, context.takeScreenshot("renderscale_fsr"));

            // Don't need to test this...
//            setRenderScale(context, 1.0f);
//            context.waitTicks(5);
//            ScreenshotVerifier.verifyFilteredScaling(nativeShot, context.takeScreenshot("renderscale_fsr_native"));
//
//            setRenderScale(context, 2.0f);
//            context.waitTicks(5);
//            ScreenshotVerifier.verifyFilteredScaling(nativeShot, context.takeScreenshot("renderscale_fsr_downsampled"));
        } finally {
            context.runOnClient(client -> {
                RenderScale.getConfig().fsr = false;
                RenderScale.getConfig().forceLinear = false;
                RenderScale.CONFIG.save();
            });
        }
    }
    //?}

    private static void verifyDynamicScale(ClientGameTestContext context) {
        try {
            context.runOnClient(client -> {
                client.options.enableVsync().set(false);
                client.options.framerateLimit().set(30);
                RenderScale.getConfig().scale = 1.0f;
                RenderScale.getConfig().targetFrameRate = 1000;
                RenderScale.getConfig().aggressionLevel = dev.zelo.renderscale.config.RenderScaleConfig.Aggression.EXTREME;
                RenderScale.getConfig().minimumScale = 0.5f;
                RenderScale.CONFIG.save();
            });
            context.waitTicks(20);
            context.runOnClient(client -> {
                if (RenderScale.getInstance().getRenderScaleFactor() != 1.0) {
                    throw new AssertionError("A stationary camera must hold its resolution");
                }
            });
            context.waitFor(client -> {
                client.player.setYRot(client.player.getYRot() > 0 ? -0.1f : 0.1f);
                return RenderScale.getInstance().getRenderScaleFactor() == 0.5;
            }, 1200);
            context.runOnClient(client -> client.player.setYRot(0));
            // Let first-person hand sway settle after the simulated camera turns.
            context.waitTicks(20);
            context.runOnClient(client -> {
                RenderScale renderer = RenderScale.getInstance();
                if (renderer.renderTarget.width != client.getWindow().getWidth() / 2
                        || renderer.renderTarget.height != client.getWindow().getHeight() / 2
                        || renderer.getCurrentScaleFactor() != 1.0) {
                    throw new AssertionError("Dynamic Scale must resize the world target and preserve native UI sizing");
                }
            });
            Path dynamicShot = context.takeScreenshot("renderscale_dynamic");

            // Change the target without resetting the controller to exercise upward recovery.
            context.runOnClient(client -> RenderScale.getConfig().targetFrameRate = 1);
            context.waitTicks(20);
            context.runOnClient(client -> {
                if (RenderScale.getInstance().getRenderScaleFactor() != 0.5) {
                    throw new AssertionError("Recovery must also wait for camera movement");
                }
            });
            context.waitFor(client -> {
                client.player.setYRot(client.player.getYRot() > 0 ? -0.1f : 0.1f);
                return RenderScale.getInstance().getRenderScaleFactor() == 1.0;
            }, 1200);
            context.runOnClient(client -> client.player.setYRot(0));
            context.runOnClient(client -> {
                RenderScale renderer = RenderScale.getInstance();
                if (renderer.renderTarget.width != client.getWindow().getWidth()
                        || renderer.renderTarget.height != client.getWindow().getHeight()) {
                    throw new AssertionError("Recovery must restore the world render target size");
                }
            });
            // Compare both resolutions after the same camera movement and at the
            // same frame cap so hand animation and lighting have matching state.
            context.waitTicks(20);
            ScreenshotVerifier.verifyScaling(context.takeScreenshot("renderscale_recovered"), dynamicShot);
        } finally {
            context.runOnClient(client -> {
                RenderScale.getConfig().targetFrameRate = 0;
                RenderScale.getConfig().aggressionLevel = dev.zelo.renderscale.config.RenderScaleConfig.Aggression.NORMAL;
                RenderScale.CONFIG.save();
            });
        }
    }

    private static void setRenderScale(ClientGameTestContext context, float scale) {
        context.runOnClient(client -> {
            RenderScale.getConfig().scale = scale;
            RenderScale.getConfig().targetFrameRate = 0;
            // Saving fires the save listener, which resizes the render targets
            RenderScale.CONFIG.save();
        });
    }

}
//~}
//?}
