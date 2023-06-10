package lin.xposed.ReflectUtils;

import dalvik.system.DexFile;
import de.robv.android.xposed.XposedHelpers;

import java.io.IOException;
import java.util.*;

public class ClassUtils {
    private static final Map<String, Class<?>> classMap = new HashMap<>();
    public static ClassLoader moduleLoader;//模块类加载器
    private static ClassLoader hostLoader;//宿主类加载器

    public static boolean isConflictingClass(String name) {
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

    /**
     * 获取实现了此接口的所有类
     *
     * @param apkPath        位于/data/app/的base.apk路径
     * @param interfaceClass 接口类
     */
    public static List<Class<?>> getAllClassByInterface(String apkPath, Class<?> interfaceClass) {
        List<Class<?>> returnClassList = new ArrayList<>(); //返回结果
        //如果不是一个接口，则不做处理
        if (interfaceClass.isInterface()) {
            String packageName = interfaceClass.getPackage().getName(); //获得当前的包名
            try {
                List<Class<?>> allClass = getClasses(apkPath, packageName); //获得当前包下以及子包下的所有类
                //判断是否是同一个接口
                for (Class<?> aClass : allClass) {
                    if (interfaceClass.isAssignableFrom(aClass)) { //判断是不是一个接口
                        if (!interfaceClass.equals(aClass)) { //本身不加进去
                            returnClassList.add(aClass);
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        return returnClassList;
    }

    //从一个包中查找出所有的类，在jar包中不能查找
    private static List<Class<?>> getClasses(String apkPath, String packageName)
            throws ClassNotFoundException, IOException {
        List<String> dexFileClassNames = getDexFileClassNames(apkPath, packageName);
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (String s : dexFileClassNames) {
            try {
                Class<?> scanClass = Class.forName(s);
                classes.add(scanClass);
            } catch (Exception ignored) {
            }
        }
        return classes;
    }

    public static List<String> getDexFileClassNames(String apkPath, String packageName) throws IOException {
        DexFile df = new DexFile(apkPath);//通过DexFile查找当前的APK中可执行文件
        Enumeration<String> enumeration = df.entries();//获取df中的元素
        // 这里包含了所有可执行的类名 该类名包含了包名+类名的方式
        List<String> classes = new ArrayList<>();
        while (enumeration.hasMoreElements()) {//遍历
            String className = enumeration.nextElement();
            //是packageName的包 非常用包
            if (className.startsWith(packageName) && !isConflictingClass(className)) {
                classes.add(className);
            }
        }
        df.close();
        return classes;
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
