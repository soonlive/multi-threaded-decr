package cn.soonlive.multi_threaded_decr.repository;

import cn.soonlive.multi_threaded_decr.entity.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by Xin on 23/11/2016.
 */

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase
public class ProductRepositoryTest {

    private final Logger LOG = LoggerFactory.getLogger(ProductRepositoryTest.class);

    @Autowired
    private TestEntityManager entityManager;


    @Autowired
    ProductRepository productRepository;

    @Test
    public void testFindOne(){
        Product product = productRepository.findOne("001");

        LOG.info("there are {} product: {}" ,product.getAvailable(), product.getProductCode());
    }
}
