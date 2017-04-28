package com.distributed.lock;

import java.util.concurrent.TimeUnit;

/**
 * Created by zgl
 * Date: 2017/4/23.
 * Email: gaoleizhou@gmail.com
 * Desc: 分布式可重入锁
 */
public interface DistributedReentrantLock {
    /**
     * 获得锁
     * @param timeout 时间
     * @param unit 时间单位
     * @see java.util.concurrent.TimeUnit
     * @return 是否获得锁 true/false
     * @throws InterruptedException
     */
    boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 释放锁
     */
    void unlock();
}
