package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

/**
 * Created by jiangyitao.
 */
public class Toast {

    private MacacaDriver driver;
    private Utils utils;

    public Toast(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public String getToast() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        return utils.request("GET", DriverCommand.GET_TOAST, jsonObject).toString();
    }
}
