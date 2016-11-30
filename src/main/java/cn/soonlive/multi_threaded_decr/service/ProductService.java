package cn.soonlive.multi_threaded_decr.service;

import cn.soonlive.multi_threaded_decr.data.ProductData;
import cn.soonlive.multi_threaded_decr.entity.Product;
import cn.soonlive.multi_threaded_decr.exception.InsufficientStockLevelException;
import cn.soonlive.multi_threaded_decr.exception.StockLevelUpdateException;
import cn.soonlive.multi_threaded_decr.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Xin on 23/11/2016.
 */
@Service
public class ProductService {

    private final Logger LOG = LoggerFactory.getLogger(ProductService.class);

    @Resource
    private ProductRepository productRepository;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    /**
     * @param productCode
     * @param value
     * @return
     */
    Integer decreaseAvailable(String productCode, Integer value) {
        String threadName = Thread.currentThread().getName();

        Product product = productRepository.findOne(productCode);
        Integer currentAvailable = product.getAvailable();

        if (product.getAvailable() >= value && product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - value);
            product = productRepository.save(product);
            LOG.info("performed by {}, {} - {} = {}", threadName, currentAvailable, value, product.getAvailable());
        }

        return product.getAvailable();
    }


    /**
     * @param productCode
     * @param value
     * @return
     */
    synchronized Integer syncDecreaseAvailable(String productCode, Integer value) {
        return this.decreaseAvailable(productCode, value);
    }

    Integer decreaseAvailableByCAS(String productCode, Integer value) {
        Jedis jedis = new Jedis("localhost");
        jedis.watch(productCode);

        String available = jedis.get(productCode);

        Transaction tx = jedis.multi();

        if (Long.valueOf(available) - value < 0) {
            tx.discard();
            return -1;
        }

        tx.decrBy(productCode, value);

        List<Object> result = tx.exec();

        if (result == null || result.isEmpty()) {
            return null;
        }
        return Integer.valueOf(result.get(0).toString());
    }

    /**
     * @param productCode
     * @param value
     * @return
     */
    Integer decreaseAvailableByScript(String productCode, Integer value) {

        Jedis jedis = new Jedis("localhost");

        String script = " local available = redis.call('get', KEYS[1]); "
                + " if ARGV[1] - available > 0 then return nil; else "
                + " return redis.call('decrby', KEYS[1], ARGV[1]); end; ";

        Integer available = (Integer) jedis.eval(script, 1, productCode, "" + value);

        if (available == null) {
            return -1;
        }

        return available;
    }

    /**
     * batch decrease available by locking table rows
     *
     * @param productDatas {@link ProductData }
     * @throws InsufficientStockLevelException
     * @throws StockLevelUpdateException
     */
    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 3000))
    void batchDecreaseAvailableByLock(List<ProductData> productDatas) throws InsufficientStockLevelException, StockLevelUpdateException {

        List<String> productCodes = productDatas.stream().map(ProductData::getProductCode).collect(Collectors.toList());

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("productCodes", productCodes);

        // 1: ok
        // -1: update fail
        // -999: insufficient stock level
        int result = transactionTemplate.execute(status -> {
            int retCode = -1;
            List<Product> beforeResults = this.findProductForUpdate(productCodes);

            for (ProductData productData : productDatas) {
                for (Product product : beforeResults) {
                    if (product.getProductCode().equals(productData.getProductCode())
                            && productData.getAvailable().compareTo(product.getAvailable()) > 0) {
                        LOG.info("{} - {} = {}", product.getAvailable(), productData.getAvailable());
                        return -999;
                    }
                }
            }

            String updateSql = "UPDATE products SET available = ((SELECT available FROM products WHERE product_code = :productCode) - :available) WHERE product_code = :productCode";
            SqlParameterSource[] params = SqlParameterSourceUtils.createBatch(productDatas.toArray());
            int[] updatedResults = namedParameterJdbcTemplate.batchUpdate(updateSql, params);

            if (updatedResults[0] == 1 && Arrays.stream(updatedResults).distinct().count() == 1) {
                retCode = 1;
            }

            return retCode;
        });

        if (result == -1) {
            throw new StockLevelUpdateException();
        }

        if (result == -999) {
            throw new InsufficientStockLevelException();
        }
    }

    List<Product> findProductForUpdate(List<String> productCodes) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("productCodes", productCodes);

        // lock the rows which are going to be updated
        String query = "SELECT product_code,available FROM products WHERE product_code IN (:productCodes) FOR UPDATE";

        List<Product> results = namedParameterJdbcTemplate.query(query, parameters, (rs, rowNum) -> {
            Product product = new Product();
            product.setProductCode(rs.getString(1));
            product.setAvailable(rs.getInt(2));
            return product;
        });
        return results;
    }
}
