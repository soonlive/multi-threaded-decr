package cn.soonlive.multi_threaded_decr.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.io.Serializable;

/**
 * Created by Xin on 23/11/2016.
 */
@Entity(name = "products")
public class Product implements Serializable {

    @Id
    String productCode;
    Integer available;
    @Version
    Integer version;

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Integer getAvailable() {
        return available;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
