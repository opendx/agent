package com.daxiang.core.pc.web;

import com.alibaba.fastjson.JSONArray;
import com.daxiang.server.ServerClient;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.remote.service.DriverService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class BrowserInitializer {

    @Autowired
    private ServerClient serverClient;
    @Value("${ip}")
    private String ip;
    @Value("${port}")
    private Integer port;

    public void init() throws IOException {
        List<BrowserJsonItem> browserJsonItems = parseBrowserProperties();
        if (browserJsonItems.isEmpty()) {
            log.warn("empty browser in " + Browser.PROPERTIES_PATH);
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
            log.info("write {}: {}", Browser.PROPERTIES_PATH, newBrowserProperties);
            FileUtils.writeStringToFile(new File(Browser.PROPERTIES_PATH), newBrowserProperties, StandardCharsets.UTF_8);
        }

        browserJsonItems.forEach(browserJsonItem -> {
            Browser browser;
            try {
                browser = Browser.BROWSER_MAP.get(browserJsonItem.getType()).newInstance();
            } catch (Exception e) {
                log.error("{} 初始化browser失败", browserJsonItem, e);
                return;
            }

            try {
                Class<? extends DriverService.Builder> builderClass = Browser.DRIVER_SERVICE_BUILDER_MAP.get(browserJsonItem.getType());
                File driverFile = new File(browserJsonItem.getDriverPath());
                BrowserDriverServer driverServer = new BrowserDriverServer(builderClass, driverFile);
                driverServer.start();
                browser.setDriverServer(driverServer);
            } catch (Exception e) {
                log.error("{} 启动driver server失败", browserJsonItem, e);
                return;
            }

            BeanUtils.copyProperties(browserJsonItem, browser);
            browser.setAgentIp(ip);
            browser.setAgentPort(port);
            browser.setPlatform(Terminal.PLATFORM);
            browser.setStatus(Browser.IDLE_STATUS);
            browser.setLastOnlineTime(new Date());

            serverClient.saveBrowser(browser);
            BrowserHolder.add(browser.getId(), browser);
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
            if (StringUtils.isEmpty(browserJsonItem.getType()) || !Browser.DRIVER_SERVICE_BUILDER_MAP.containsKey(browserJsonItem.getType())) {
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
