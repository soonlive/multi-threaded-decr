package cn.soonlive.multi_threaded_decr.service;

import cn.soonlive.multi_threaded_decr.data.ProductData;
import cn.soonlive.multi_threaded_decr.entity.Product;
import cn.soonlive.multi_threaded_decr.exception.InsufficientStockLevelException;
import cn.soonlive.multi_threaded_decr.repository.ProductRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.fail;

/**
 * Created by Xin on 25/11/2016.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductServiceTest {

    private final Logger LOG = LoggerFactory.getLogger(ProductServiceTest.class);

    @Autowired
    private ProductRepository productRepository;

    @SpyBean
    private ProductService productService;

    private int nThreads = 10;

    @Test
    public void decreaseAvailable() throws Exception {
        String productCode = "001";

        ExecutorService service = Executors.newFixedThreadPool(10);

        List<Callable<Integer>> callables = new ArrayList<>();

        for (int i = 0; i < nThreads * 2; i++) {
            Callable<Integer> task = () -> {
                Product product = productRepository.findOne(productCode);
                Integer randomInt = ThreadLocalRandom.current().nextInt(1, 10 + 1);
                Integer available = product.getAvailable();
                if (product.getAvailable() > 0) {
                    available = productService.decreaseAvailable(productCode, randomInt);
                }
                return available;
            };
            callables.add(task);
        }
        ;

        service.invokeAll(callables);
    }

    @Test
    public void syncDecreaseAvailable() throws Exception {
        String productCode = "001";

        ExecutorService service = Executors.newFixedThreadPool(10);

        List<Callable<Integer>> callables = new ArrayList<>();

        for (int i = 0; i < nThreads * 2; i++) {
            Callable<Integer> task = () -> {

                Product product = productRepository.findOne(productCode);
                Integer randomInt = ThreadLocalRandom.current().nextInt(1, 10 + 1);
                Integer available = product.getAvailable();
                if (product.getAvailable() > 0) {
                    available = productService.syncDecreaseAvailable(productCode, randomInt);
                }
                return available;
            };
            callables.add(task);
        }

        service.invokeAll(callables);
    }


    /**
     * Tests decrease available atomically by using redis CAS
     *
     * @throws Exception
     */
    @Test
    public void decreaseAvailableByCAS() throws Exception {
        Jedis jedis = new Jedis("localhost");
        jedis.set("001", "100");

        String productCode = "001";
        ExecutorService service = Executors.newFixedThreadPool(nThreads);

        List<Callable<String>> callables = new ArrayList<>();

        for (int i = 0; i < nThreads * 2; i++) {
            Callable<String> task = () -> {
                Integer available = 0;
                try {
                    Integer randomInt = ThreadLocalRandom.current().nextInt(1, 10 + 1);
                    String threadName = Thread.currentThread().getName();

                    available = productService.decreaseAvailableByCAS(productCode, randomInt);
                    if (available < 0) {
                        LOG.error("performed by {} out of stock", threadName);
                    } else {
                        LOG.info("performed by {}, {} - {} = {}", threadName, randomInt + available, randomInt, available);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return String.valueOf(available);
            };
            callables.add(task);
        }

        service.invokeAll(callables);
    }


    /**
     * Tests decrease available atomically by using redis script
     *
     * @throws Exception
     */
    @Test
    public void decreaseAvailableByScript() throws Exception {
        Jedis jedis = new Jedis("localhost");
        jedis.set("001", "100");

        String productCode = "001";
        ExecutorService service = Executors.newFixedThreadPool(nThreads);

        List<Callable<String>> callables = new ArrayList<>();

        for (int i = 0; i < nThreads * 2; i++) {
            Callable<String> task = () -> {
                Integer available = 0;
                try {
                    Integer randomInt = ThreadLocalRandom.current().nextInt(1, 10 + 1);
                    String threadName = Thread.currentThread().getName();

                    available = productService.decreaseAvailableByScript(productCode, randomInt);
                    if (available < 0) {
                        LOG.error("performed by {} out of stock", threadName);
                    } else {
                        LOG.info("performed by {}, {} - {} = {}", threadName, randomInt + available, randomInt, available);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return String.valueOf(available);
            };
            callables.add(task);
        }

        service.invokeAll(callables);
    }


    /**
     * Tests batch decrease available by using table lock
     * creates available of product randomly and tests decrease in multi-thread
     *
     * @throws Exception
     */
    @Test
    public void batchDecreaseAvailableByLock() throws Exception {

        ExecutorService service = Executors.newFixedThreadPool(nThreads);

        List<Callable<String>> callables = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            Callable<String> task = () -> {
                Integer available = 0;
                try {
                    List<ProductData> productDatas = new ArrayList<>();

                    for (int j = 0; j < 3; j++) {
                        Integer randomInt = ThreadLocalRandom.current().nextInt(1, 10 + 1);
                        ProductData productData = new ProductData();

                        if (j % 3 == 0) {
                            productData.setProductCode("003");
                        } else if (j % 2 == 0) {
                            productData.setProductCode("002");
                        } else {
                            productData.setProductCode("001");
                        }

                        productData.setAvailable(randomInt);
                        productDatas.add(productData);
                    }
                    productService.batchDecreaseAvailableByLock(productDatas);
                } catch (final InsufficientStockLevelException e) {
                    fail("out of stock");
                }

                return String.valueOf(available);
            };
            callables.add(task);
        }

        service.invokeAll(callables);
    }
}
