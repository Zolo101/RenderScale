//~ if >= 1.21.11 'AutoConfig' -> 'AutoConfigClient' {
//? neoforge {

/*package dev.zelo.renderscale.platform.neoforge;

//? > 26.1
import net.minecraft.client.gui.Gui;

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
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
//import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import com.mojang.blaze3d.platform.InputConstants;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class NeoforgeClientEntrypoint {
    // TODO: Consider using Lazy? (https://docs.neoforged.net/docs/misc/keymappings/#checking-a-keymapping)
    private static KeyMapping keyBinding;
    //? >= 1.21.9
    private static KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("renderscale", "category"));


    public NeoforgeClientEntrypoint(IEventBus eventBus, ModContainer modContainer) {
        //? >= 26.1 {
        keyBinding = new KeyMapping("key.renderscale.options", /^? < 26.2 {^/ /^InputConstants.KEY_O ^//^?} else {^/ InputConstants.KEY_U /^?}^/, category);
        //?} else {
        /^keyBinding = new KeyMapping("key.renderscale.options", InputConstants.KEY_O, /^¹? >= 1.21.9 {¹^/ category /^¹?} else {¹^/ /^¹"key.renderscale.category" ¹^//^¹?}¹^/);
         ^///?}

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, screen) -> NeoforgeClientEntrypoint.getConfigScreen(screen));

        eventBus.addListener(this::onClientSetup);
        eventBus.addListener(this::registerBindings);

        NeoForge.EVENT_BUS.addListener(this::onWorldRenderStart);
        NeoForge.EVENT_BUS.addListener(this::onClientTickEnd);
    }

    public static Screen getConfigScreen(Screen parent) {
        return AutoConfigClient.getConfigScreen(RenderScaleConfig.class, parent).get();
    }

    private static RenderScale getOrCreateRenderScale() {
        RenderScale renderScale = RenderScale.getInstance();
        if (renderScale == null) {
            RenderScale.init(Minecraft.getInstance());
            renderScale = RenderScale.getInstance();
        }
        return renderScale;
    }

    //? >= 1.21.10 {
    public void onWorldRenderStart(RenderLevelStageEvent.AfterLevel event) {
//        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
        RenderScale renderScale = getOrCreateRenderScale();
        if (!renderScale.hasRun) {
            renderScale.resizeRenderTarget();
            renderScale.hasRun = true;
        }
//        }
    }
    //? } else {
    /^public void onWorldRenderStart(RenderLevelStageEvent event) {
//        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            RenderScale renderScale = getOrCreateRenderScale();
            if (!renderScale.hasRun) {
                renderScale.resizeRenderTarget();
                renderScale.hasRun = true;
            }
//        }
    }
    ^///? }

    public void onClientTickEnd(ClientTickEvent.Post event) {
        RenderScale renderScale = getOrCreateRenderScale();
        if (Minecraft.getInstance().level == null && renderScale.hasRun) {
            renderScale.hasRun = false;
        }

        //? <1.21.4
        //dev.zelo.renderscale.gametest.RenderScaleAutoTest.INSTANCE.tick(Minecraft.getInstance());

        while (keyBinding.consumeClick()) {
            //? > 26.1 {
            Gui gui = Minecraft.getInstance().gui;
            gui.setScreen(AutoConfigClient.getConfigScreen(RenderScaleConfig.class, gui.screen()).get());
            //?} else
            //Minecraft.getInstance().setScreen(AutoConfigClient.getConfigScreen(RenderScaleConfig.class, Minecraft.getInstance().screen).get());
        }
    }

//    public static void onDatapackReload() {
//        AutoConfigClient.getConfigHolder(RenderScaleConfig.class).load();
//    }

    public void onClientSetup(FMLClientSetupEvent event) {
        RenderScale.init(Minecraft.getInstance());
    }

    public void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(keyBinding);
    }

}
*///?}
//~}
