package com.fgnb.android.stf;

import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.fgnb.android.AndroidDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class MinicapScreenShoter {

    private static final String MINICAP_SCREENSHOT_PATH = "/data/local/tmp/minicap.jpg";
    private static final String MINICAP_SCREENSHOT_CMD = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/0 -s >"+MINICAP_SCREENSHOT_PATH;

    /**
     * minicap截图
     * @param localPath
     * @param androidDevice
     * @throws Exception
     */
    public static void takeScreenShot(String localPath,AndroidDevice androidDevice) throws Exception{

        String deviceId = androidDevice.getDevice().getId();

        //检查设备连接状态
        if(!androidDevice.isConnected()){
            log.info("[{}]设备未连接",deviceId);
            throw new RuntimeException("设备未连接，无法截图");
        }

        IDevice iDevice = androidDevice.getIDevice();
        //使用minicap截图
        String minicapScreenShotCmd = String.format(MINICAP_SCREENSHOT_CMD,androidDevice.getDevice().getResolution(),androidDevice.getDevice().getResolution());
        log.info("[{}]minicap截图命令:{}",deviceId,minicapScreenShotCmd);
        CollectingOutputReceiver minicapReceiver = new CollectingOutputReceiver();
        iDevice.executeShellCommand(minicapScreenShotCmd,minicapReceiver);
        String minicapOutput = minicapReceiver.getOutput();
        log.info("[{}]minicap截图 output => {}",deviceId,minicapOutput);
        if(StringUtils.isEmpty(minicapOutput) || !minicapOutput.contains("bytes for JPG encoder")){
            //截图失败
            log.error("[{}]minicap截图失败",deviceId);
            throw new RuntimeException("minicap截图失败，执行minicap截图命令输出:"+minicapOutput);
        }
        log.info("[{}]minicap截图成功,截图位置:{}",deviceId,MINICAP_SCREENSHOT_PATH);

        //pull到本地
        androidDevice.getIDevice().pullFile(MINICAP_SCREENSHOT_PATH,localPath);
    }
}
