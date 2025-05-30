package com.immortals.miniurl.context;

import com.immortals.miniurl.model.enums.DbType;

public class DbContextHolder {
    private static final ThreadLocal<DbType> contextHolder = new ThreadLocal<>();

    public static void set(DbType dbType) {
        contextHolder.set(dbType);
    }

    public static DbType get() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }
}
