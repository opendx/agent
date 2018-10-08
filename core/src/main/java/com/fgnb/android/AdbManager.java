package com.fgnb.android;

import com.android.ddmlib.AndroidDebugBridge;
import com.fgnb.utils.ShellExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AdbManager {

    private static AndroidDebugBridge androidDebugBridge = null;

    private static final String ADB_PLATFORM_TOOLS = "platform-tools";

    /**
     * 添加Android设备监听器
     * @param deviceChangeListener
     */
    public static void addDeviceChangeListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener){
        AndroidDebugBridge.addDeviceChangeListener(deviceChangeListener);
        log.info("DeviceChangeListener added");
    }

    /**
     * 杀掉adb服务
     */
    public static void killAdbServer(){
        try {
            log.info("start kill adb-server");
            ShellExecutor.exec("adb kill-server");
            log.info("kill adb-server success");
        } catch (IOException e) {
            log.error("kill adb server出错",e);
        }
    }

    /**
     * 获取ADB
     * @return
     * @throws Exception
     */
    public static synchronized AndroidDebugBridge getAdb() throws Exception {
        if(androidDebugBridge == null){
            log.info("init adb");
            //初始化adb
            AndroidDebugBridge.init(false);
            log.info("createing adb ...");
            androidDebugBridge = AndroidDebugBridge.createBridge(getAdbPath(), false);
            //等待adb连接 10s
            log.info("wait for adb connected");
            long start = System.currentTimeMillis();
            while(System.currentTimeMillis()-start<=10000){
                if(androidDebugBridge.isConnected()){
                    log.info("adb connected");
                    return androidDebugBridge;
                }
            }
        }
        log.info("adb has inited");
        return androidDebugBridge;
    }
    /**
     * 获取adb的位置
     * @return
     * @throws Exception
     */
    private static String getAdbPath() throws Exception{
        String adbPath = System.getenv("ANDROID_HOME");
        log.info("ANDROID_HOME ==> {}",adbPath);
        if(StringUtils.isEmpty(adbPath)){
            log.error("环境变量未配置ANDROID_HOME");
            throw new RuntimeException("环境变量未配置ANDROID_HOME");
        }
        adbPath = adbPath + File.separator + ADB_PLATFORM_TOOLS + File.separator+"adb";
        log.info("adbPath ==> {}",adbPath);
        return adbPath;
    }
}
