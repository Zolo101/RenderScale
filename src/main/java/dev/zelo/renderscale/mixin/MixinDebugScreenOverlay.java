package dev.zelo.renderscale.mixin;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.RenderScale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//? >= 1.21.9 {
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
//? >= 1.21.11 {
import net.minecraft.resources.Identifier;
//?} else
//import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?} else {
/*import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;
*///?}

//? >= 1.21.9 {
@Mixin(DebugScreenEntries.class)
//?} else
//@Mixin(DebugScreenOverlay.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinDebugScreenOverlay {
    //? >= 1.21.9 {
    //? >= 1.21.11 {
    @Invoker("register")
    private static Identifier renderScale$register(Identifier id, DebugScreenEntry entry) {
        throw new AssertionError();
    }
    //?} else {
    /*@Invoker("register")
    private static Identifier renderScale$register(Identifier id, DebugScreenEntry entry) {
        throw new AssertionError();
    }
    *///?}

    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("rawtypes")
    public static Map PROFILES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void renderScale$registerDebugEntry(CallbackInfo callbackInfo) {
        DebugScreenEntry entry = new DebugScreenEntry() {
            @Override
            public void display(DebugScreenDisplayer displayer, Level level,
                    LevelChunk clientChunk, LevelChunk serverChunk) {
                RenderScale renderScale = RenderScale.getInstance();
                if (renderScale != null) {
                    displayer.addPriorityLine(String.format(Locale.ROOT, "Render scale: %.1f%% (%s)",
                            renderScale.getRenderScaleFactor() * 100.0, renderScale.getScalingMode()));
                }
            }
        };

        //? >= 1.21.11 {
        Identifier id = Identifier.fromNamespaceAndPath("renderscale", "render_scale");
        //?} else
        //Identifier id = Identifier.fromNamespaceAndPath("renderscale", "render_scale");
        renderScale$register(id, entry);

        renderScale$addToProfiles(id);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void renderScale$addToProfiles(Object id) {
        Map profiles = new HashMap(PROFILES);
        for (DebugScreenProfile profile : DebugScreenProfile.values()) {
            Map entries = new HashMap((Map) profiles.getOrDefault(profile, Map.of()));
            //? >= 1.21.11 {
            entries.put(id, DebugScreenEntryStatus.IN_OVERLAY);
            //?} else
            //entries.put(id, DebugScreenEntryStatus.IN_F3);
            profiles.put(profile, Map.copyOf(entries));
        }
        PROFILES = Map.copyOf(profiles);
    }
    //?} else {
    /*@Inject(method = "getGameInformation", at = @At("RETURN"), cancellable = true)
    private void renderScale$addScale(CallbackInfoReturnable<List<String>> callbackInfo) {
        RenderScale renderScale = RenderScale.getInstance();
        if (renderScale != null) {
            List<String> lines = new ArrayList<>(callbackInfo.getReturnValue());
            lines.add(Math.min(2, lines.size()), String.format(Locale.ROOT, "Render scale: %.1f%% (%s)",
                    renderScale.getRenderScaleFactor() * 100.0, renderScale.getScalingMode()));
            callbackInfo.setReturnValue(lines);
        }
    }
    *///?}
}
