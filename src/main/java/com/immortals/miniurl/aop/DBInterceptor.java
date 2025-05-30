package com.immortals.miniurl.aop;

import com.immortals.miniurl.context.DbContextHolder;
import com.immortals.miniurl.model.enums.DbType;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DBInterceptor {

    @Before("@annotation(com.immortals.miniurl.annotation.ReadOnly)")
    public void setReadOnlyDataSource() {
        DbContextHolder.set(DbType.READ);
    }

    @After("@annotation(com.immortals.miniurl.annotation.ReadOnly) || @annotation(com.immortals.miniurl.annotation.WriteOnly)")
    public void clearContext() {
        DbContextHolder.clear();
    }


    @Before("@annotation(com.immortals.miniurl.annotation.WriteOnly)")
    public void setWriteOnlyDataSource() {
        DbContextHolder.set(DbType.WRITE);
    }

}
