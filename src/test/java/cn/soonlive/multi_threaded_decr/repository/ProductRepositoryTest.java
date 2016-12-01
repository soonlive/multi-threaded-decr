package cn.soonlive.multi_threaded_decr.repository;

import cn.soonlive.multi_threaded_decr.entity.Product;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xin on 23/11/2016.
 */

@RunWith(SpringRunner.class)
@SpringBootTest

//@AutoConfigureTestDatabase
public class ProductRepositoryTest {

    private final Logger LOG = LoggerFactory.getLogger(ProductRepositoryTest.class);

//    @Autowired
//    private TestEntityManager entityManager;


    @Autowired
    ProductRepository productRepository;

    @Test
    public void testFindOne() {
        Product product = productRepository.findOne("001");

        LOG.info("there are {} product: {}", product.getAvailable(), product.getProductCode());
    }

    @Test
    public void findByProductCodes() throws Exception {
        List<String> productCodes = new ArrayList<>();
        productCodes.add("001");
        productCodes.add("002");
        productCodes.add("003");

        List<Product> products = productRepository.findByProductCodeIn(productCodes);
        LOG.info("there are {} products", products.size());
        Assert.assertEquals(products.size(), 3);

    }
}
