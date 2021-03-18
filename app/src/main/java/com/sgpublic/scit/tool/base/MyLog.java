package com.sgpublic.scit.tool.base;

import android.util.Log;

import com.sgpublic.scit.tool.BuildConfig;

import java.util.HashMap;
import java.util.Map;

public class MyLog {
    private final static boolean out = BuildConfig.DEBUG;

    public static void v(Object message){
        if (out){
            doLog(new DoLogSimplify() {
                @Override
                public void onLog(String tag, String message) {
                    Log.v(tag, message);
                }
            }, message);
        }
    }

    public static void v(Object message, Throwable e){
        if (out){
            doLog(new DoLog() {
                @Override
                public void onLog(String tag, String message, Throwable e) {
                    Log.v(tag, message);
                }
            }, message, e);
        }
    }

    public static void d(Object message){
        if (out){
            doLog(new DoLogSimplify() {
                @Override
                public void onLog(String tag, String message) {
                    Log.d(tag, message);
                }
            }, message);
        }
    }

    public static void d(Object message, Throwable e){
        if (out){
            doLog(new DoLog() {
                @Override
                public void onLog(String tag, String message, Throwable e) {
                    Log.d(tag, message);
                }
            }, message, e);
        }
    }

    public static void i(Object message){
        if (out){
            doLog(new DoLogSimplify() {
                @Override
                public void onLog(String tag, String message) {
                    Log.w(tag, message);
                }
            }, message);
        }
    }

    public static void i(Object message, Throwable e){
        if (out){
            doLog(new DoLog() {
                @Override
                public void onLog(String tag, String message, Throwable e) {
                    Log.i(tag, message);
                }
            }, message, e);
        }
    }

    public static void w(Object message){
        if (out){
            doLog(new DoLogSimplify() {
                @Override
                public void onLog(String tag, String message) {
                    Log.w(tag, message);
                }
            }, message);
        }
    }

    public static void w(Object message, Throwable e){
        if (out){
            doLog(new DoLog() {
                @Override
                public void onLog(String tag, String message, Throwable e) {
                    Log.w(tag, message);
                }
            }, message, e);
        }
    }

    public static void e(Object message){
        if (out){
            doLog(new DoLogSimplify() {
                @Override
                public void onLog(String tag, String message) {
                    Log.e(tag, message);
                }
            }, message);
        }
    }

    public static void e(Object message, Throwable e){
        if (out){
            doLog(new DoLog() {
                @Override
                public void onLog(String tag, String message, Throwable e) {
                    Log.e(tag, message);
                }
            }, message, e);
        }
    }

    private static void doLog(DoLogSimplify doLog, Object message){
        StackTraceElement ste = new Throwable().getStackTrace()[2];
        String tag_name = "MyLog (" + ste.getFileName() + ":" + ste.getLineNumber() + ")";
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

    private static void doLog(DoLog doLog, Object message, Throwable e){
        StackTraceElement ste = new Throwable().getStackTrace()[2];
        String tag_name = "MyLog (" + ste.getFileName() + ":" + ste.getLineNumber() + ")";
        String message_string = String.valueOf(message);
        if (message_string.length() > 1024){
            int index;
            for (index = 0; index < message_string.length() - 1024; index = index + 1024){
                String out = message_string.substring(index, index + 1024);
                doLog.onLog(tag_name, out, e);
            }
            doLog.onLog(tag_name, message_string.substring(index), e);
        } else {
            doLog.onLog(tag_name, message_string + "，[" + e.getClass().getCanonicalName() + "] " + e.getLocalizedMessage(), e);
        }
    }

    private interface DoLogSimplify {
        void onLog(String tag, String message);
    }

    private interface DoLog {
        void onLog(String tag, String message, Throwable e);
    }
}
