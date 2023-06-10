package lin.xposed.ReflectUtils;

import de.robv.android.xposed.XposedHelpers;

import java.util.HashMap;
import java.util.Map;

public class ClassUtils {
    private static final Map<String, Class<?>> classMap = new HashMap<>();
    public static ClassLoader moduleLoader;//模块类加载器
    private static ClassLoader hostLoader;//宿主应用类加载器

    public static boolean isCommonClass(String name) {
        return name.startsWith("androidx.") || name.startsWith("android.") ||
                name.startsWith("kotlin.") || name.startsWith("kotlinx.")
                || name.startsWith("com.tencent.mmkv.")
                || name.startsWith("com.android.tools.r8.")
                || name.startsWith("com.google.android.")
                || name.startsWith("com.google.gson.")
                || name.startsWith("com.google.common.")
                || name.startsWith("com.microsoft.appcenter.")
                || name.startsWith("org.intellij.lang.annotations.")
                || name.startsWith("org.jetbrains.annotations.");
    }


    public static Class<?> getClass(String className) {
        //类缓存里有这个类就直接返回
        Class<?> clazz = classMap.get(className);
        if (clazz != null) {
            return clazz;
        }
        try {
            if (className.equals("void"))
                clazz = void.class;
            else {
                clazz = XposedHelpers.findClass(className, hostLoader);
            }
            classMap.put(className, clazz);
            return clazz;
        } catch (Throwable throwable) {
            throw new RuntimeException("没有找到类: " + className);
        }
    }

    public static Class<?> loadClass(ClassLoader loader, String className) {
        //类缓存里有这个类就直接返回
        Class<?> clazz = classMap.get(className);
        if (clazz != null) {
            return clazz;
        }
        if (className.equals("void"))
            clazz = void.class;
        else {
            clazz = XposedHelpers.findClass(className, loader);
        }
        classMap.put(className, clazz);
        return clazz;
    }



    public static void setHostClassLoader(ClassLoader loader) {
        hostLoader = loader;
    }

    public static ClassLoader getHostLoader() {
        return hostLoader;
    }

    public static ClassLoader getModuleLoader() {
        return moduleLoader;
    }

    public static void setModuleLoader(ClassLoader loader) {
        moduleLoader = loader;
    }
}
