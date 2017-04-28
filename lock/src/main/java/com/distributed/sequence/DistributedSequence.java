package com.distributed.sequence;

/**
 * Created by zgl
 * Date: 2017/4/23.
 * Email: gaoleizhou@gmail.com
 */
public interface DistributedSequence {

    Long sequence(String sequenceName);
}
