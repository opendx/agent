package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;


public class Cookie {

    private MacacaDriver driver;
    private Utils utils;

    public Cookie(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    /**
     * get all cookies
     *
     * @return all cookies
     */
    public String allCookies() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        return (String) utils.request("GET", DriverCommand.COOKIE, jsonObject);
    }

    /**
     * @param jsonObject cookie example:
     *                   {name:'fruit', value:'apple'}
     *                   Optional cookie fields:
     *                   path, domain, secure, expiry
     */
    public void setCookie(JSONObject jsonObject) throws Exception {
        jsonObject.put("sessionId", driver.getSessionId());
        utils.request("POST", DriverCommand.COOKIE, jsonObject);
    }

    /**
     * delete specific cookie
     *
     * @param cookieName name of cookie to delete
     */
    public void deleteCookie(String cookieName) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        utils.deleteRequest(DriverCommand.COOKIE + cookieName, jsonObject);
    }

    /**
     * delete all cookies
     */
    public void deleteAllCookies() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        utils.deleteRequest(DriverCommand.COOKIE, jsonObject);
    }


}
