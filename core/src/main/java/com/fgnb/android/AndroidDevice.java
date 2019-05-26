package com.fgnb.android;

import com.android.ddmlib.IDevice;
import com.fgnb.JavaCodeCompiler;
import com.fgnb.android.stf.AdbKit;
import com.fgnb.android.stf.Minicap;
import com.fgnb.android.stf.Minitouch;
import com.fgnb.android.uiautomator.Uiautomator2Server;
import com.fgnb.api.MasterApi;
import com.fgnb.model.Device;
import com.fgnb.model.action.Action;
import com.fgnb.model.action.GlobalVar;
import com.fgnb.model.devicetesttask.DeviceTestTask;
import com.fgnb.model.devicetesttask.Testcase;
import com.fgnb.testng.TestNGCodeConverter;
import com.fgnb.testng.TestNGRunner;
import com.fgnb.utils.UUIDUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Data
public class AndroidDevice {

    public final static String TMP_FOLDER = "/data/local/tmp/";

    /** 执行自动化测试任务队列 */
    private final BlockingQueue<DeviceTestTask> testTaskQueue = new LinkedBlockingQueue();
    /** 执行自动化测试任务线程 */
    private Thread excuteTestTaskThread;

    private Device device;
    private IDevice iDevice;

    private Minicap minicap;
    private Minitouch minitouch;
    private Uiautomator2Server uiautomator2Server;
    private AdbKit adbKit;

    public AndroidDevice(Device device, IDevice iDevice){
        this.device = device;
        this.iDevice = iDevice;

        excuteTestTaskThread = new Thread(()->{
            while(true){
                DeviceTestTask deviceTestTask;
                try {
                    deviceTestTask = testTaskQueue.take(); //没有测试任务，线程阻塞在此
                } catch (InterruptedException e) {
                    // 调用excuteTestTaskThread.interrupt()可以执行到这里
                    log.info("[{}][自动化测试]停止获取测试任务",getId());
                    break;
                }
                try {
                    excuteTestTask(deviceTestTask);
                } catch (Exception e) {
                    log.error("[{}][自动化测试]执行测试任务'{}'出错",getId(),deviceTestTask.getTestTaskName(),e);
                }
            }
        });
        excuteTestTaskThread.start();
    }

    /**
     * 提交测试任务
     * @param deviceTestTask
     */
    public void commitTestTask(DeviceTestTask deviceTestTask){
        if(!testTaskQueue.offer(deviceTestTask)){
            throw new RuntimeException("[自动化测试]提交测试任务'"+ deviceTestTask.getTestTaskName() +"'失败");
        }
    }

    /**
     * 设备是否连接
     * @return
     */
    public boolean isConnected() {
        return device.getStatus() != Device.OFFLINE_STATUS;
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

    /**
     * 执行测试任务
     * @param deviceTestTask
     */
    private void excuteTestTask(DeviceTestTask deviceTestTask) throws Exception{
        log.info("[{}][自动化测试]开始执行测试任务: {}",getId(),deviceTestTask.getTestTaskName());

        device.setStatus(Device.USING_STATUS);
        device.setUsername(deviceTestTask.getTestTaskName());
        MasterApi.getInstance().saveDevice(device);

        try {
            int port = uiautomator2Server.start();

            Action beforeSuite = deviceTestTask.getBeforeSuite();
            List<Testcase> testcases = deviceTestTask.getTestcases();
            List<GlobalVar> globalVars = deviceTestTask.getGlobalVars();

            List<Class> classes = new ArrayList();

            if (beforeSuite != null) {
                String beforeSuiteClassName = "BeforeSuite_" + UUIDUtil.getUUID();
                String beforeSuiteCode = new TestNGCodeConverter()
                        .setActionTree(beforeSuite)
                        .setTestClassName(beforeSuiteClassName)
                        .setIsBeforeSuite(true)
                        .setPlatform(beforeSuite.getPlatform())
                        .setDeviceId(deviceTestTask.getDeviceId())
                        .setPort(port)
                        .setGlobalVars(globalVars)
                        .setBasePackagePath("/codetemplate")
                        .setFtlFileName("testngCode.ftl")
                        .convert();
                Class beforeSuiteClass = JavaCodeCompiler.compile(beforeSuiteClassName, beforeSuiteCode);
                classes.add(beforeSuiteClass);
            }

            for(Testcase testcase: testcases) {
                String testcaseClassName = "Test_" + UUIDUtil.getUUID();
                String testcaseCode = new TestNGCodeConverter()
                        .setDeviceTestTaskId(deviceTestTask.getId())
                        .setActionTree(testcase)
                        .setTestClassName(testcaseClassName)
                        .setIsBeforeSuite(false)
                        .setPlatform(testcase.getPlatform())
                        .setDeviceId(deviceTestTask.getDeviceId())
                        .setPort(port)
                        .setGlobalVars(globalVars)
                        .setBasePackagePath("/codetemplate")
                        .setFtlFileName("testngCode.ftl")
                        .convert();
                Class testcaseClass = JavaCodeCompiler.compile(testcaseClassName, testcaseCode);
                classes.add(testcaseClass);
            }

            TestNGRunner.runTestCases(classes.toArray(new Class[classes.size()]));
        } finally {
            if(isConnected()){
                device.setStatus(Device.IDLE_STATUS);
                MasterApi.getInstance().saveDevice(device);
            }
        }
    }
}
