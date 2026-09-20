//? >=26.2 {
package dev.zelo.renderscale.mixin;

//? >=26.3 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanPhysicalDevice;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
//?} else {
/*import com.mojang.blaze3d.vulkan.VulkanBackend;
import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import com.mojang.blaze3d.vulkan.init.VulkanFeature;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import java.util.Collection;
*///?}
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import dev.zelo.renderscale.compat.vulkan.VulkanFsrSupport;
import org.lwjgl.vulkan.VkDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(VulkanBackend.class)
@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
public abstract class MixinVulkanBackend {
    //? >=26.3 {
    @ModifyExpressionValue(method = "createDevice", at = @At(value = "INVOKE",
            target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanFeatureSets;optionalFeatureSets()Ljava/util/Set;"))
    private Set<FeatureSet> renderScale$optionalFp16(Set<FeatureSet> original) {
        Set<FeatureSet> features = new HashSet<>(original);
        features.add(VulkanFsrSupport.FP16);
        return features;
    }

    @Inject(method = "createDevice(Lcom/mojang/renderpearl/backend/vulkan/init/FeatureSet;Lcom/mojang/renderpearl/backend/vulkan/VulkanPhysicalDevice;)Lorg/lwjgl/vulkan/VkDevice;", at = @At("RETURN"))
    private static void renderScale$recordFp16(FeatureSet features, VulkanPhysicalDevice physicalDevice,
                                               CallbackInfoReturnable<VkDevice> cir) {
        VulkanFsrSupport.recordEnabled(cir.getReturnValue(), features.contains(VulkanFsrSupport.FP16));
    }
    //?} else {
    /*@Inject(method = "createDevice(Ljava/util/Collection;Lcom/mojang/blaze3d/vulkan/VulkanPhysicalDevice;Ljava/util/Set;)Lorg/lwjgl/vulkan/VkDevice;", at = @At("HEAD"))
    private static void renderScale$optionalFp16(Collection<String> extensions, VulkanPhysicalDevice physicalDevice,
                                                Set<VulkanFeature> features, CallbackInfoReturnable<VkDevice> cir) {
        // 26.2 predates optional FeatureSets. Query the selected GPU and add only
        // supported FP16 arithmetic to its per-device feature set.
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var fp16 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default();
            var supported = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default().pNext(fp16);
            VK11.vkGetPhysicalDeviceFeatures2(physicalDevice.vkPhysicalDevice(), supported);
            if (fp16.shaderFloat16()) features.add(VulkanFsrSupport.SHADER_FLOAT16);
        }
    }

    @Inject(method = "createDevice(Ljava/util/Collection;Lcom/mojang/blaze3d/vulkan/VulkanPhysicalDevice;Ljava/util/Set;)Lorg/lwjgl/vulkan/VkDevice;", at = @At("RETURN"))
    private static void renderScale$recordFp16(Collection<String> extensions, VulkanPhysicalDevice physicalDevice,
                                               Set<VulkanFeature> features, CallbackInfoReturnable<VkDevice> cir) {
        VulkanFsrSupport.recordEnabled(cir.getReturnValue(), features.contains(VulkanFsrSupport.SHADER_FLOAT16));
    }
    *///?}
}
//?}
