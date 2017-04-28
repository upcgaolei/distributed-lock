package com.distributed.lock.redis;

import com.distributed.lock.Callback;
import com.distributed.redis.RedisDistributedLockTemplate;
import com.distributed.redis.RedisReentrantLock;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

/**
 * Created by zgl
 * Date: 2017/4/23.
 * Email: gaoleizhou@gmail.com
 */
public class RedisReentrantLockTest {

    private JedisPool jedisPool;

    @Before
    public void init() {
        jedisPool = new JedisPool("127.0.0.1", 6379);
    }

    @Test
    public void testRedisReentrantLock() {
        RedisReentrantLock lock = new RedisReentrantLock(jedisPool, "订单流水号");
        try {
            if(lock.tryLock(5000L, TimeUnit.MILLISECONDS)) {
                System.out.println("生成订单流水");
            } else {
                System.out.println("超时");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }

    @Test
    public void testRedisDistributedLockTemplate(){
        final RedisDistributedLockTemplate template=new RedisDistributedLockTemplate(jedisPool);
        template.execute("订单流水号", 5000, new Callback() {
            @Override
            public Object onGetLock() throws InterruptedException {
                System.out.println("生成订单流水");
                return null;
            }

            @Override
            public Object onTimeout() throws InterruptedException {
                System.out.println("超时");
                return null;
            }
        });
    }
}
