package com.fgnb.android;

import com.android.ddmlib.IDevice;
import com.fgnb.android.uiautomator.UiautomatorServerManager;
import com.fgnb.api.UIServerApi;
import com.fgnb.model.Device;
import com.fgnb.excutor.Excutor;
import com.fgnb.init.AppicationContextRegister;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AndroidDevice {

    //任务队列 执行自动化测试任务
    private BlockingQueue<Map<String,String>> taskQueue = new LinkedBlockingQueue();

    private Device device;

    private IDevice iDevice;

    private int deviceHeight;
    private int deviceWidth;

    private UiautomatorServerManager uiautomatorServerManager;

    private boolean isConnected = false;

    private UIServerApi uiServerApi = AppicationContextRegister.getApplicationContext().getBean(UIServerApi.class);

    public AndroidDevice(Device device){
        this.device = device;
        String[] resolution = device.getResolution().split("x");
        deviceWidth =Integer.parseInt(resolution[0]);
        deviceHeight = Integer.parseInt(resolution[1]);
        //开启一个线程 专门执行推送过来的自动化测试任务
        new Thread(()->{
            while(true){
                try {
                    Map<String,String> codes = taskQueue.take();
                    log.info("[{}]准备执行测试任务",device.getId());

                    Excutor excutor = new Excutor();

                    List<Class> classes = new ArrayList();
                    for(String fullClassName:codes.keySet()){
                        //编译测试代码
                        Class clazz = excutor.compiler(fullClassName, codes.get(fullClassName));
                        classes.add(clazz);
                    }
                    log.info("[{}]开始执行测试任务",device.getId());
                    excutor.runTestCasesByTestNG(classes.toArray(new Class[classes.size()]));
                    log.info("[{}]执行测试任务完成",device.getId());
                } catch (Exception e) {
                    log.error("[{}]执行测试任务出现出错",device.getId(),e);
                } finally {
                    //如果设备还处于连接电脑状态 则将设备改为闲置
                    if(isConnected){
                        if(uiautomatorServerManager != null){
                            //停掉执行自动化测试的uiautomatorserver
                            uiautomatorServerManager.stopServer();
                        }
                        device.setStatus(Device.IDLE_STATUS);
                        try {
                            uiServerApi.save(device);
                        } catch (Exception e) {
                            log.error("保存手机失败",e);
                        }
                    }
                }
            }
        }).start();
    }
    public void addTask(Map<String,String> codes){
        if(!taskQueue.offer(codes)){
            throw new RuntimeException("添加测试任务失败");
        }
    }

    public UiautomatorServerManager getUiautomatorServerManager() {
        return uiautomatorServerManager;
    }

    public void setUiautomatorServerManager(UiautomatorServerManager uiautomatorServerManager) {
        this.uiautomatorServerManager = uiautomatorServerManager;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean connected) {
        isConnected = connected;
    }

    public int getDeviceHeight() {
        return deviceHeight;
    }

    public int getDeviceWidth() {
        return deviceWidth;
    }

    public IDevice getIDevice() {
        return iDevice;
    }

    public void setIDevice(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    public Device getDevice(){
        return device;
    }
}
