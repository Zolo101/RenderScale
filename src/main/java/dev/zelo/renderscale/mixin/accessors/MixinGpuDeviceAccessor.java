//? >=26.2 {
package dev.zelo.renderscale.mixin.accessors;

//? >=26.3 {
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
//?} else {
/*import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
*///?}
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? >=26.3 {
@Mixin(FrontendGpuDevice.class)
//?} else
//@Mixin(GpuDevice.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public interface MixinGpuDeviceAccessor {
    @Accessor("backend")
    GpuDeviceBackend renderScale$getBackend();
}
//?}
