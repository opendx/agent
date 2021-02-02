package com.daxiang.core.mobile.ios;

import com.android.ddmlib.IDevice;
import com.daxiang.core.mobile.Mobile;
import com.daxiang.core.mobile.MobileChangeHandler;
import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.core.mobile.appium.AppiumServer;
import com.daxiang.model.UploadFile;
import io.appium.java_client.AppiumDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Component
public class DefaultIosDeviceChangeListener extends MobileChangeHandler implements IosDeviceChangeListener {

    @Override
    public void deviceConnected(IDevice iDevice) {
        new Thread(() -> mobileConnected(iDevice)).start();
    }

    @Override
    public void deviceDisconnected(IDevice iDevice) {
        new Thread(() -> mobileDisconnected(iDevice.getSerialNumber())).start();
    }

    @Override
    protected MobileDevice newMobile(IDevice iDevice, Mobile mobile, AppiumServer appiumServer) {
        return new IosDevice(mobile, appiumServer);
    }

    @Override
    protected MobileDevice initMobile(IDevice iDevice, AppiumServer appiumServer) throws Exception {
        String mobileId = iDevice.getSerialNumber();
        boolean isRealDevice = !iDevice.isEmulator();

        Mobile mobile = new Mobile();

        mobile.setPlatform(IosDevice.PLATFORM);
        mobile.setCreateTime(new Date());
        mobile.setId(mobileId);
        mobile.setName(IosUtil.getDeviceName(mobileId, isRealDevice));
        mobile.setEmulator(isRealDevice ? Mobile.REAL_MOBILE : Mobile.EMULATOR);

        if (isRealDevice) {
            mobile.setSystemVersion(IosUtil.getRealDeviceSystemVersion(mobileId));
        }

        IosDevice iosDevice = new IosDevice(mobile, appiumServer);

        log.info("[{}]开始初始化appium", mobileId);
        RemoteWebDriver driver = iosDevice.freshDriver(null, true);
        log.info("[{}]初始化appium完成", mobileId);

        if (!isRealDevice) {
            try {
                AppiumDriver appiumDriver = (AppiumDriver) driver;
                String sdkVersion = (String) appiumDriver.getSessionDetail("sdkVersion");
                mobile.setSystemVersion(sdkVersion);
            } catch (Exception e) {
                log.warn("[{}]获取sdkVersion失败", mobileId, e);
            }
        }

        // 有时window获取的宽高可能为0
        while (true) {
            Dimension window = driver.manage().window().getSize();
            int width = window.getWidth();
            int height = window.getHeight();

            if (width > 0 && height > 0) {
                mobile.setScreenWidth(width);
                mobile.setScreenHeight(height);
                break;
            } else {
                log.warn("[{}]未获取到正确的屏幕宽高: {}", mobileId, window);
            }
        }

        // 截图并上传到服务器
        UploadFile uploadFile = iosDevice.screenshotAndUploadToServer();
        mobile.setImgPath(uploadFile.getFilePath());

        driver.quit();
        return iosDevice;
    }

}
