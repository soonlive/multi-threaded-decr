package cn.soonlive.multi_threaded_decr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class MultiThreadedDecrApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiThreadedDecrApplication.class, args);
    }
}
