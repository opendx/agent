package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

public class Keys {

    private MacacaDriver driver;
    private Utils utils;

    public Keys(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public void keys(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("POST", DriverCommand.KEYS, jsonObject);
    }

}
