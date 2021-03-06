package cn.soonlive.multi_threaded_decr.repository;

import cn.soonlive.multi_threaded_decr.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Xin on 23/11/2016.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByProductCodeIn(List<String> productCodes);
}
