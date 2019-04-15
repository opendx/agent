package com.fgnb.init;

import com.fgnb.App;
import com.fgnb.android.ADB;
import com.fgnb.android.AndroidDeviceChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AndroidDeviceChangeListener deviceChangeListener;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        log.info("Server: {}", App.getProperty("server"));
        //agent每次启动都先kill adb server
        ADB.killServer();
        //初始化adb
        ADB.init();
        //添加设备监听器，监听设备连接、断开
        ADB.addDeviceChangeListener(deviceChangeListener);
    }
}
