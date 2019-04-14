package com.fgnb.init;

import com.fgnb.android.AdbManager;
import com.fgnb.android.DeviceChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by jiangyitao.
 * 首次启动初始化器
 */
@Component
@Slf4j
public class AndroidInitializer implements ApplicationRunner{

    @Autowired
    private DeviceChangeListener deviceChangeListener;

    @Value("${server}")
    private String server;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        log.info("UIServer -> {}",server);
        //每次启动 都先杀掉adb server
        AdbManager.killAdbServer();
        //初始化adb
        AdbManager.getAdb();
        //添加设备监听器，监听设备连接、断开
        AdbManager.addDeviceChangeListener(deviceChangeListener);
    }
}
