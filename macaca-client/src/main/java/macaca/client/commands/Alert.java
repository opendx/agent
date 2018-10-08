package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

public class Alert {

    private MacacaDriver driver;
    private Utils utils;

    public Alert(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public void acceptAlert() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("POST", DriverCommand.ACCEPT_ALERT, jsonObject);
    }

    public void dismissAlert() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("POST", DriverCommand.DISMISS_ALERT, jsonObject);
    }

    public String alertText() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        return (String) utils.request("GET", DriverCommand.ALERT_TEXT, jsonObject);
    }

    public void alertKeys(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("POST", DriverCommand.ALERT_TEXT, jsonObject);
    }

}
