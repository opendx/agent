package com.daxiang.core.ios;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Created by jiangyitao.
 */
@Component
@Slf4j
public class IosInitializer implements ApplicationRunner {

    @Autowired
    private IosDeviceChangeListener iosDeviceChangeListener;

    @Value("${ios}")
    private boolean needIos;

    @Override
    public void run(ApplicationArguments args) {
        if (needIos) {
            IosDeviceMonitor iosDeviceMonitor = IosDeviceMonitor.getInstance();
            iosDeviceMonitor.start(iosDeviceChangeListener);
            log.info("[ios]开始监听设备连接/断开");
        }
    }
}
