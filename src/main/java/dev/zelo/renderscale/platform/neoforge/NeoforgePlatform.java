package dev.zelo.renderscale.platform.neoforge;

//? neoforge {

/*import dev.zelo.renderscale.platform.Platform;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.VersionInfo;

public class NeoforgePlatform implements Platform {

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public Platform.ModLoader loader() {
		return ModLoader.NEOFORGE;
	}

//	@Override
//	public String mcVersion() {
//		return "";
//	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader/^? if > 1.21.8 {^/.getCurrent()/^?}^/.isProduction();
	}
}
*///?}
