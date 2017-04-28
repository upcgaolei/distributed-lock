package io.redis;

import io.redis.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 周高磊
 * Date: 2017/4/28.
 * Email: gaoleizhou@gmail.com
 * Desc:
 */
@SpringBootApplication // same as @Configuration @EnableAutoConfiguration @ComponentScan
@RestController
public class Application {
    @Autowired
    private RedisClient redisClient;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @RequestMapping(value = "/set", method = RequestMethod.GET)
    public String set(String key, String value) throws Exception{
        redisClient.set(key, value);
        return "success";
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public String get(String key) throws Exception {
        return redisClient.get(key);
    }
}
