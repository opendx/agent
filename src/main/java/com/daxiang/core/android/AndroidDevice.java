package com.daxiang.core.android;

import com.android.ddmlib.IDevice;
import com.daxiang.core.MobileDevice;
import com.daxiang.core.android.stf.AdbKit;
import com.daxiang.core.android.stf.Minicap;
import com.daxiang.core.android.stf.Minitouch;
import com.daxiang.model.Device;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Data
public class AndroidDevice extends MobileDevice {

    public final static String TMP_FOLDER = "/data/local/tmp/";

    private IDevice iDevice;

    private Minicap minicap;
    private Minitouch minitouch;
    private AdbKit adbKit;

    public AndroidDevice(Device device, IDevice iDevice) {
        super(device);
        this.iDevice = iDevice;
    }

    public boolean canUseUiautomator2() {
        String androidVersion = getDevice().getSystemVersion();
        for (String sdkVersion : AndroidUtil.ANDROID_VERSION.keySet()) {
            if (androidVersion.equals(AndroidUtil.ANDROID_VERSION.get(sdkVersion))) {
                if (Integer.parseInt(sdkVersion) > 20) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        throw new RuntimeException("无法判断是否能用Uiautomator2，请更新AndroidUtil.ANDROID_VERSION");
    }
}
