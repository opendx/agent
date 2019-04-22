package com.fgnb.android.stf.minitouch;

import com.android.ddmlib.NullOutputReceiver;
import com.fgnb.android.AndroidDevice;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class MinitouchX {


    public static final String START_MINITOUCH_SHELL = AndroidDevice.TMP_FOLDER + "minitouch";

    public MinitouchX() {

    }

    public void start() {
        //需要开线程启动minitouch 因为executeShellCommand(START_MINITOUCH_SHELL) 后线程会阻塞在此处
        new Thread(() -> {
            try {
                log.info("[{}]start minitouch service，exec => {},thread id => {}",deviceId,START_MINITOUCH_SHELL,Thread.currentThread().getId());
                iDevice.executeShellCommand(START_MINITOUCH_SHELL, new NullOutputReceiver(),0, TimeUnit.SECONDS);
                log.info("[{}]minitouch service stopped",deviceId);
            } catch (Exception e) {
                log.error("[{}]minitouch执行异常",deviceId,e);
            }
        }).start();
    }
}
