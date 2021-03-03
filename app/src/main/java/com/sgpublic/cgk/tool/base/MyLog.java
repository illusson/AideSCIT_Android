package com.sgpublic.cgk.tool.base;

import android.util.Log;

import com.sgpublic.cgk.tool.BuildConfig;

import java.util.HashMap;
import java.util.Map;

public class MyLog {
    private final static boolean out = BuildConfig.DEBUG;
    private final static Map<String, Boolean> filter = new HashMap<>();

    public static void setup(){
        filter.put("Login", true);
    }

    public static void v(Class<?> tag, Object message){
        if (out){
            doLog(new DoLogSimplify() {
                @Override
                public void onLog(String tag, String message) {
                    Log.v(tag, message);
                }
            }, tag, message);
        }
    }

    public static void v(Class<?> tag, Object message, Throwable e){
        if (out){
            doLog(new DoLog() {
                @Override
                public void onLog(String tag, String message, Throwable e) {
                    Log.v(tag, message);
                }
            }, tag, message, e);
        }
    }

    public static void d(Class<?> tag, Object message){
        if (out){
            doLog(new DoLogSimplify() {
                @Override
                public void onLog(String tag, String message) {
                    Log.d(tag, message);
                }
            }, tag, message);
        }
    }

    public static void d(Class<?> tag, Object message, Throwable e){
        if (out){
            doLog(new DoLog() {
                @Override
                public void onLog(String tag, String message, Throwable e) {
                    Log.d(tag, message);
                }
            }, tag, message, e);
        }
    }

    public static void i(Class<?> tag, Object message){
        if (out){
            doLog(new DoLogSimplify() {
                @Override
                public void onLog(String tag, String message) {
                    Log.w(tag, message);
                }
            }, tag, message);
        }
    }

    public static void i(Class<?> tag, Object message, Throwable e){
        if (out){
            doLog(new DoLog() {
                @Override
                public void onLog(String tag, String message, Throwable e) {
                    Log.i(tag, message);
                }
            }, tag, message, e);
        }
    }

    public static void w(Class<?> tag, Object message){
        if (out){
            doLog(new DoLogSimplify() {
                @Override
                public void onLog(String tag, String message) {
                    Log.w(tag, message);
                }
            }, tag, message);
        }
    }

    public static void w(Class<?> tag, Object message, Throwable e){
        if (out){
            doLog(new DoLog() {
                @Override
                public void onLog(String tag, String message, Throwable e) {
                    Log.w(tag, message);
                }
            }, tag, message, e);
        }
    }

    public static void e(Class<?> tag, Object message){
        if (out){
            doLog(new DoLogSimplify() {
                @Override
                public void onLog(String tag, String message) {
                    Log.e(tag, message);
                }
            }, tag, message);
        }
    }

    public static void e(Class<?> tag, Object message, Throwable e){
        if (out){
            doLog(new DoLog() {
                @Override
                public void onLog(String tag, String message, Throwable e) {
                    Log.e(tag, message);
                }
            }, tag, message, e);
        }
    }

    private static void doLog(DoLogSimplify doLog, Class<?> tag, Object message){
        String tag_name = tag.getSimpleName();
        if (!filter.getOrDefault(tag_name, false)){
            return;
        }
        tag_name = "MyLog -> " + tag_name;
        String message_string = String.valueOf(message);
        if (message_string.length() > 1024){
            int index;
            for (index = 0; index < message_string.length() - 1024; index = index + 1024){
                String out = message_string.substring(index, index + 1024);
                doLog.onLog(tag_name, out);
            }
            doLog.onLog(tag_name, message_string.substring(index));
        } else {
            doLog.onLog(tag_name, message_string);
        }
    }

    private static void doLog(DoLog doLog, Class<?> tag, Object message, Throwable e){
        String tag_name = tag.getSimpleName();
        if (!filter.getOrDefault(tag_name, false)){
            return;
        }
        String message_string = String.valueOf(message);
        if (message_string.length() > 1024){
            int index;
            for (index = 0; index < message_string.length() - 1024; index = index + 1024){
                String out = message_string.substring(index, index + 1024);
                doLog.onLog(tag_name, out, e);
            }
            doLog.onLog(tag_name, message_string.substring(index), e);
        } else {
            doLog.onLog(tag_name, message_string, e);
        }
    }

    private interface DoLogSimplify {
        void onLog(String tag, String message);
    }

    private interface DoLog {
        void onLog(String tag, String message, Throwable e);
    }
}
