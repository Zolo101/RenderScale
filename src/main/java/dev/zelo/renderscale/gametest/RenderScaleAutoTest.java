package dev.zelo.renderscale.gametest;

// Auto test stuff
// scripts/run-gametests.sh.
//? <1.21.4 {

/*import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import dev.zelo.renderscale.Constants;
import dev.zelo.renderscale.RenderScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class RenderScaleAutoTest {
    public static final boolean ENABLED = System.getProperty("renderscale.autotest") != null;
    public static final RenderScaleAutoTest INSTANCE = new RenderScaleAutoTest();

    private static final float TEST_SCALE = 0.5f;
    private static final int WATCHDOG_TICKS = 20 * 180;

    private enum Phase {
        WAIT_TITLE, WAIT_WORLD, SETTLE, NATIVE_SHOT, WAIT_NATIVE, SCALED_SHOT, WAIT_SCALED,
        DYNAMIC_SHOT, DYNAMIC_CAPTURE, WAIT_DYNAMIC, RECOVERY, DONE
    }

    private Phase phase = Phase.WAIT_TITLE;
    private int phaseTicks = 0;
    private int totalTicks = 0;
    private int phaseFrames = 0;
    private final AtomicInteger pendingShots = new AtomicInteger();
    private boolean announced = false;

    private RenderScaleAutoTest() {
    }

    /^* Call once per client tick (end phase) from each loader's entrypoint. ^/
    public void tick(Minecraft client) {
        if (!ENABLED || phase == Phase.DONE) return;

        if (!announced) {
            announced = true;
            Constants.LOG.info("RenderScale autotest enabled");
        }

        totalTicks++;
        phaseTicks++;

        try {
            step(client);

            if (totalTicks > WATCHDOG_TICKS) {
                throw new AssertionError("Autotest timed out in phase " + phase);
            }
        } catch (Throwable t) {
            fail(client, t);
        }
    }

    // Several client ticks can run before the next frame is rendered.
    public void frameRendered() {
        if (ENABLED && phase != Phase.DONE) phaseFrames++;
    }

    private boolean rendered() {
        return phaseFrames >= 2;
    }

    private void step(Minecraft client) {
        switch (phase) {
            case WAIT_TITLE -> {
                client.options.pauseOnLostFocus = false;

                if (client.screen instanceof TitleScreen && client.getOverlay() == null) {
                    createWorld(client);
                    setPhase(Phase.WAIT_WORLD);
                } else if (phaseTicks > 200) {
                    // Skips whatever else is up, e.g. the accessibility onboarding screen
                    client.setScreen(new TitleScreen());
                    phaseTicks = 0;
                }
            }
            case WAIT_WORLD -> {
                if (client.player != null && client.level != null && client.screen == null) {
                    setPhase(Phase.SETTLE);
                }
            }
            case SETTLE -> {
                if (phaseTicks == 60) {
                    // Keep the GUI visible (RenderScale must NOT scale it) and force gui
                    // scale 1 so the UI detail is fine enough for the hotbar check
                    client.options.guiScale().set(1);
                    client.resizeDisplay();
                    runCommand(client, "fill -1 148 -1 1 148 1 minecraft:barrier");
                    runCommand(client, "tp @s 0.5 149.0 0.5 0.0 0.0");
                    runCommand(client, "summon minecraft:armor_stand 0.5 149.0 3.5 {Glowing:1b,NoGravity:1b,Rotation:[180f,0f]}");
                    runCommand(client, "time set noon");
                    setRenderScale(1.0f);
                    setPhase(Phase.NATIVE_SHOT);
                }
            }
            case NATIVE_SHOT -> {
                if (phaseTicks >= 60 && rendered()) {
                    takeScreenshot(client, "renderscale_native.png");
                    setPhase(Phase.WAIT_NATIVE);
                }
            }
            case WAIT_NATIVE -> {
                if (fileReady(screenshotPath(client, "renderscale_native.png"))) {
                    setRenderScale(TEST_SCALE);
                    setPhase(Phase.SCALED_SHOT);
                } else if (phaseTicks > 200) {
                    throw new AssertionError("Native screenshot was never written");
                }
            }
            case SCALED_SHOT -> {
                if (phaseTicks >= 20 && rendered()) {
                    takeScreenshot(client, "renderscale_scaled.png");
                    setPhase(Phase.WAIT_SCALED);
                }
            }
            case WAIT_SCALED -> {
                if (fileReady(screenshotPath(client, "renderscale_scaled.png"))) {
                    ScreenshotVerifier.verifyScaling(
                            screenshotPath(client, "renderscale_native.png"),
                            screenshotPath(client, "renderscale_scaled.png"));
                    RenderScale.getConfig().scale = 1.0f;
                    RenderScale.getConfig().targetFrameRate = 1000;
                    RenderScale.getConfig().aggressionLevel = dev.zelo.renderscale.config.RenderScaleConfig.Aggression.EXTREME;
                    RenderScale.getConfig().minimumScale = 50;
                    client.options.enableVsync().set(false);
                    client.options.framerateLimit().set(30);
                    // Automated clients may open behind the editor; exercise focused gameplay.
                    client.setWindowActive(true);
                    RenderScale.CONFIG.save();
                    setPhase(Phase.DYNAMIC_SHOT);
                } else if (phaseTicks > 200) {
                    throw new AssertionError("Scaled screenshot was never written");
                }
            }
            case DYNAMIC_SHOT -> {
                RenderScale renderer = RenderScale.getInstance();
                if (phaseTicks <= 40) {
                    if (renderer.getRenderScaleFactor() != 1.0) {
                        throw new AssertionError("A stationary camera must hold its resolution");
                    }
                    return;
                }
                client.player.setYRot(client.player.getYRot() > 0 ? -0.1f : 0.1f);
                if (renderer.getRenderScaleFactor() == 0.5) {
                    if (renderer.renderTarget.width != client.getWindow().getWidth() / 2
                            || renderer.renderTarget.height != client.getWindow().getHeight() / 2
                            || renderer.getCurrentScaleFactor() != 1.0) {
                        throw new AssertionError("Dynamic target size or native UI sizing is incorrect");
                    }
                    client.player.setYRot(0);
                    setPhase(Phase.DYNAMIC_CAPTURE);
                } else if (phaseTicks > 600) {
                    throw new AssertionError("Dynamic Scale did not reach the minimum: scale="
                            + renderer.getRenderScaleFactor() + ", focused=" + client.isWindowActive()
                            + ", paused=" + client.isPaused() + ", screen=" + client.screen);
                }
            }
            case DYNAMIC_CAPTURE -> {
                // Let hand sway settle after the camera stops before comparing screenshots.
                if (phaseTicks >= 20 && rendered()) {
                    takeScreenshot(client, "renderscale_dynamic.png");
                    setPhase(Phase.WAIT_DYNAMIC);
                }
            }
            case WAIT_DYNAMIC -> {
                if (fileReady(screenshotPath(client, "renderscale_dynamic.png"))) {
                    ScreenshotVerifier.verifyScaling(
                            screenshotPath(client, "renderscale_native.png"),
                            screenshotPath(client, "renderscale_dynamic.png"));
                    RenderScale.getConfig().targetFrameRate = 1;
                    setPhase(Phase.RECOVERY);
                } else if (phaseTicks > 200) {
                    throw new AssertionError("Dynamic screenshot was never written");
                }
            }
            case RECOVERY -> {
                RenderScale renderer = RenderScale.getInstance();
                if (phaseTicks <= 40) {
                    if (renderer.getRenderScaleFactor() != 0.5) {
                        throw new AssertionError("Recovery must wait for camera movement");
                    }
                    return;
                }
                client.player.setYRot(client.player.getYRot() > 0 ? -0.1f : 0.1f);
                if (renderer.getRenderScaleFactor() == 1.0) {
                    if (renderer.renderTarget.width != client.getWindow().getWidth()
                            || renderer.renderTarget.height != client.getWindow().getHeight()) {
                        throw new AssertionError("Recovery did not restore the world render target size");
                    }
                    RenderScale.getConfig().targetFrameRate = 0;
                    RenderScale.getConfig().aggressionLevel = dev.zelo.renderscale.config.RenderScaleConfig.Aggression.NORMAL;
                    RenderScale.CONFIG.save();
                    pass(client);
                } else if (phaseTicks > 600) {
                    throw new AssertionError("Dynamic Scale did not recover to the maximum");
                }
            }
            case DONE -> {}
        }
    }

    private void setPhase(Phase next) {
        Constants.LOG.info("Autotest phase: {} -> {}", phase, next);
        phase = next;
        phaseTicks = 0;
        phaseFrames = 0;
    }

    void createWorld(Minecraft client) {
        LevelSettings levelSettings = new LevelSettings(
                "RenderScale Autotest",
                GameType.CREATIVE,
                false,
                Difficulty.PEACEFUL,
                true, // cheats, needed for the scene commands
                new GameRules(/^? >=1.21.2 {^/ net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS /^?}^/),
                WorldDataConfiguration.DEFAULT);

        client.createWorldOpenFlows().createFreshLevel(
                "renderscale-autotest-" + System.currentTimeMillis() / 1000,
                levelSettings,
                new WorldOptions(1L, false, false),
                //? >=1.21.2 {
                WorldPresets::createFlatWorldDimensions
                //?} else {
                /^access -> access.registryOrThrow(Registries.WORLD_PRESET)
                        .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions()
                ^///?}
                //? >1.20.1
                , null
        );
    }

    private void runCommand(Minecraft client, String command) {
        if (client.player == null) throw new AssertionError("No player to run command: " + command);
        client.player.connection.sendUnsignedCommand(command);
    }

    private void setRenderScale(float scale) {
        RenderScale.getConfig().scale = scale;
        RenderScale.getConfig().targetFrameRate = 0;
        // Saving fires the save listener, which resizes the render targets
        RenderScale.CONFIG.save();
    }

    private void takeScreenshot(Minecraft client, String fileName) {
        // A previous run must not satisfy the completion check if this write fails.
        try {
            Files.deleteIfExists(screenshotPath(client, fileName));
        } catch (java.io.IOException e) {
            throw new AssertionError("Could not remove previous screenshot " + fileName, e);
        }
        pendingShots.incrementAndGet();
        Screenshot.grab(client.gameDirectory, fileName, client.getMainRenderTarget(),
                message -> pendingShots.decrementAndGet());
    }

    private static Path screenshotPath(Minecraft client, String fileName) {
        return client.gameDirectory.toPath().resolve("screenshots").resolve(fileName);
    }

    /^* Wait for the asynchronous writer's callback before reading the screenshot. ^/
    private boolean fileReady(Path path) {
        if (pendingShots.get() != 0) return false;
        try {
            if (!Files.isRegularFile(path) || Files.size(path) == 0) {
                throw new AssertionError("Screenshot was not written: " + path);
            }
            return true;
        } catch (java.io.IOException e) {
            throw new AssertionError("Could not inspect screenshot " + path, e);
        }
    }

    private void pass(Minecraft client) {
        setPhase(Phase.DONE);
        writeResult(client, "PASS");
        Constants.LOG.info("RenderScale autotest PASSED");
        client.stop();
    }

    private void fail(Minecraft client, Throwable cause) {
        setPhase(Phase.DONE);
        Constants.LOG.error("RenderScale autotest FAILED", cause);
        writeResult(client, "FAIL: " + cause);
        // halt() so the gradle task fails like a failed gametest does on newer versions
        Runtime.getRuntime().halt(1);
    }

    private static void writeResult(Minecraft client, String text) {
        try {
            File file = new File(client.gameDirectory, "autotest-result.txt");
            Files.writeString(file.toPath(), text + System.lineSeparator());
        } catch (Exception e) {
            Constants.LOG.error("Failed to write autotest result file", e);
        }
    }
}
*///?}
