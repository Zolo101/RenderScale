package dev.zelo.renderscale.compat.iris;

import java.lang.reflect.Method;

public class IrisCompatibility {
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
