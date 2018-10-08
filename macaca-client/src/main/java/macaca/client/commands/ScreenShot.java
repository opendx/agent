package macaca.client.commands;

import com.alibaba.fastjson.JSONObject;
import macaca.client.common.DriverCommand;
import macaca.client.common.MacacaDriver;
import macaca.client.common.Utils;
import org.apache.commons.codec.binary.Base64;

import java.io.FileOutputStream;

public class ScreenShot {

    private MacacaDriver driver;
    private Utils utils;

    public ScreenShot(MacacaDriver driver) {
        this.driver = driver;
        this.utils = new Utils(driver);
    }

    public Object takeScreenshot() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", driver.getSessionId());
        return utils.request("GET", DriverCommand.SCREENSHOT, jsonObject);
    }

    public void saveScreenshot(String filename) throws Exception {
        Base64 decoder = new Base64();
        try {
            // Decode Base64
            byte[] b = decoder.decode(takeScreenshot().toString());
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            // generate the image file
            FileOutputStream out = new FileOutputStream(filename);
            int n = 0;
            byte[] bb = new byte[1024];
            out.write(b);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
