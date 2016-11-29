package cn.soonlive.multi_threaded_decr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

/**
 * Created by Xin on 7/31/16.
 */
@Configuration
@EnableCaching(proxyTargetClass = true)
public class CacheConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.host}")
    String redisHost;

    @Value("${spring.redis.port}")
    int redisPort;


    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();

        redisConnectionFactory.setDatabase(10);
        // Defaults
        redisConnectionFactory.setHostName("127.0.0.1");
        redisConnectionFactory.setPort(6379);
        return redisConnectionFactory;
    }

//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        return jedisConnectionFactory();
//    }

//  @Bean("settingRedisTemplate")
//  public RedisTemplate<String, String> settingRedisTemplate() {
////    LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisHost, redisPort);
////    connectionFactory.setDatabase(10);
////    connectionFactory.afterPropertiesSet();
//
//    StringRedisTemplate redisTemplate = new StringRedisTemplate();
//    redisTemplate.setConnectionFactory(redisConnectionFactory());
//    redisTemplate.setEnableTransactionSupport(true);
//    return redisTemplate;
//  }

}
