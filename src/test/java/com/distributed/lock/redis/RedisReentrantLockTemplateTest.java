package com.distributed.lock.redis;

import com.distributed.lock.Callback;
import com.distributed.redis.RedisDistributedLockTemplate;
import org.junit.Test;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by zgl
 * Date: 2017/4/23.
 * Email: gaoleizhou@gmail.com
 */
public class RedisReentrantLockTemplateTest {

    @Test
    public void testTry() throws InterruptedException {
        JedisPool jedisPool = new JedisPool("127.0.0.1", 6379);

        final RedisDistributedLockTemplate template = new RedisDistributedLockTemplate(jedisPool);

        int size = 100;
        final CountDownLatch startCountDownLatch = new CountDownLatch(1);
        final CountDownLatch endDownLatch = new CountDownLatch(size);

        for (int i = 0 ; i < size; i ++){
            new Thread(() -> {
                try {
                    //计数到达零之前, await方法一定会阻塞
                    startCountDownLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                final int sleepTime=ThreadLocalRandom.current().nextInt(5)*1000;
                template.execute("test", 5000, new Callback() {
                    public Object onGetLock() throws InterruptedException {
                        System.out.println(Thread.currentThread().getName() + ":getLock");
                        Thread.currentThread().sleep(sleepTime);
                        System.out.println(Thread.currentThread().getName() + ":sleeped:"+sleepTime);
                        endDownLatch.countDown();
                        return null;
                    }
                    public Object onTimeout() throws InterruptedException {
                        System.out.println(Thread.currentThread().getName() + ":timeout");
                        endDownLatch.countDown();
                        return null;
                    }
                });
            }).start();
        }

        startCountDownLatch.countDown();
        //100个线程开始
        //100个线程结束
        endDownLatch.await();
    }
}
