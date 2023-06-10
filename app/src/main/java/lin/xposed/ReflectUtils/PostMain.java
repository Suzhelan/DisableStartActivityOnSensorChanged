package lin.xposed.ReflectUtils;

import android.os.Handler;
import android.os.Looper;

public class PostMain {
    public static void postMain(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static void postMain(Runnable readable, long l) {
        new Handler(Looper.getMainLooper()).postDelayed(readable, l);
    }
}
