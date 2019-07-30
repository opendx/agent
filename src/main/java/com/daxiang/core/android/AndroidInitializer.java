package com.daxiang.core.android;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Component
public class AndroidInitializer implements ApplicationRunner {

    @Autowired
    private AndroidDeviceChangeListener androidDeviceChangeListener;

    @Value("${android}")
    private boolean needAndroid;

    @Override
    public void run(ApplicationArguments args) throws IOException, InterruptedException {
        if (needAndroid) {
            ADB.killServer();
            Thread.sleep(1000);
            ADB.startServer();
            ADB.init();
            // 添加设备监听器，监听设备连接、断开
            ADB.addDeviceChangeListener(androidDeviceChangeListener);
        }
    }
}
