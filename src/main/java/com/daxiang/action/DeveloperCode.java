import com.daxiang.core.MobileDeviceHolder;
import io.appium.java_client.AppiumDriver;
import org.testng.annotations.Test;

/**
 * Created by jiangyitao.
 * 不要set package name 否则无法调试
 */
public class DeveloperCode {


    private AppiumDriver appiumDriver;

    @Test
    public void test() {
        final String deviceId = "DEVICE_ID";
        appiumDriver = MobileDeviceHolder.get(deviceId).getAppiumDriver();

        // 以下为要测试的代码
        String context = appiumDriver.getContext();
        System.out.println(context);
    }
}
