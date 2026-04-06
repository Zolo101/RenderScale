//? fabric {
//~ if >= 1.21.11 'AutoConfig' -> 'AutoConfigClient' {
package dev.zelo.renderscale.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.zelo.renderscale.config.RenderScaleConfig;

import me.shedaniel.autoconfig.AutoConfigClient;

/**
 * A compat layer for integrating with Mod Menu.
 */
public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> AutoConfigClient.getConfigScreen(RenderScaleConfig.class, screen).get();
    }
}

//~}
//?}