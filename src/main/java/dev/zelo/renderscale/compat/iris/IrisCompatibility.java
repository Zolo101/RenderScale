package dev.zelo.renderscale.compat.iris;

import dev.zelo.renderscale.RenderScale;
import net.irisshaders.iris.api.v0.IrisApi;

import java.lang.reflect.Method;

public class IrisCompatibility {
    public static boolean isShaderPackInUse() {
        //? forge {
        /*return RenderScale.PLATFORM.isModLoaded("oculus") && IrisApi.getInstance().isShaderPackInUse();
        *///?} else {
        return RenderScale.PLATFORM.isModLoaded("iris") && IrisApi.getInstance().isShaderPackInUse();
        //?}
    }

    public static void reloadShaders() {
        try {
            Class<?> Iris = findClass("net.irisshaders.iris.Iris");
            Method IrisReload = Iris.getMethod("reload");
            IrisReload.invoke(null);
        } catch (Exception e) {
            // OK so no iris then
        }
    }

    public static Class<?> findClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
