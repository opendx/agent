package com.fgnb.selenium;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by jiangyitao.
 */
@Component
@Slf4j
public class SeleniumInitializer implements ApplicationRunner{

    @Value("${chrome-driver-path}")
    private String chromeDriverPath;

    /** chromeDriver服务端口 */
    private static int chromeDriverServicePort = -1;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始启动ChromeDriverService:{}",chromeDriverPath);
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(chromeDriverPath))
                .usingAnyFreePort()
                .build();
        service.start();
        chromeDriverServicePort = service.getUrl().getPort();
        log.info("ChromeDriverService启动完成，端口：{}",chromeDriverServicePort);
    }

    /**
     * 获取chromedriver服务端口
     * @return
     */
    public static int getChromeDriverServicePort(){
        if(chromeDriverServicePort == -1){
            throw new RuntimeException("无法获取ChromeDriverServicePort");
        }
        return chromeDriverServicePort;
    }
}
