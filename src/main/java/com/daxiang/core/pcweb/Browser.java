package com.daxiang.core.pcweb;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;

/**
 * Created by jiangyitao.
 */
@Data
public class Browser extends BrowserJsonItem {

    public static final int USING_STATUS = 1;
    public static final int IDLE_STATUS = 2;

    public static final String PROPERTIES_PATH = "browser.json";

    public static final Map<String, String> DRIVER_SERVICE_BUILDER = ImmutableMap.<String, String>builder()
            .put("chrome", "org.openqa.selenium.chrome.ChromeDriverService$Builder")
            .put("firefox", "org.openqa.selenium.firefox.GeckoDriverService$Builder")
            .put("safari", "org.openqa.selenium.safari.SafariDriverService$Builder")
            .put("edge", "org.openqa.selenium.edge.EdgeDriverService$Builder")
            .put("ie", "org.openqa.selenium.ie.InternetExplorerDriverService$Builder")
            .put("opera", "org.openqa.selenium.opera.OperaDriverService$Builder")
            .build();

    private Integer platform; // 1.windows 2.linux 3.macos
    private Integer status;

    protected BrowserDriverServer driverServer;
    protected WebDriver webDriver;

    public WebDriver freshWebDriver() {
        quitWebDriver();
        webDriver = new RemoteWebDriver(driverServer.getUrl(), createCapabilities());
        return webDriver;
    }

    protected Capabilities createCapabilities() {
        return null;
    }

    public void quitWebDriver() {
        if (webDriver != null) {
            try {
                webDriver.quit();
            } catch (Exception ign) {
            }
        }
    }
}
