package com.daxiang.core.pc.web;

import com.daxiang.core.Device;
import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.safari.SafariDriverService;

import java.util.Date;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
public abstract class BrowserDevice extends Device {

    public static final Map<String, Class<? extends DriverService.Builder>> DRIVER_SERVICE_BUILDER_MAP = ImmutableMap.<String, Class<? extends DriverService.Builder>>builder()
            .put("chrome", ChromeDriverService.Builder.class)
            .put("firefox", GeckoDriverService.Builder.class)
            .put("safari", SafariDriverService.Builder.class)
            .put("edge", EdgeDriverService.Builder.class)
            .put("ie", InternetExplorerDriverService.Builder.class)
            .build();

    public static final Map<String, Class<? extends BrowserDevice>> BROWSER_MAP = ImmutableMap.<String, Class<? extends BrowserDevice>>builder()
            .put("chrome", ChromeDevice.class)
            .put("firefox", FirefoxDevice.class)
            .build();

    protected Browser browser;

    public BrowserDevice(Browser browser, BrowserServer browserServer) {
        super(browserServer);
        this.browser = browser;
    }

    public Browser getBrowser() {
        return browser;
    }

    @Override
    public String getId() {
        return browser.getId();
    }

    @Override
    public Integer getStatus() {
        return browser.getStatus();
    }

    @Override
    public void onlineToServer() {
        browser.setAgentIp(agentIp);
        browser.setAgentPort(agentPort);
        browser.setLastOnlineTime(new Date());
        idleToServer();
    }

    @Override
    public void usingToServer(String username) {
        browser.setUsername(username);
        browser.setStatus(Device.USING_STATUS);
        serverClient.saveBrowser(browser);
    }

    @Override
    public void idleToServer() {
        browser.setStatus(Device.IDLE_STATUS);
        serverClient.saveBrowser(browser);
    }

    @Override
    public void offlineToServer() {
        browser.setStatus(Device.OFFLINE_STATUS);
        serverClient.saveBrowser(browser);
    }
}
