package com.daxiang.core;

import com.daxiang.core.android.ADB;
import com.daxiang.core.android.AndroidDeviceChangeListener;
import com.daxiang.core.ios.IosDeviceChangeListener;
import com.daxiang.core.ios.IosDeviceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class MobileInitializer implements ApplicationRunner {

    @Autowired
    private AndroidDeviceChangeListener androidDeviceChangeListener;
    @Autowired
    private IosDeviceChangeListener iosDeviceChangeListener;

    @Value("${android}")
    private boolean needAndroid;
    @Value("${ios}")
    private boolean needIos;

    @Override
    public void run(ApplicationArguments args) throws IOException, InterruptedException {
        if (needAndroid) {
            ADB.killServer();
            Thread.sleep(1000);
            ADB.startServer();
            ADB.init();
            ADB.addDeviceChangeListener(androidDeviceChangeListener);
            log.info("[android]开始监听设备连接/断开");
        }

        if (needIos) {
            IosDeviceMonitor iosDeviceMonitor = IosDeviceMonitor.getInstance();
            iosDeviceMonitor.start(iosDeviceChangeListener);
            log.info("[ios]开始监听设备连接/断开");
        }
    }
}
