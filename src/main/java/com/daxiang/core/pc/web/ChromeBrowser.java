package com.daxiang.core.pc.web;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
public class ChromeBrowser extends Browser {
    @Override
    protected Capabilities createCapabilities() {
        ChromeOptions chromeOptions = new ChromeOptions();

        if (!StringUtils.isEmpty(path)) {
            chromeOptions.setBinary(path);
        }

        return chromeOptions;
    }
}
