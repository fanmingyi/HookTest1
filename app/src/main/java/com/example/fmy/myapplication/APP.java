package com.example.fmy.myapplication;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.os.Handler.Callback;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;


public class APP extends Application {
    private static final String KEY_EXTRA_TARGET_INTENT = "EXTRA_TARGET_INTENT";
    private static final String TAG = "APP";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        try {
            Class<?> ams_class = Class.forName("android.app.ActivityManagerNative");
            Field gDefault = ams_class.getDeclaredField("gDefault");
            gDefault.setAccessible(true);

            Object gDefault_instance = gDefault.get(null);

            Class<?> singleton_class = Class.forName("android.util.Singleton");

            Field mInstance = singleton_class.getDeclaredField("mInstance");
            mInstance.setAccessible(true);
            final Object mInstance_instance = mInstance.get(gDefault_instance);

            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), mInstance_instance.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    if (method.getName().equals("startActivity")) {

                        for (int i = 0; i < args.length; i++) {
                            Object arg = args[i];
                            if (arg instanceof Intent) {
                                Intent intent = new Intent();
//                                Intent arg_intent = (Intent) arg;

                                ComponentName componentName = new ComponentName("com.example.fmy.myapplication", PlaceholdActivity.class.getName());
                                intent.setComponent(componentName);
                                intent.putExtra(KEY_EXTRA_TARGET_INTENT, ((Intent) arg));
                                args[i] = intent;
                                return method.invoke(mInstance_instance, args);
                            }
                        }

                    }

                    return method.invoke(mInstance_instance, args);
                }
            });
//            // gDefault是一个 android.util.Singleton对象; 我们取出这个单例里面的字段

            mInstance.set(gDefault_instance, proxyInstance);

            //过检验结束
            Class<?> activityThread_class = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThread_field = activityThread_class.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThread_field.setAccessible(true);
            Object activityThread_instance = sCurrentActivityThread_field.get(null);

            Field mH_field = activityThread_class.getDeclaredField("mH");
            mH_field.setAccessible(true);
            Object mH_insance = mH_field.get(activityThread_instance);

            Field mCallback_field = Handler.class.getDeclaredField("mCallback");


            mCallback_field.setAccessible(true);

            mCallback_field.set(mH_insance, new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == 100) {
                        Object obj = msg.obj;
                        try {
                            Field intent_field = obj.getClass().getDeclaredField("intent");
                            intent_field.setAccessible(true);
                            Intent intent = (Intent) intent_field.get(obj);

                            Intent target_intent = intent.getParcelableExtra(KEY_EXTRA_TARGET_INTENT);

                            if (target_intent != null) {
                                intent.setComponent(target_intent.getComponent());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    return false;

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
