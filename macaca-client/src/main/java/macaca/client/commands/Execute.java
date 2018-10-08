package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

public class Execute {

    private MacacaDriver driver;
    private Utils utils;

    public Execute(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public JSONObject execute(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        JSONObject result = (JSONObject) utils.request("POST", DriverCommand.EXECUTE_SCRIPT, jsonObject);
        return result;
    }
}
