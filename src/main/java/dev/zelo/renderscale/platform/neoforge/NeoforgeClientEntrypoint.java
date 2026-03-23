//? if neoforge {
/*package dev.zelo.renderscale.platform.neoforge;

import dev.zelo.renderscale.Constants;
import dev.zelo.renderscale.RenderScale;
import dev.zelo.renderscale.config.RenderScaleConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class NeoforgeEntrypoint {
    // TODO: Consider using Lazy? (https://docs.neoforged.net/docs/misc/keymappings/#checking-a-keymapping)
    private static KeyMapping keyBinding;
    //? if >= 1.21.9
    private static KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("renderscale", "category"));


    public NeoforgeEntrypoint(IEventBus eventBus, ModContainer modContainer) {
        KeyMapping keyBinding = new KeyMapping("key.renderscale.options", GLFW.GLFW_KEY_O, /^? >= 1.21.9 {^/ category /^?} else {^/ /^"key.renderscale.category" ^//^?}^/);

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, screen) -> NeoforgeEntrypoint.getConfigScreen(screen));

        NeoForge.EVENT_BUS.addListener(this::onWorldRenderStart);
        NeoForge.EVENT_BUS.addListener(this::onClientTickEnd);
    }

    public static Screen getConfigScreen(Screen parent) {
        return AutoConfigClient.getConfigScreen(RenderScaleConfig.class, parent).get();
    }

    public void onWorldRenderStart(RenderLevelStageEvent event) {
//        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
//            if (!RenderScale.getInstance().hasRun) {
//                RenderScale.getInstance().resizeRenderTarget();
//                RenderScale.getInstance().hasRun = true;
//            }
//        }
    }

    public void onClientTickEnd(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null && RenderScale.getInstance().hasRun) {
            RenderScale.getInstance().hasRun = false;
        }

        while (keyBinding.consumeClick()) {
            Minecraft.getInstance().setScreen(AutoConfigClient.getConfigScreen(RenderScaleConfig.class, Minecraft.getInstance().screen).get());
        }
    }

    public static void onDatapackReload() {
        AutoConfigClient.getConfigHolder(RenderScaleConfig.class).load();
    }

    //? < 1.21.11
    //@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
    //? >= 1.21.11
    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
//    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public class EventHandler {
        @SubscribeEvent
        public static void registerReloadManager(AddClientReloadListenersEvent event) {
            event.addListener(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "load_config"),
                    (ResourceManagerReloadListener) c -> NeoforgeEntrypoint.onDatapackReload());
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            RenderScale.init(Minecraft.getInstance());
        }

        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(keyBinding);
        }
    }

}
*///?}