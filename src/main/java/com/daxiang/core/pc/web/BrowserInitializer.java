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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class BrowserInitializer {

    public void init() throws IOException {
        List<BrowserJsonItem> browserJsonItems = parseBrowserProperties();
        if (browserJsonItems.isEmpty()) {
            log.warn("empty browser in " + Browser.PROPERTIES_PATH);
            return;
        }

        // 检查浏览器配置文件是否合法
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
            log.info("write {}: {}", Browser.PROPERTIES_PATH, newBrowserProperties);
            FileUtils.writeStringToFile(new File(Browser.PROPERTIES_PATH), newBrowserProperties, StandardCharsets.UTF_8);
        }

        browserJsonItems.forEach(browserJsonItem -> {
            Browser browser = new Browser();
            BeanUtils.copyProperties(browserJsonItem, browser);
            browser.setPlatform(Terminal.PLATFORM);

            BrowserServer browserServer;
            try {
                Class<? extends DriverService.Builder> builderClass = BrowserDevice.DRIVER_SERVICE_BUILDER_MAP.get(browserJsonItem.getType());
                if (builderClass == null) {
                    log.warn("{} 找不到DriverService.Builder", browserJsonItem);
                    return;
                }
                File driverFile = new File(browserJsonItem.getDriverPath());

                browserServer = new BrowserServer(builderClass, driverFile);
                browserServer.start();
            } catch (Exception e) {
                log.error("{} 启动browserServer失败", browserJsonItem, e);
                return;
            }

            BrowserDevice browserDevice;
            try {
                Class<? extends BrowserDevice> clazz = BrowserDevice.BROWSER_MAP.get(browserJsonItem.getType());
                Constructor<? extends BrowserDevice> constructor = clazz.getConstructor(Browser.class, BrowserServer.class);
                browserDevice = constructor.newInstance(browser, browserServer);
            } catch (Exception e) {
                log.error("{} 初始化browserDevice失败", browserJsonItem, e);
                return;
            }

            browserDevice.onlineToServer();
            DeviceHolder.put(browser.getId(), browserDevice);
        });
    }

    private List<BrowserJsonItem> parseBrowserProperties() throws IOException {
        File browserPropertiesFile = new File(Browser.PROPERTIES_PATH);
        if (!browserPropertiesFile.exists()) {
            log.warn(Browser.PROPERTIES_PATH + " not exists");
            return Collections.EMPTY_LIST;
        }

        String browserProperties = FileUtils.readFileToString(browserPropertiesFile, StandardCharsets.UTF_8);
        if (!StringUtils.hasText(browserProperties)) {
            log.warn(Browser.PROPERTIES_PATH + " has no text");
            return Collections.EMPTY_LIST;
        }

        List<BrowserJsonItem> browserJsonItems = JSONArray.parseArray(browserProperties, BrowserJsonItem.class);
        if (CollectionUtils.isEmpty(browserJsonItems)) {
            log.warn(Browser.PROPERTIES_PATH + " has no browser");
            return Collections.EMPTY_LIST;
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
