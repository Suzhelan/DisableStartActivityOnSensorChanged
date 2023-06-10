package lin.xposed.hook;

import android.app.Application;
import android.content.Context;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.main.HookEnv;

import java.util.concurrent.atomic.AtomicBoolean;

public class InitInject implements IXposedHookLoadPackage,
        IXposedHookZygoteInit,
        IXposedHookInitPackageResources {
    private static final AtomicBoolean Initialized = new AtomicBoolean();
    private static XC_LoadPackage.LoadPackageParam mLoadPackageParam;
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

    //初始化开始hook
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        mLoadPackageParam = loadPackageParam;
        String packageName = loadPackageParam.packageName;
        if (!loadPackageParam.isFirstApplication) return;//通用的注入可能不需要这样的判断方式
        if (!packageName.matches(HookEnv.HostPackageName)) return;
        //防止重复挂钩命中
        if (Initialized.getAndSet(true)) return;

        HookEnv.setCurrentHostAppPackageName(packageName);
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                //获取类加载器和全局的上下文
                HookEnv.setHostAppContext((Context) param.args[0]);
                ClassUtils.setHostClassLoader(HookEnv.getHostAppContext().getClassLoader());
                //加载hook
                HookInit.loadHook();
            }
        });
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        mStartupParam = startupParam;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam initPackageResourcesParam) {
        mInitPackageResourcesParam = initPackageResourcesParam;
    }
}
