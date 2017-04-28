package com.distributed.lock;

/**
 * Created by zgl
 * Date: 2017/4/23.
 * Email: gaoleizhou@gmail.com
 * Desc: 分布式锁模板类
 */
public interface DistributedLockTemplate {
    /**
     * 对接接口方法
     * @param lockName 锁id(对应业务唯一ID)
     * @param timeout 单位毫秒
     * @param callback 回调函数
     * @return 返回结果
     */
    Object execute(String lockName, int timeout, Callback callback);
}
