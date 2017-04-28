# Distributed-Lock
基于Redis和ZooKeeper实现的分布式锁

##基于Redis实现的分布式锁(可重入)
~~~ java
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
~~~