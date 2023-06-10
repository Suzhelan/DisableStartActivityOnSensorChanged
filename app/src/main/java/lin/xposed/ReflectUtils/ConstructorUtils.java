package lin.xposed.ReflectUtils;

import java.lang.reflect.Constructor;

public class ConstructorUtils {


    public static <T> T newInstance(Class<?> clz, Object... params) throws Exception {
        Class<?>[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramTypes[i] = params[i].getClass();
        }
        return newInstance(clz, paramTypes, params);
    }

    public static Constructor<?> findConstructor(Class<?> clazz, Class<?>[] paramTypes) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        co:
        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes1 = constructor.getParameterTypes();
            if (paramTypes.length != paramTypes1.length) continue;
            for (int j = 0; j < paramTypes1.length; j++) {
                if (!CheckClassType.CheckClass(paramTypes[j], paramTypes1[j])) continue co;
            }
            constructor.setAccessible(true);
            return constructor;
        }
        return null;
    }

    public static <T> T newInstance(Class<?> clz, Class<?>[] paramTypes, Object... params) throws Exception {
        Loop:
        for (Constructor<?> con : clz.getDeclaredConstructors()) {
            Class<?>[] CheckParam = con.getParameterTypes();
            if (CheckParam.length != paramTypes.length) continue;
            for (int i = 0; i < paramTypes.length; i++) {
                if (!CheckClassType.CheckClass(CheckParam[i], paramTypes[i])) {
                    continue Loop;
                }
            }
            con.setAccessible(true);
            return (T) con.newInstance(params);
        }
        throw new RuntimeException("找不到构造方法" + clz);
    }

}
