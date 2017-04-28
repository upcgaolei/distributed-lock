package com.distributed.lock;

/**
 * Created by zgl
 * Date: 2017/4/23.
 * Email: gaoleizhou@gmail.com
 */
public interface Callback {

    /**
     * 获得锁后要做的事情
     * @return 处理结果
     * @throws InterruptedException
     */
    Object onGetLock() throws InterruptedException;

    /**
     * 超时后要做的事情
     * @return 处理结果
     * @throws InterruptedException
     */
    Object onTimeout() throws InterruptedException;
}
