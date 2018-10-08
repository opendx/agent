package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;

public class Window {

    private MacacaDriver driver;
    private Utils utils;

    public Window(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public void getWindow() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        String windowHandle = (String) utils.request("GET", DriverCommand.WINDOW_HANDLE, jsonObject);
        driver.setWindowHandle(windowHandle);
    }

    public void getWindows() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("GET", DriverCommand.WINDOW_HANDLES, jsonObject);
    }

    public JSONObject getWindowSize() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("windowHandle", "current");
        return (JSONObject) utils.request("GET", DriverCommand.WINDOW_SIZE, jsonObject);
    }

    public void setWindowSize(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("windowHandle", "current");
        utils.request("POST", DriverCommand.WINDOW_SIZE, jsonObject);
    }

    public void maximize() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        jsonObject.put("windowHandle", "current");
        utils.request("POST", DriverCommand.MAXIMIZE_WINDOW, jsonObject);
    }

    public String setWindow(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        String name = (String) utils.request("POST", DriverCommand.WINDOW, jsonObject);
        return name;
    }

    public void deleteWindow() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("DELETE", DriverCommand.WINDOW, jsonObject);
    }

    public void setFrame(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        String name = utils.request("POST", DriverCommand.FRAME, jsonObject).toString();
    }

}
