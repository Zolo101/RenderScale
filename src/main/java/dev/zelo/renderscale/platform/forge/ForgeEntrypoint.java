//? if forge {
/*package dev.zelo.renderscale.platform.forge;

import dev.zelo.renderscale.Constants;
import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod(Constants.MOD_ID)
public class ForgeEntrypoint {
    private static final KeyMapping KEY_BINDING = new KeyMapping("key.renderscale.options", GLFW.GLFW_KEY_O, "key.renderscale.category");

    public ForgeEntrypoint() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::registerBindings);
        modBus.addListener(this::registerReloadManager);

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(ForgeEntrypoint::getConfigScreen)
        );

        MinecraftForge.EVENT_BUS.addListener(this::onWorldRenderStart);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTickEnd);
    }

    public static Screen getConfigScreen(Screen parent) {
        return AutoConfig.getConfigScreen(RenderScaleConfig.class, parent).get();
    }

    public void onWorldRenderStart(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        if (!RenderScale.getInstance().hasRun) {
            RenderScale.getInstance().resizeRenderTarget();
            RenderScale.getInstance().hasRun = true;
        }
    }

    public void onClientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null && RenderScale.getInstance().hasRun) {
            RenderScale.getInstance().hasRun = false;
        }

        while (KEY_BINDING.consumeClick()) {
            minecraft.setScreen(getConfigScreen(minecraft.screen));
        }
    }

    public static void onDatapackReload() {
//        AutoConfig.getConfigHolder(RenderScaleConfig.class).load();
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        RenderScale.init(Minecraft.getInstance());
    }

    public void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(KEY_BINDING);
    }

    public void registerReloadManager(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) c -> ForgeEntrypoint.onDatapackReload());
    }
}
*///?}
