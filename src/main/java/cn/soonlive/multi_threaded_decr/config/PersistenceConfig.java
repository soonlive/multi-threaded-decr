package cn.soonlive.multi_threaded_decr.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by Xin on 23/11/2016.
 */
@Configuration
@EnableJpaRepositories(basePackages = {
        "cn.soonlive.multi_threaded_decr.repository",
})
@EntityScan(basePackages = {
        "cn.soonlive.multi_threaded_decr.entity",
})
public class PersistenceConfig {

}
