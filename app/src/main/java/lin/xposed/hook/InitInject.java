package lin.xposed.hook;

import android.app.Application;
import android.content.Context;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import lin.xposed.ReflectUtils.ClassUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class InitInject implements IXposedHookLoadPackage,
        IXposedHookZygoteInit,
        IXposedHookInitPackageResources {
    private static final AtomicBoolean Initialized = new AtomicBoolean();
    private static XC_LoadPackage.LoadPackageParam mLoadPackageParam;

    //初始化开始hook 本类其他方法暂时无意义
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        mLoadPackageParam = loadPackageParam;
        String packageName = loadPackageParam.packageName;
        if (!loadPackageParam.isFirstApplication) return;//通用的注入可能不需要这样的判断方式
        if (!packageName.matches(HookEnv.HostPackageName)) return;

        HookEnv.setCurrentHostAppPackageName(packageName);

        /*不考虑出现锁同步对象空的情况*/
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (Initialized.getAndSet(true)) return;
                //获取和设置全局上下文和类加载器
                HookEnv.setHostAppContext((Context) param.args[0]);
                ClassUtils.setHostClassLoader(HookEnv.getHostAppContext().getClassLoader());

                //加载hook
                HookInit.loadHook();
            }
        });
    }

    private static StartupParam mStartupParam;
    private static XC_InitPackageResources.InitPackageResourcesParam mInitPackageResourcesParam;

    public static XC_LoadPackage.LoadPackageParam getLoadPackageParam() {
        return mLoadPackageParam;
    }

    public static StartupParam getStartupParam() {
        return mStartupParam;
    }

    public static XC_InitPackageResources.InitPackageResourcesParam getInitPackageResourcesParam() {
        return mInitPackageResourcesParam;
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        mStartupParam = startupParam;
    }

    /**
     * 实现此接口可以用于添加替换res
     */
    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam initPackageResourcesParam) {
        mInitPackageResourcesParam = initPackageResourcesParam;
    }
}
