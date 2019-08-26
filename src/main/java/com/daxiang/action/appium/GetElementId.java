package com.daxiang.action.appium;

import io.appium.java_client.MobileElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class GetElementId {

    public String excute(Object webElement) {
        Assert.notNull(webElement, "元素不能为空");

        MobileElement mobileElement = (MobileElement) webElement;
        String eleId = mobileElement.getId();
        log.info("eleId: {}", eleId);
        return eleId;
    }
}
