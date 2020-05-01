package com.daxiang.core.pcweb;

import com.alibaba.fastjson.JSONArray;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class BrowserInitializer {

    private String browserPropertiesPath;

    public BrowserInitializer(String browserPropertiesPath) {
        if (StringUtils.isEmpty(browserPropertiesPath)) {
            throw new IllegalArgumentException();
        }

        this.browserPropertiesPath = browserPropertiesPath;
    }

    public void init() throws IOException {
        List<BrowserJsonItem> browserJsonItems = parseBrowserProperties();
        if (browserJsonItems.isEmpty()) {
            log.warn("empty browser in " + browserPropertiesPath);
            return;
        }

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
            BrowserDriverServer driverServer;
            try {
                String builderClassName = Browser.DRIVER_SERVICE_BUILDER.get(browserJsonItem.getType());
                File driverFile = new File(browserJsonItem.getDriverPath());
                driverServer = new BrowserDriverServer(builderClassName, driverFile);
                driverServer.start();
            } catch (Exception e) {
                log.error("{} 启动driver server失败", browserJsonItem, e);
                return;
            }

            Browser browser = new Browser();
            browser.setDriverServer(driverServer);
            BeanUtils.copyProperties(browserJsonItem, browser);
            browser.setPlatform(Terminal.PLATFORM);
            browser.setStatus(Browser.IDLE_STATUS);

            BrowserHolder.add(browser.getId(), browser);
        });
    }

    private List<BrowserJsonItem> parseBrowserProperties() throws IOException {
        File browserPropertiesFile = new File(browserPropertiesPath);
        if (!browserPropertiesFile.exists()) {
            log.warn(browserPropertiesPath + " not exists");
            return Collections.EMPTY_LIST;
        }

        String browserProperties = FileUtils.readFileToString(browserPropertiesFile, StandardCharsets.UTF_8);
        if (!StringUtils.hasText(browserProperties)) {
            log.warn(browserPropertiesPath + " has no text");
            return Collections.EMPTY_LIST;
        }

        List<BrowserJsonItem> browserJsonItems = JSONArray.parseArray(browserProperties, BrowserJsonItem.class);
        if (CollectionUtils.isEmpty(browserJsonItems)) {
            log.warn(browserPropertiesPath + " has no browser");
            return Collections.EMPTY_LIST;
        }

        return browserJsonItems.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void checkBrowserJsonItems(List<BrowserJsonItem> browserJsonItems) {
        for (BrowserJsonItem browserJsonItem : browserJsonItems) {
            if (StringUtils.isEmpty(browserJsonItem.getType()) || !Browser.DRIVER_SERVICE_BUILDER.containsKey(browserJsonItem.getType())) {
                throw new IllegalArgumentException("illegal browserType");
            }
            if (StringUtils.isEmpty(browserJsonItem.getDriverPath())) {
                throw new IllegalArgumentException("illegal driverPath");
            }
            if (StringUtils.isEmpty(browserJsonItem.getVersion())) {
                throw new IllegalArgumentException("illegal version");
            }
        }
    }
}
