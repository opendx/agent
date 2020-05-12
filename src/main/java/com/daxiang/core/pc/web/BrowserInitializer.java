package com.daxiang.core.pc.web;

import com.alibaba.fastjson.JSONArray;
import com.daxiang.core.DeviceHolder;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.remote.service.DriverService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class BrowserInitializer {

    public void init(String browserPropertiesPath) throws IOException {
        List<BrowserJsonItem> browserJsonItems = parseBrowserProperties(browserPropertiesPath);
        if (browserJsonItems.isEmpty()) {
            throw new IllegalStateException(browserPropertiesPath + " has no browser");
        }

        // 检查浏览器配置文件内容是否合法
        checkBrowserJsonItems(browserJsonItems);

        boolean reWriteBrowserProperties = false;
        for (BrowserJsonItem browserJsonItem : browserJsonItems) {
            if (StringUtils.isEmpty(browserJsonItem.getId())) {
                browserJsonItem.setId(UUIDUtil.getUUID()); // 给浏览器一个唯一id
                reWriteBrowserProperties = true;
            }
        }
        if (reWriteBrowserProperties) { // 填充了浏览器id，重新写入配置
            String newBrowserProperties = JSONArray.toJSONString(browserJsonItems, true);
            log.info("write {}: {}", browserPropertiesPath, newBrowserProperties);
            FileUtils.writeStringToFile(new File(browserPropertiesPath), newBrowserProperties, StandardCharsets.UTF_8);
        }

        browserJsonItems.forEach(browserJsonItem -> {
            Browser browser = new Browser();
            BeanUtils.copyProperties(browserJsonItem, browser);
            browser.setPlatform(Terminal.PLATFORM);

            BrowserServer browserServer;
            try {
                Class<? extends DriverService.Builder> driverBuilderClass = BrowserDevice.DRIVER_SERVICE_BUILDER_MAP.get(browserJsonItem.getType());
                if (driverBuilderClass == null) {
                    log.warn("[{}]找不到DriverService.Builder", browserJsonItem.getId());
                    return;
                }
                File driverFile = new File(browserJsonItem.getDriverPath());

                log.info("[{}]启动browser server...", browserJsonItem.getId());
                browserServer = new BrowserServer(driverBuilderClass, driverFile);
                browserServer.start();
                log.info("[{}]启动browser server完成, url: {}", browserJsonItem.getId(), browserServer.getUrl());
            } catch (Exception e) {
                log.error("[{}]启动browser server失败", browserJsonItem.getId(), e);
                return;
            }

            BrowserDevice browserDevice;
            try {
                Class<? extends BrowserDevice> clazz = BrowserDevice.BROWSER_MAP.get(browserJsonItem.getType());
                Constructor<? extends BrowserDevice> constructor = clazz.getConstructor(Browser.class, BrowserServer.class);
                browserDevice = constructor.newInstance(browser, browserServer);
            } catch (Exception e) {
                log.error("[{}]初始化browserDevice失败", browserJsonItem.getId(), e);
                log.info("[{}]停止browser server", browserJsonItem.getId());
                browserServer.stop();
                return;
            }

            browserDevice.onlineToServer();
            DeviceHolder.put(browser.getId(), browserDevice);
        });
    }

    private List<BrowserJsonItem> parseBrowserProperties(String browserPropertiesPath) throws IOException {
        if (!StringUtils.hasText(browserPropertiesPath)) {
            throw new IllegalArgumentException(browserPropertiesPath + " must hasText");
        }

        File browserPropertiesFile = new File(browserPropertiesPath);
        if (!browserPropertiesFile.exists()) {
            throw new IllegalArgumentException(browserPropertiesPath + " not exists");
        }

        String browserProperties = FileUtils.readFileToString(browserPropertiesFile, StandardCharsets.UTF_8);
        if (!StringUtils.hasText(browserProperties)) {
            throw new IllegalArgumentException(browserPropertiesPath + " must has content");
        }

        List<BrowserJsonItem> browserJsonItems = JSONArray.parseArray(browserProperties, BrowserJsonItem.class);
        if (CollectionUtils.isEmpty(browserJsonItems)) {
            throw new IllegalArgumentException(browserPropertiesPath + " has no browser");
        }

        return browserJsonItems.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void checkBrowserJsonItems(List<BrowserJsonItem> browserJsonItems) {
        for (BrowserJsonItem browserJsonItem : browserJsonItems) {
            if (StringUtils.isEmpty(browserJsonItem.getType())
                    || !BrowserDevice.DRIVER_SERVICE_BUILDER_MAP.containsKey(browserJsonItem.getType())) {
                throw new IllegalArgumentException("illegal browserType");
            }
            if (StringUtils.isEmpty(browserJsonItem.getDriverPath())
                    || !Files.exists(Paths.get(browserJsonItem.getDriverPath()))) {
                throw new IllegalArgumentException("illegal driverPath");
            }
            if (StringUtils.isEmpty(browserJsonItem.getVersion())) {
                throw new IllegalArgumentException("illegal version");
            }
        }
    }
}
