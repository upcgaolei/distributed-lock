package com.distributed.redis;

import com.distributed.common.Constants;
import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static com.distributed.common.Arguments.isNull;
import static com.distributed.common.Arguments.notNull;
import static com.distributed.common.Randoms.randomId;

/**
 * Created by zgl
 * Date: 2017/4/24.
 * Email: gaoleizhou@gmail.com
 */
class RedisLockInternals {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RedisLockInternals.class);

    /**
     * Redis连接池
     */
    private JedisPool jedisPool;

    RedisLockInternals(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 尝试获得锁
     * @param lockName 锁名称
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @return 锁Key对应的Value
     */
    String tryRedisLock(String lockName, long timeout, TimeUnit timeUnit) {
        final long startMillis = System.currentTimeMillis();
        final Long millisToWait = timeUnit.toMillis(timeout);
        String lockValue = null;
        while (isNull(lockValue)) {
            lockValue = createRedisKey(lockName);
            if(notNull(lockValue)){
                break;
            }

            //执行时间 > 重试等待时间 + 超时时间
            if(System.currentTimeMillis() - startMillis - Constants.RETRY_AWAIT > millisToWait) {
                break;
            }

            //300毫秒后线程自动从中断中恢复
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(Constants.RETRY_AWAIT));
        }
        return lockValue;
    }

    /**
     * 为锁创建对应的key/value
     * @param lockName 锁名称
     * @return Key对应的Value
     */
    private String createRedisKey(String lockName) {
        Jedis jedis = null;
        try {
            String value = lockName + randomId(1);

            jedis = jedisPool.getResource();

            String luaScript = ""
                    + "\nlocal r = tonumber(redis.call('SETNX', KEYS[1], ARGV[1]));"
                    + "\nredis.call('PEXPIRE', KEYS[1], ARGV[2]);"
                    + "\nreturn r";

            List<String> keys = Lists.newArrayList();
            keys.add(lockName);

            List<String> args = Lists.newArrayList();
            args.add(value);
            args.add(String.valueOf(Constants.LOCK_TIMEOUT));

            Long ret = (Long) jedis.eval(luaScript, keys, args);

            if(Objects.equals(ret, 1L)) return value;
        }finally {
            if(notNull(jedis)) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 释放锁
     * @param key 锁对应的key
     * @param value 锁对应的value
     */
    void unlockRedisLock(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            String luaScript=""
                    +"\nlocal v = redis.call('GET', KEYS[1]);"
                    +"\nlocal r= 0;"
                    +"\nif v == ARGV[1] then"
                    +"\nr =redis.call('DEL',KEYS[1]);"
                    +"\nend"
                    +"\nreturn r";

            List<String> keys = Lists.newArrayList();
            keys.add(key);

            List<String> args = Lists.newArrayList();
            args.add(value);

            jedis.eval(luaScript, keys, args);
        } finally {
            if(notNull(jedis)) jedis.close();
        }
    }

}
