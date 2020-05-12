package com.daxiang.core.pc.web;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
public class ChromeDevice extends BrowserDevice {

    public ChromeDevice(Browser browser, BrowserServer browserServer) {
        super(browser, browserServer);
    }

    @Override
    protected Capabilities createCapabilities() {
        ChromeOptions chromeOptions = new ChromeOptions();

        if (!StringUtils.isEmpty(browser.getPath())) {
            chromeOptions.setBinary(browser.getPath());
        }

        return chromeOptions;
    }
}
