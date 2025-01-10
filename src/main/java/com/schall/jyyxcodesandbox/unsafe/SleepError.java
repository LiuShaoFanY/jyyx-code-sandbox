package com.schall.jyyxcodesandbox.unsafe;

/**
 * 无限睡眠（阻塞程序执行）
 */
public class SleepError {
    public static void main(String[] args) throws InterruptedException {
        long ONE_HOME = 60 * 60 * 1000L;
        Thread.sleep(ONE_HOME);
        System.out.println("睡完啦！");
    }
}
