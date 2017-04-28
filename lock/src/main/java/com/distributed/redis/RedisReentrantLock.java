package com.distributed.redis;

import com.distributed.lock.DistributedReentrantLock;
import com.google.common.collect.Maps;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.distributed.common.Arguments.notNull;

/**
 * Created by zgl
 * Date: 2017/4/24.
 * Email: gaoleizhou@gmail.com
 */
public class RedisReentrantLock implements DistributedReentrantLock {

    //线程与对应的线程上的锁的映射关系
    private final ConcurrentMap<Thread, LockData> threadData = Maps.newConcurrentMap();

    private JedisPool jedisPool;

    private RedisLockInternals internals;

    private String lockName;


    public RedisReentrantLock(JedisPool jedisPool, String lockName) {
        this.jedisPool = jedisPool;
        this.lockName = lockName;
        this.internals = new RedisLockInternals(jedisPool);
    }

    /**
     * 内部类
     */
    private static class LockData {
        final Thread owningThread;
        final String lockVal;
        final AtomicInteger lockCount = new AtomicInteger(1);

        private LockData(Thread owningThread, String lockVal) {
            this.owningThread = owningThread;
            this.lockVal = lockVal;
        }
    }

    /**
     * 尝试获得锁
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 是否获得锁 true/false
     * @throws InterruptedException
     */
    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException{
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadData.get(currentThread);
        if (notNull(lockData)) {
            //Owner Thread重入
            lockData.lockCount.incrementAndGet();
            return true;
        }
        String lockVal = internals.tryRedisLock(lockName, timeout, unit);
        if (notNull(lockVal)) {
            LockData newLockData = new LockData(currentThread, lockVal);
            threadData.put(currentThread, newLockData);
            return true;
        }
        return false;
    }

    /**
     * 释放锁
     */
    @Override
    public void unlock() {
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadData.get(currentThread);
        if ( lockData == null ) {
            throw new IllegalMonitorStateException("You do not own the lock: " + lockName);
        }
        int newLockCount = lockData.lockCount.decrementAndGet();
        if ( newLockCount > 0 ) {
            return;
        }
        if ( newLockCount < 0 ) {
            throw new IllegalMonitorStateException("Lock count has gone negative for lock: " + lockName);
        }
        try {
            internals.unlockRedisLock(lockName, lockData.lockVal);
        } finally {
            threadData.remove(currentThread);
        }
    }
}
