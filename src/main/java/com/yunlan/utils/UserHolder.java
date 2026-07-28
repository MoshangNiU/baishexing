package com.yunlan.utils;

public class UserHolder {
    private static final ThreadLocal<Long> tl = new ThreadLocal<>();

    public static void set(Long userId) {
        tl.set(userId);
    }

    public static Long get() {
        return tl.get();
    }

    public static void remove() {
        tl.remove();
    }
}
