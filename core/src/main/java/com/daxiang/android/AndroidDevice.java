package com.daxiang.android;

import com.android.ddmlib.IDevice;
import com.daxiang.android.stf.AdbKit;
import com.daxiang.android.stf.Minicap;
import com.daxiang.android.stf.Minitouch;
import com.daxiang.android.uiautomator.Uiautomator2Server;
import com.daxiang.api.MasterApi;
import com.daxiang.javacompile.JavaCompiler;
import com.daxiang.model.Device;
import com.daxiang.model.action.Action;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import com.daxiang.testng.TestNGCodeConverter;
import com.daxiang.testng.TestNGRunner;
import com.daxiang.utils.UUIDUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Data
public class AndroidDevice {

    public final static String TMP_FOLDER = "/data/local/tmp/";

    /**
     * 执行自动化测试任务队列
     */
    private final BlockingQueue<DeviceTestTask> testTaskQueue = new LinkedBlockingQueue();
    /**
     * 执行自动化测试任务线程
     */
    private Thread excuteTestTaskThread;

    private Device device;
    private IDevice iDevice;

    private Minicap minicap;
    private Minitouch minitouch;
    private Uiautomator2Server uiautomator2Server;
    private AdbKit adbKit;

    public AndroidDevice(Device device, IDevice iDevice) {
        this.device = device;
        this.iDevice = iDevice;

        excuteTestTaskThread = new Thread(() -> {
            while (true) {
                DeviceTestTask deviceTestTask;
                try {
                    deviceTestTask = testTaskQueue.take(); //没有测试任务，线程阻塞在此
                } catch (InterruptedException e) {
                    // 调用excuteTestTaskThread.interrupt()可以执行到这里
                    log.info("[{}][自动化测试]停止获取测试任务", getId());
                    break;
                }
                try {
                    excuteTestTask(deviceTestTask);
                } catch (Exception e) {
                    log.error("[{}][自动化测试]执行测试任务'{}'出错", getId(), deviceTestTask.getTestTaskName(), e);
                }
            }
        });
        excuteTestTaskThread.start();
    }

    /**
     * 提交测试任务
     *
     * @param deviceTestTask
     */
    public void commitTestTask(DeviceTestTask deviceTestTask) {
        if (!testTaskQueue.offer(deviceTestTask)) {
            throw new RuntimeException("[自动化测试]提交测试任务'" + deviceTestTask.getTestTaskName() + "'失败");
        }
    }

    /**
     * 设备是否连接
     *
     * @return
     */
    public boolean isConnected() {
        return device.getStatus() != Device.OFFLINE_STATUS;
    }

    /**
     * 获取设备屏幕分辨率
     *
     * @return eg.1080x1920
     */
    public String getResolution() {
        return String.valueOf(device.getScreenWidth()) + "x" + String.valueOf(device.getScreenHeight());
    }

    /**
     * 获取设备id
     *
     * @return
     */
    public String getId() {
        return device.getId();
    }

    /**
     * 执行测试任务
     *
     * @param deviceTestTask
     */
    private void excuteTestTask(DeviceTestTask deviceTestTask) throws Exception {
        log.info("[{}][自动化测试]开始执行测试任务: {}", getId(), deviceTestTask.getTestTaskName());

        device.setStatus(Device.USING_STATUS);
        device.setUsername(deviceTestTask.getTestTaskName());
        MasterApi.getInstance().saveDevice(device);

        try {
            int port = uiautomator2Server.start();
            String className = "Test_" + UUIDUtil.getUUID();
            String code = new TestNGCodeConverter()
                    .setDeviceTestTaskId(deviceTestTask.getId())
                    .setDeviceId(deviceTestTask.getDeviceId())
                    .setPort(port)
                    .setGlobalVars(deviceTestTask.getGlobalVars())
                    .setBeforeClass(deviceTestTask.getBeforeClass())
                    .setAfterClass(deviceTestTask.getAfterClass())
                    .setBeforeMethod(deviceTestTask.getBeforeMethod())
                    .setAfterMethod(deviceTestTask.getAfterMethod())
                    .convert(className, deviceTestTask.getTestcases().stream().map(testcase -> {
                        Action action = new Action();
                        BeanUtils.copyProperties(testcase, action);
                        return action;
                    }).collect(Collectors.toList()), "/codetemplate", "android.ftl");
            log.info("[{}][自动化测试]转换代码：{}", getId(), code);
            // todo 捕获到DynamicCompilerException即编译失败，通知master纠正用例，否则错误的用例会无限下发给agent执行
            Class clazz = JavaCompiler.compile(className, code);
            TestNGRunner.runTestCases(new Class[]{clazz});
        } finally {
            if (isConnected()) {
                device.setStatus(Device.IDLE_STATUS);
                MasterApi.getInstance().saveDevice(device);
            }
        }
    }
}
