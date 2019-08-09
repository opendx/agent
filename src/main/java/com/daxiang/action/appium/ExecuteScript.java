package com.daxiang.action.appium;

import io.appium.java_client.AppiumDriver;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class ExecuteScript {
    private AppiumDriver appiumDriver;

    public ExecuteScript(AppiumDriver appiumDriver) {
        this.appiumDriver = appiumDriver;
    }

    /**
     * http://appium.io/docs/en/commands/mobile-command/
     * @param script
     * @param args
     * @return
     */
    public Object excute(Object script, Object... args) {
        Assert.notNull(script,"脚本不能为空");
        String _script = (String) script;
        return appiumDriver.executeScript(_script, args);
    }
}
