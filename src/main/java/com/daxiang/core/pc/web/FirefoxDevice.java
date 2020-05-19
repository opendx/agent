package com.daxiang.core.pc.web;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
public class FirefoxDevice extends BrowserDevice {

    public FirefoxDevice(Browser browser, BrowserServer browserServer) {
        super(browser, browserServer);
    }

    @Override
    protected Capabilities newCaps(Capabilities capsToMerge) {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setAcceptInsecureCerts(true);

        // **** 以上capabilities可被传入的caps覆盖 ****

        firefoxOptions.merge(capsToMerge);

        // **** 以下capabilities具有更高优先级，将覆盖传入的caps ****

        if (!StringUtils.isEmpty(browser.getPath())) {
            firefoxOptions.setBinary(browser.getPath());
        }

        return firefoxOptions;
    }
}
