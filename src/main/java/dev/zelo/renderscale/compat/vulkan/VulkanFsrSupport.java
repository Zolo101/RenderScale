//? >=26.2 {
package dev.zelo.renderscale.compat.vulkan;

//? >=26.3 {
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import com.mojang.renderpearl.backend.vulkan.init.VulkanFeature;
//?} else {
/*import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanBackend;
import com.mojang.blaze3d.vulkan.init.VulkanFeature;
*///?}
import dev.zelo.renderscale.mixin.accessors.MixinGpuDeviceAccessor;
import org.lwjgl.vulkan.VkDevice;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class VulkanFsrSupport {
    // Both Vulkan backends require Vulkan 1.2. Reuse their existing feature
    // structure; no KHR extension or additional 16-bit storage feature is needed.
    public static final VulkanFeature SHADER_FLOAT16 = new VulkanFeature(
            //? >=26.3 {
            VulkanFeatureSets.VK12_FEATURES_STRUCT,
            //?} else
            //VulkanBackend.VK12_FEATURES_STRUCT,
            "shaderFloat16", org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features.SHADERFLOAT16);

    //? >=26.3 {
    public static final FeatureSet FP16 = new FeatureSet("RenderScale FSR1 FP16", Set.of(), Set.of(SHADER_FLOAT16));
    //?}

    // Track successful logical-device creation, not just physical-device support.
    // Weak keys avoid retaining devices after a backend switch or failed startup.
    private static final Map<VkDevice, Boolean> ENABLED = Collections.synchronizedMap(new WeakHashMap<>());

    private VulkanFsrSupport() {}

    public static void recordEnabled(VkDevice device, boolean enabled) {
        ENABLED.put(device, enabled);
    }

    public static boolean isEnabled(GpuDevice device) {
        return device instanceof MixinGpuDeviceAccessor accessor
                && accessor.renderScale$getBackend() instanceof VulkanDevice vulkan
                && Boolean.TRUE.equals(ENABLED.get(vulkan.vkDevice()));
    }
}
//?}
