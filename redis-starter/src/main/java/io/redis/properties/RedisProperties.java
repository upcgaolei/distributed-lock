package io.redis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by 周高磊
 * Date: 2017/4/28.
 * Email: gaoleizhou@gmail.com
 * Desc:
 */
@Data
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {

    private String host;

    private int port;

    private int maxTotal;

    private int maxIdle;

    private int maxWaitMillis;
}
