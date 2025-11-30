//? if fabric {
package dev.zelo.renderscale.loaders.fabric;

import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class FabricEntrypoint implements ClientModInitializer {
    private static KeyMapping keyBinding;
    @Override
    public void onInitializeClient() {
        RenderScale.init();
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.renderscale.options", GLFW.GLFW_KEY_O, "key.renderscale.category"));

        WorldRenderEvents.START.register(worldRenderContext -> {
            if (!RenderScale.getInstance().hasRun) {
                RenderScale.getInstance().resizeRenderTarget();
                RenderScale.getInstance().hasRun = true;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (minecraft.level == null && RenderScale.getInstance().hasRun) {
                RenderScale.getInstance().hasRun = false;
            }

            while (keyBinding.consumeClick()) {
                minecraft.setScreen(AutoConfig.getConfigScreen(RenderScaleConfig.class, minecraft.screen).get());
            }
        });
    }
}
//?}