package com.yunlan.utils;

public class AdminUserHolder {
    private static final ThreadLocal<Long> ADMIN_ID = new ThreadLocal<>();

    public static void set(Long adminId) {
        ADMIN_ID.set(adminId);
    }

    public static Long get() {
        return ADMIN_ID.get();
    }

    public static void remove() {
        ADMIN_ID.remove();
    }
}
