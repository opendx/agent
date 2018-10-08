package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

public class Url {

    private MacacaDriver driver;
    private Utils utils;

    public Url(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public String url() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        String url = (String) utils.request("GET", DriverCommand.URL, jsonObject);
        return url;
    }

    public void getUrl(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("POST", DriverCommand.URL, jsonObject);
    }

    public void forward() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("POST", DriverCommand.URL, jsonObject);
    }

    public void back(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("POST", DriverCommand.URL, jsonObject);
    }

    public void refresh(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("POST", DriverCommand.URL, jsonObject);
    }


}
