package com.fgnb.service;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.stf.minicap.MinicapManager;
import com.fgnb.android.stf.minitouch.MinitouchManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class AndroidDeviceService {

    /**
     * 开启minitouch服务
     * @param deviceId
     * @return
     * @throws Exception
     */
    public MinitouchManager startMinitouchService(String deviceId) throws Exception{
        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        if(androidDevice == null){
            throw new RuntimeException("通过"+deviceId+"在AndroidDevice未获取到AndroidDevice对象");
        }

        MinitouchManager minitouchManager = new MinitouchManager(androidDevice);

        //1.端口转发到minitouch服务
        minitouchManager.createForward();
        log.info("[{}]createforward : {} => localabstract:minitouch",deviceId,minitouchManager.getMinitouchPort());

        //2.启动minitouch服务
        minitouchManager.startMinitouch();
        log.info("[{}]minitouch started,local port => {}",deviceId,minitouchManager.getMinitouchPort());

        return minitouchManager;
    }

    public MinicapManager startMiniCapService(String deviceId) throws Exception{

        AndroidDevice androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        if(androidDevice == null){
            throw new RuntimeException("通过"+deviceId+"在AndroidDevice未获取到AndroidDevice对象");
        }

        MinicapManager minicapManager = new MinicapManager(androidDevice);

        //1.端口转发到minicap服务
        minicapManager.createForward();
        log.info("[{}]createforward : {} => localabstract:minicap",deviceId,minicapManager.getMinicapPort());

        //2.启动minicap服务
        minicapManager.startMiniCap();
        log.info("[{}]minicap started,local port => {}",deviceId,minicapManager.getMinicapPort());

        return minicapManager;
    }


}
