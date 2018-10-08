package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

public class Source {

    private MacacaDriver driver;
    private Utils utils;

    public Source(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public String getSource() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        return utils.request("GET", DriverCommand.GET_SOURCE, jsonObject).toString();
    }

}
