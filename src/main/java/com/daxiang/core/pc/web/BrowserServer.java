package com.daxiang.core.pc.web;

import com.daxiang.core.DeviceServer;
import com.daxiang.core.PortProvider;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.remote.service.DriverService.Builder;

import java.io.File;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class BrowserServer extends DeviceServer {

    private Class<? extends Builder> builderClass;
    private File driverFile;

    private DriverService driverService;

    public BrowserServer(Class<? extends Builder> builderClass, File driverFile) {
        this.builderClass = builderClass;
        this.driverFile = driverFile;
    }

    @Override
    public synchronized void start() {
        if (isRunning) {
            return;
        }

        try {
            DriverService.Builder builder = builderClass.newInstance();
            builder.usingDriverExecutable(driverFile);
            port = PortProvider.getPcDriverServiceAvailablePort();
            builder.usingPort(port);

            driverService = builder.build();
            log.info("start driver service, port: {}, driverFile: {}", port, driverFile.getAbsolutePath());
            driverService.start();

            url = driverService.getUrl();
            isRunning = driverService.isRunning();
        } catch (Exception e) {
            throw new RuntimeException("启动driver service失败", e);
        }
    }

    @Override
    public synchronized void stop() {
        if (isRunning) {
            driverService.stop();
            isRunning = false;
        }
    }

}
