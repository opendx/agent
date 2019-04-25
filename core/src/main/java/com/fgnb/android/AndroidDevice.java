package com.fgnb.android;

import com.android.ddmlib.IDevice;
import com.fgnb.android.stf.adbkit.AdbKit;
import com.fgnb.android.stf.minicap.Minicap;
import com.fgnb.android.stf.minitouch.Minitouch;
import com.fgnb.android.uiautomator.Uiautomator2Server;
import com.fgnb.api.ServerApi;
import com.fgnb.model.Device;
import com.fgnb.excutor.Excutor;
import com.fgnb.App;
import lombok.Data;
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
@Data
public class AndroidDevice {

    public final static String TMP_FOLDER = "/data/local/tmp/";

    //任务队列 执行自动化测试任务
    private BlockingQueue<Map<String,String>> taskQueue = new LinkedBlockingQueue();

    private Device device;
    private IDevice iDevice;

    private Minicap minicap;
    private Minitouch minitouch;
    private Uiautomator2Server uiautomator2Server;
    private AdbKit adbKit;

    public AndroidDevice(Device device, IDevice iDevice){
        this.device = device;
        this.iDevice = iDevice;

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
                    if(isConnected()){
                        if(uiautomator2Server != null){
                            //停掉执行自动化测试的uiautomatorserver
                            uiautomator2Server.stop();
                        }
                        device.setStatus(Device.IDLE_STATUS);
                        App.getBean(ServerApi.class).saveDevice(device);
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

    /**
     * 设备是否连接
     * @return
     */
    public boolean isConnected() {
        return device.getStatus() == Device.OFFLINE_STATUS ? false : true;
    }

    /**
     * 获取设备屏幕分辨率
     * @return eg.1080x1920
     */
    public String getResolution() {
        return String.valueOf(device.getScreenWidth()) + "x" + String.valueOf(device.getScreenHeight());
    }

    /**
     * 获取设备id
     * @return
     */
    public String getId() {
        return device.getId();
    }
}
