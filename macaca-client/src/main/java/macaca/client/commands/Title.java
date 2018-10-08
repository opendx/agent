package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

public class Title {

    private MacacaDriver driver;
    private Utils utils;

    public Title(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public String title() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        String title = (String) utils.request("GET", DriverCommand.GET_TITLE, jsonObject);
        return title;
    }

}
