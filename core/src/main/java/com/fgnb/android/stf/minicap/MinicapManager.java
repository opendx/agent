package com.fgnb.android.stf.minicap;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.fgnb.android.AndroidDevice;
import com.fgnb.App;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;


/**
 * Created by jiangyitao.
 * Minicap管理器
 */
@Slf4j
public class MinicapManager {

    private static final String START_MINICAP_SHELL = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/0";


    private AndroidDevice androidDevice;



    /**
     * minicap端口
     */
    private int minicapPort = -1;

    public MinicapManager(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
    }

    /**
     * 开启安卓手机里的minicap输出屏幕数据
     *
     * @return
     * @throws Exception
     */
    public void startMiniCap() throws Exception {


    }


    public int getMinicapPort() {
        return minicapPort;
    }



    /**
     * 获取屏幕在网页上显示的分辨率
     *
     * @return
     */
    private String getScreenDisplayResolution() throws Exception {
        int screenDisplayHeight = Integer.parseInt(App.getProperty("screenDisplayHeight"));
        //比例
        double scale = (double) screenDisplayHeight / androidDevice.getDevice().getScreenHeight();
        int screenDisplayWidth = (int) Math.round(androidDevice.getDevice().getScreenWidth() * scale);
        return screenDisplayWidth + "x" + screenDisplayHeight;
    }

}
