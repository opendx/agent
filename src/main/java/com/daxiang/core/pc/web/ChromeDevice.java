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
    protected Capabilities newCaps(Capabilities capsToMerge) {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--ignore-certificate-errors");
        chromeOptions.addArguments("no-default-browser-check");

        // **** 以上capabilities可被传入的caps覆盖 ****

        chromeOptions.merge(capsToMerge);

        // **** 以下capabilities具有更高优先级，将覆盖传入的caps ****

        if (!StringUtils.isEmpty(browser.getPath())) {
            chromeOptions.setBinary(browser.getPath());
        }

        return chromeOptions;
    }
}
