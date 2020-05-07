package com.daxiang.core.pc.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.safari.SafariDriverService;

import java.util.Date;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
@Data
public class Browser extends BrowserJsonItem {

    public static final String PROPERTIES_PATH = "browser.json";

    public static final int USING_STATUS = 1;
    public static final int IDLE_STATUS = 2;

    public static final Map<String, Class<? extends DriverService.Builder>> DRIVER_SERVICE_BUILDER_MAP = ImmutableMap.<String, Class<? extends DriverService.Builder>>builder()
            .put("chrome", ChromeDriverService.Builder.class)
            .put("firefox", GeckoDriverService.Builder.class)
            .put("safari", SafariDriverService.Builder.class)
            .put("edge", EdgeDriverService.Builder.class)
            .put("ie", InternetExplorerDriverService.Builder.class)
            .build();

    public static final Map<String, Class<? extends Browser>> BROWSER_MAP = ImmutableMap.<String, Class<? extends Browser>>builder()
            .put("chrome", ChromeBrowser.class)
            .build();

    private Integer platform; // 1.windows 2.linux 3.macos
    private Integer status;
    private String username;
    private String agentIp;
    private Integer agentPort;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastOnlineTime;

    @JsonIgnore
    protected BrowserDriverServer driverServer;
    @JsonIgnore
    protected RemoteWebDriver driver;

    public RemoteWebDriver freshDriver() {
        quitDriver();
        driver = new RemoteWebDriver(driverServer.getUrl(), createCapabilities());
        return driver;
    }

    protected Capabilities createCapabilities() {
        return null;
    }

    public void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ign) {
            }
        }
    }

    public boolean isIdle() {
        return status == IDLE_STATUS;
    }
}
