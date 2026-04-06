package dev.zelo.renderscale.platform.fabric;

//? fabric {

import dev.zelo.renderscale.platform.Platform;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatform implements Platform {

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public ModLoader loader() {
		return ModLoader.FABRIC;
	}

    // 0.17+ fabric loader required
//	@Override
//	public String mcVersion() {
//		return FabricLoader.getInstance().getRawGameVersion();
//	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
}
//?}
