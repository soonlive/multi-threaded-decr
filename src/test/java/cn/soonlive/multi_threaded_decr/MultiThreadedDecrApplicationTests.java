package cn.soonlive.multi_threaded_decr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableRetry
public class MultiThreadedDecrApplicationTests {

    @Test
    public void contextLoads() {
    }

}
