package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

public class Session {

    private MacacaDriver driver;
    private Utils utils;

    public Session(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public void createSession(JSONObject jsonObj) throws Exception {

        JSONObject desiredCapabilities = jsonObj.getJSONObject("desiredCapabilities");
        if (desiredCapabilities.get("host") != null) {
            String host = (String) desiredCapabilities.get("host");
            this.driver.setRemoteHost(host);
        }

        if (desiredCapabilities.get("port") != null) {
            int port = (int) desiredCapabilities.get("port");
            this.driver.setRemotePort(port);
        }

        if (System.getenv("MACACA_UDID") != null) {
            jsonObj.put("udid", System.getenv("MACACA_UDID"));
        }

        if (System.getenv("MACACA_APP_NAME") != null) {
            jsonObj.put("package", System.getenv("MACACA_APP_NAME"));
        }

//        JSONObject response = (JSONObject) utils.request("POST", DriverCommand.CREATE_SESSION, jsonObj);
//        String sessionId = (String) response.get("sessionId");
//        this.driver.setSessionId(sessionId);
//
//        this.driver.setCapabilities(response);
        this.driver.setSessionId("888");
    }

    public void delSession() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("DELETE", DriverCommand.SESSION, jsonObject);
    }

    public JSONObject sessionAvailable() throws Exception {
        return this.driver.getCapabilities();
    }

}
