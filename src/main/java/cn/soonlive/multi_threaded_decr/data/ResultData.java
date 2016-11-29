package cn.soonlive.multi_threaded_decr.data;

import java.util.List;

/**
 * Created by Xin on 29/11/2016.
 */
public class ResultData {
    String status;
    String reason;
    List data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }
}
