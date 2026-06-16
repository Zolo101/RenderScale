package dev.zelo.renderscale.platform.fabric;
//? fabric {
//~ if >= 1.21.11 'AutoConfig' -> 'AutoConfigClient' {
//~ if >= 1.21.10 'START' -> 'START_MAIN' {

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? >= 26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} else {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
*///?}

// can they decide??
//? >= 26 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
//?} else >= 1.21.10 {
/*import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
*///?} else
//import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.KeyMapping;

import org.lwjgl.glfw.GLFW;

//? <= 1.21.6
//import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

//? >= 1.21.6
import net.minecraft.resources.Identifier;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {
    private static KeyMapping keyBinding;
    @Override
    public void onInitializeClient() {
        RenderScale.init();
        //? >= 1.21.9
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("renderscale", "category"));
        //? >= 26.1 {
        keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.renderscale.options", /*? < 26.2 {*/ /*GLFW.GLFW_KEY_O *//*?} else {*/ GLFW.GLFW_KEY_U /*?}*/, category));
        //?} else {
        /*keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.renderscale.options", GLFW.GLFW_KEY_O, /^? >= 1.21.9 {^/ category /^?} else {^/ /^"key.renderscale.category" ^//^?}^/));
         *///?}

        /*? >= 26 {*/ LevelRenderEvents /*?} else {*/ /*WorldRenderEvents *//*?}*/.START_MAIN.register(worldRenderContext -> {
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
                //? > 26.1 {
                minecraft.gui.setScreen(AutoConfigClient.getConfigScreen(RenderScaleConfig.class, minecraft.gui.screen()).get());
                //?} else
                //minecraft.setScreen(AutoConfigClient.getConfigScreen(RenderScaleConfig.class, minecraft.screen).get());
            }
        });
    }
}
//~}
//~}
//?}