package cn.soonlive.multi_threaded_decr.data;

/**
 * Created by Xin on 29/11/2016.
 */
public class ProductData {
    String productCode;
    Integer available;
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
