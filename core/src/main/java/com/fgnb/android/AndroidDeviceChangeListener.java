package com.fgnb.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.fgnb.App;
import com.fgnb.android.stf.StfResourceReleaser;
import com.fgnb.android.stf.minicap.MinicapManager;
import com.fgnb.android.stf.minitouch.MinitouchManager;
import com.fgnb.android.uiautomator.UiautomatorServerManager;
import com.fgnb.api.ServerApi;
import com.fgnb.model.Device;
import com.fgnb.utils.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

/**
 * Created by jiangyitao.
 * 安卓手机接入，拔出监听器
 */
@Component
@Slf4j
public class AndroidDeviceChangeListener implements AndroidDebugBridge.IDeviceChangeListener {

    @Autowired
    private ServerApi serverApi;

    @Override
    public void deviceConnected(IDevice device) {
        new Thread(() -> {
            try {
                androidDeviceConnected(device);
            } catch (Exception e) {
                log.error("[{}]设备连接处理出错",device.getSerialNumber(),e);
            }
        }).start();
    }

    @Override
    public void deviceDisconnected(IDevice device) {
        new Thread(() -> androidDeviceDisconnected(device)).start();
    }

    @Override
    public void deviceChanged(IDevice device, int changeMask) {
        //ignore
    }

    /**
     * Android设备连接上，调用的方法
     * @param iDevice
     * @throws Exception
     */
    private void androidDeviceConnected(IDevice iDevice) throws Exception {
        String deviceId = iDevice.getSerialNumber();
        log.info("[{}]已连接", deviceId);

        //等待设备上线
        log.info("[{}]等待手机上线", deviceId);
        AndroidUtils.waitForDeviceOnline(iDevice, 60);
        log.info("[{}]手机已上线", deviceId);

        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        if (androidDevice == null) {//该agent未接入过该手机
            //第一次上线
            log.info("[{}]首次上线", deviceId);
            Device device = serverApi.getDeviceById(deviceId);
            if (device == null) {
                //首次接入
                log.info("[{}]首次接入系统", deviceId);
                androidDevice = initDevice(iDevice);
            } else {
                //接入过 首次上线
                log.info("[{}]非首次接入系统", deviceId);
                androidDevice = new AndroidDevice(device);
                androidDevice.setIDevice(iDevice);
            }
            AndroidDeviceHolder.addAndroidDevice(deviceId, androidDevice);
        }

        Device device = androidDevice.getDevice();
        //agent ip
        device.setAgentIp(NetUtil.getLocalHostAddress());
        //agent port
        device.setAgentPort(Integer.parseInt(App.getProperty("server.port")));
        //闲置状态
        device.setStatus(Device.IDLE_STATUS);
        //最后一次在线时间
        device.setLastOnlineTime(new Date());
        //上报服务器
        serverApi.saveDevice(device);
        log.info("[{}]deviceConnected处理完成", deviceId);

    }

    /**
     * 设备断开连接，调用的方法
     * 注意这里是多线程调用，尽量不要用成员变量
     *
     * @param iDevice
     */
    public void androidDeviceDisconnected(IDevice iDevice) {
        //设备id
        String deviceId = iDevice.getSerialNumber();

        log.info("[{}]断开连接", deviceId);

        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        //有可能刚连上就断了 androidDevice还没初始化
        if (androidDevice == null) {
            return;
        }

        //手机断开 回收minicap/minitouch/adbkit等占用的资源，如关闭输入输出流，端口释放等
        StfResourceReleaser stfResourceReleaser = new StfResourceReleaser(deviceId);
        stfResourceReleaser.release();

        androidDevice.getDevice().setStatus(Device.OFFLINE_STATUS);
        androidDevice.getDevice().setLastOfflineTime(new Date());
        serverApi.saveDevice(androidDevice.getDevice());

        log.info("[{}]deviceDisconnected处理完成", deviceId);
    }

    /**
     * 首次接入获取设备的信息
     *
     * @param iDevice
     * @return
     */
    private AndroidDevice initDevice(IDevice iDevice) {
        File screenshot = null;
        try {
            Device device = new Device();
            device.setCreateTime(new Date());
            //设备id
            device.setId(iDevice.getSerialNumber());
            //cpu
            device.setCpuInfo(AndroidUtils.getCpuInfo(iDevice));
            //内存
            device.setMemSize(AndroidUtils.getMemSize(iDevice));
            //设备名
            device.setName(AndroidUtils.getDeviceName(iDevice));
            //安卓版本
            device.setSystemVersion(AndroidUtils.getAndroidVersion(iDevice));
            //屏幕分辨率
            String[] resolution = AndroidUtils.getResolution(iDevice).split("x");
            device.setScreenWidth(Integer.parseInt(resolution[0]));
            device.setScreenHeight(Integer.parseInt(resolution[1]));
            //设备类型
            device.setType(Device.ANDROID_TYPE);
            //截图并上传到服务器
            screenshot = AndroidUtils.screenshot(iDevice);
            String downloadURL = serverApi.uploadFile(screenshot);
            //首次截屏下载地址
            device.setImgUrl(downloadURL);

            AndroidDevice androidDevice = new AndroidDevice(device);
            androidDevice.setIDevice(iDevice);
            //安装minicap minitouch uiautomatorServerApk
            installMinicapAndMinitouchAndUiAutomatorServerApk(iDevice.getSerialNumber(), androidDevice);
            // 手机stf初始化成功
            device.setStfStatus(1);
            //手机macaca初始化成功
            device.setMacacaStatus(1);
            return androidDevice;
        } catch (Exception e) {
            throw new RuntimeException("初始化设备失败", e);
        } finally {
            //删除首次接入系统的截图
            if (screenshot != null) {
                try {
                    screenshot.delete();
                } catch (Exception e) {
                    //ignore
                }
            }
        }
    }

    /**
     * 安装minicap minitouch uiautomatorServerApk
     *
     * @param deviceId
     * @param androidDevice
     */
    private void installMinicapAndMinitouchAndUiAutomatorServerApk(String deviceId, AndroidDevice androidDevice) {
        try {
            //安装minicap
            log.info("[{}]开始安装minicap", deviceId);
            MinicapManager minicapManager = new MinicapManager(androidDevice);
            minicapManager.installMinicap();
            log.info("[{}]安装minicap成功", deviceId);
            //安装minitouch
            log.info("[{}]开始安装minitouch", deviceId);
            MinitouchManager minitouchManager = new MinitouchManager(androidDevice);
            minitouchManager.installMinitouch();
            log.info("[{}]安装minitouch成功", deviceId);
            //安装uiautomator2 server apk
            UiautomatorServerManager uiautomatorServerManager = new UiautomatorServerManager(androidDevice);
            uiautomatorServerManager.installServerApk();
        } catch (Exception e) {
            throw new RuntimeException("安装minicap/minitouch/UiAutomatorServerApk出错", e);
        }
    }
}
