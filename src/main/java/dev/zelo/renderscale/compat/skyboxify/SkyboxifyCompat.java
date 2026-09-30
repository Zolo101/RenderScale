package dev.zelo.renderscale.compat.skyboxify;

import dev.zelo.renderscale.Constants;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Skyboxify queues its sky layers every frame during extraction, but only clears the queue when the vanilla sky
 * pass actually renders. When vanilla skips the sky (e.g. underwater), the queue keeps growing and is drawn all at
 * once when the sky comes back, causing a large frametime spike that scales with the render resolution.
 * Clearing the queue after the level has rendered is a no-op in normal frames.
 * <p>
 * Everything is reflective so Skyboxify stays an optional dependency.
 */
public final class SkyboxifyCompat {
    private static boolean initialized;
    private static Field skyRendererField;
    private static Field featureRendererField;
    private static Method clearMethod;

    private SkyboxifyCompat() {
    }

    public static void clearStaleSkySubmits() {
        if (!initialized) {
            initialized = true;
            init();
        }
        if (clearMethod == null) return;

        try {
            Object skyRenderer = skyRendererField.get(Minecraft.getInstance().levelRenderer);
            if (skyRenderer == null) return;
            Object featureRenderer = featureRendererField.get(skyRenderer);
            if (featureRenderer != null) clearMethod.invoke(featureRenderer);
        } catch (ReflectiveOperationException | RuntimeException e) {
            clearMethod = null;
            Constants.LOG.warn("Skyboxify compatibility disabled", e);
        }
    }

    private static void init() {
        try {
            Class<?> featureRenderer = Class.forName("btw.lowercase.skyboxify.skybox.renderer.FeatureRenderer");
            Class<?> skyRenderer = Class.forName("net.minecraft.client.renderer.SkyRenderer");

            Field levelRendererSky = null;
            for (Field field : Minecraft.getInstance().levelRenderer.getClass().getDeclaredFields()) {
                if (field.getType() == skyRenderer) {
                    levelRendererSky = field;
                    break;
                }
            }
            if (levelRendererSky == null) return;

            Field renderer = skyRenderer.getDeclaredField("skyboxify$skyFeatureRenderer");
            Method clear = featureRenderer.getMethod("clear");
            levelRendererSky.setAccessible(true);
            renderer.setAccessible(true);

            skyRendererField = levelRendererSky;
            featureRendererField = renderer;
            clearMethod = clear;
            Constants.LOG.info("Skyboxify detected: stale sky layers will be cleared each frame");
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Skyboxify is not installed or has a different layout
        }
    }
}
