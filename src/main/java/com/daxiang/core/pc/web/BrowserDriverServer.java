package com.daxiang.core.pc.web;

import com.daxiang.core.PortProvider;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.remote.service.DriverService.Builder;

import java.io.File;
import java.net.URL;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class BrowserDriverServer {

    private Class<? extends Builder> builderClass;
    private File driverFile;

    private DriverService driverService;
    private boolean isRunning;

    public BrowserDriverServer(Class<? extends Builder> builderClass, File driverFile) {
        if (builderClass == null || driverFile == null || !driverFile.exists()) {
            throw new IllegalArgumentException();
        }

        this.builderClass = builderClass;
        this.driverFile = driverFile;
        isRunning = false;
    }

    public synchronized void start() {
        if (isRunning) {
            return;
        }

        try {
            DriverService.Builder builder = builderClass.newInstance();

            builder.usingDriverExecutable(driverFile);
            int port = PortProvider.getPcDriverServiceAvailablePort();
            builder.usingPort(port);

            driverService = builder.build();
            log.info("start driver service, port: {}, driverFile: {}", port, driverFile.getAbsolutePath());
            driverService.start();

            isRunning = driverService.isRunning();
        } catch (Exception e) {
            throw new RuntimeException("启动driver service失败", e);
        }
    }

    public synchronized void stop() {
        if (isRunning) {
            driverService.stop();
        }
    }

    public URL getUrl() {
        if (!isRunning) {
            throw new IllegalStateException("driverService is not in running");
        }
        return driverService.getUrl();
    }

}
