package com.youku.java.navi.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 异步执行程序工具
 *
 */
public class AsynchExecUtil {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void execute(Runnable task) {
        executor.execute(task);
    }

    public static <T> Future<T> execute(Callable<T> caller) {
        return executor.submit(caller);
    }


}
