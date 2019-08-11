package com.daxiang.action.appium;

import io.appium.java_client.MobileElement;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
public class GetElementId {

    // todo insert sql
    public String excute(Object webElement) {
        Assert.notNull(webElement, "元素不能为空");

        MobileElement mobileElement = (MobileElement) webElement;
        return mobileElement.getId();
    }
}
