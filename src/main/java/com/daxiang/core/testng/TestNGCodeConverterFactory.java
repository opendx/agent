package com.daxiang.core.testng;

import com.daxiang.core.ProjectPlatform;
import com.daxiang.core.mobile.android.AndroidTestNGCodeConverter;
import com.daxiang.core.mobile.ios.IosTestNGCodeConverter;
import com.daxiang.core.pc.web.BrowserTestNGCodeConverter;

/**
 * Created by jiangyitao.
 */
public class TestNGCodeConverterFactory {
    public static TestNGCodeConverter create(Integer projectPlatform) {
        if (projectPlatform == ProjectPlatform.ANDROID) {
            return new AndroidTestNGCodeConverter();
        } else if (projectPlatform == ProjectPlatform.iOS) {
            return new IosTestNGCodeConverter();
        } else if (projectPlatform == ProjectPlatform.WEB) {
            return new BrowserTestNGCodeConverter();
        }

        throw new IllegalArgumentException("不支持的projectPlatform: " + projectPlatform);
    }
}
