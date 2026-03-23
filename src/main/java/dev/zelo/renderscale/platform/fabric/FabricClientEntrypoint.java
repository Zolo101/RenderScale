package dev.zelo.renderscale.platform.fabric;
//? fabric {

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

import org.lwjgl.glfw.GLFW;

//? if <= 1.21.6
//import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

//? if >= 1.21.6
import net.minecraft.resources.Identifier;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {
    private static KeyMapping keyBinding;
    @Override
    public void onInitializeClient() {
        RenderScale.init();
        //? if >= 1.21.9
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("renderscale", "category"));
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.renderscale.options", GLFW.GLFW_KEY_O, /*? >= 1.21.9 {*/ category /*?} else {*/ /*"key.renderscale.category" *//*?}*/));

        //? if <= 1.21.6 {
        /*WorldRenderEvents.START.register(worldRenderContext -> {
            if (!RenderScale.getInstance().hasRun) {
                RenderScale.getInstance().resizeRenderTarget();
                RenderScale.getInstance().hasRun = true;
            }
        });
        *///?}

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