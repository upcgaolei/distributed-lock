package com.distributed.redis;

import com.distributed.lock.Callback;
import com.distributed.lock.DistributedLockTemplate;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

/**
 * Created by zgl
 * Date: 2017/4/23.
 * Email: gaoleizhou@gmail.com
 */
public class RedisDistributedLockTemplate implements DistributedLockTemplate {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RedisDistributedLockTemplate.class);

    private JedisPool jedisPool;

    public RedisDistributedLockTemplate(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * @param lockName 锁id(对应业务唯一ID)
     * @param timeout 超时时间,单位毫秒
     * @param callback 回调函数
     * @return 返回结果
     */
    public Object execute(String lockName, int timeout, Callback callback) {
        RedisReentrantLock distributedReentrantLock = null;
        boolean getLock = false;
        try {
            distributedReentrantLock = new RedisReentrantLock(jedisPool, lockName);
            if(distributedReentrantLock.tryLock((long) timeout, TimeUnit.MILLISECONDS)){
                getLock = true;
                return callback.onGetLock();
            } else {
                return callback.onTimeout();
            }
        }catch(InterruptedException ex){
            log.error(ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }finally {
            if(getLock) {
                distributedReentrantLock.unlock();
            }
        }
        return null;
    }
}
