package com.daxiang.selenium;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Component
@Slf4j
public class SeleniumInitializer implements ApplicationRunner {

    @Value("${chrome-driver-path}")
    private String chromeDriverPath;

    /**
     * chromeDriver服务端口
     */
    private static int chromeDriverServicePort = -1;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        // todo 下两个版本 支持web
//        log.info("[selenium]启动ChromeDriverService: {}", chromeDriverPath);
//        ChromeDriverService service = new ChromeDriverService.Builder()
//                .usingDriverExecutable(new File(chromeDriverPath))
//                .usingAnyFreePort()
//                .build();
//        service.start();
//        chromeDriverServicePort = service.getUrl().getPort();
//        log.info("[selenium]ChromeDriverService启动完成，端口：{}", chromeDriverServicePort);
    }
}
