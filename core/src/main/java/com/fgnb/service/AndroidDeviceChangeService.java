package com.fgnb.service;

import com.android.ddmlib.IDevice;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.AndroidUtils;
import com.fgnb.android.stf.StfResourceReleaser;
import com.fgnb.android.stf.minicap.MinicapManager;
import com.fgnb.android.stf.minitouch.MinitouchManager;
import com.fgnb.android.uiautomator.UiautomatorServerManager;
import com.fgnb.api.UIServerApi;
import com.fgnb.bean.Device;
import com.fgnb.enums.DeviceMacacaStatus;
import com.fgnb.enums.DeviceStatus;
import com.fgnb.enums.DeviceStfStatus;
import com.fgnb.enums.DeviceType;
import com.fgnb.init.AppicationContextRegister;
import com.fgnb.utils.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Date;


/**
 * Created by jiangyitao.
 * 安卓设备连接和断开连接服务
 */
@Service
@Slf4j
public class AndroidDeviceChangeService {


    @Autowired
    private UIServerApi uiServerApi;

    /**
     * 设备连接上，调用的方法
     * 注意这里是多线程调用，尽量不要用成员变量
     *
     * @param iDevice
     * @throws Exception
     */
    public void deviceConnected(IDevice iDevice) {

        String deviceId = iDevice.getSerialNumber();
        log.info("[{}]已连接", deviceId);

        //等待设备上线
        try {
            log.info("[{}]等待手机上线", deviceId);
            AndroidUtils.waitForDeviceOnline(iDevice, 10);
            log.info("[{}]手机已上线", deviceId);
        } catch (Exception e) {
            throw new RuntimeException("等待手机上线出错", e);
        }

        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        if (androidDevice == null) {//该agent未接入过该手机
            //第一次上线
            log.info("[{}]首次上线", deviceId);
            Device device = checkDeviceIsFirstAccessSystem(deviceId);
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
        //设备ip地址
        device.setPhoneIp(AndroidUtils.getIp(iDevice));
        //agent ip
        try {
            device.setAgentIp(NetUtil.getLocalHostAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException("[" + deviceId + "]获取agent ip失败", e);
        }
        //agent port
        device.setAgentPort(Integer.parseInt(AppicationContextRegister.getApplicationContext().getEnvironment().getProperty("server.port")));
        //闲置状态
        device.setStatus(DeviceStatus.IDLE.getStatus());
        //最后一次在线时间
        device.setLastOnlineTime(new Date());
        //上报服务器
        try {
            uiServerApi.save(device);
        } catch (Exception e) {
            throw new RuntimeException("[" + deviceId + "]保存手机信息到服务器失败", e);
        }
        //将手机状态改为已连接
        androidDevice.setIsConnected(true);
        log.info("[{}]deviceConnected处理完成", deviceId);

    }

    /**
     * 设备断开连接，调用的方法
     * 注意这里是多线程调用，尽量不要用成员变量
     *
     * @param iDevice
     */
    public void deviceDisconnected(IDevice iDevice) {
        //设备id
        String deviceId = iDevice.getSerialNumber();

        log.info("[{}]断开连接", deviceId);

        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        //有可能刚连上就断了 androidDevice还没初始化
        if (androidDevice == null) {
            return;
        }
        //将手机状态改为断开
        androidDevice.setIsConnected(false);

        //手机断开 回收minicap/minitouch/adbkit等占用的资源，如关闭输入输出流，端口释放等
        StfResourceReleaser stfResourceReleaser = new StfResourceReleaser(deviceId);
        stfResourceReleaser.release();

        androidDevice.getDevice().setStatus(DeviceStatus.OFFLINE.getStatus());
        androidDevice.getDevice().setLastOfflineTime(new Date());
        try {
            uiServerApi.save(androidDevice.getDevice());
        } catch (Exception e) {
            throw new RuntimeException("[" + deviceId + "]手机离线失败", e);
        }

        log.info("[{}]deviceDisconnected处理完成", deviceId);
    }

    /**
     * 手机是否首次接入系统
     *
     * @param deviceId
     * @return
     */
    private Device checkDeviceIsFirstAccessSystem(String deviceId) {
        try {
            Device device = uiServerApi.findById(deviceId);
            return device;
        } catch (Exception e) {
            throw new RuntimeException("[" + deviceId + "]检查手机是否首次接入系统出错", e);
        }
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
            device.setDeviceId(iDevice.getSerialNumber());
            // api level
            device.setApiLevel(AndroidUtils.getApiLevel(iDevice));
            // cpu架构
            device.setCpuAbi(AndroidUtils.getCpuAbi(iDevice));
            //cpu
            device.setCpuInfo(AndroidUtils.getCpuInfo(iDevice));
            //内存
            device.setMemSize(AndroidUtils.getMemSize(iDevice));
            //设备名
            device.setDeviceName(AndroidUtils.getDeviceName(iDevice));
            //安卓版本
            device.setSystemVersion(AndroidUtils.getAndroidVersion(iDevice));
            //屏幕分辨率
            device.setResolution(AndroidUtils.getResolution(iDevice));
            //设备类型
            device.setDeviceType(DeviceType.ANDROID.getType());
            //截图并上传到服务器
            screenshot = AndroidUtils.screenshot(iDevice);
            String downloadURL = uiServerApi.uploadFile(screenshot);
            //首次截屏下载地址
            device.setImgUrl(downloadURL);

            AndroidDevice androidDevice = new AndroidDevice(device);
            androidDevice.setIDevice(iDevice);
            //安装minicap minitouch uiautomatorServerApk
            installMinicapAndMinitouchAndUiAutomatorServerApk(iDevice.getSerialNumber(), androidDevice);
            // 手机stf初始化成功
            device.setStfStatus(DeviceStfStatus.SUCCESS.getStatus());
            //手机macaca初始化成功
            device.setMacacaStatus(DeviceMacacaStatus.SUCCESS.getStatus());
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
