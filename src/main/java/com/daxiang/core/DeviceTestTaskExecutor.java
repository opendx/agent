package com.daxiang.core;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.core.testng.TestNGCodeConverterFactory;
import com.daxiang.server.ServerClient;
import com.daxiang.core.testng.TestNGCodeConvertException;
import com.daxiang.core.testng.TestNGRunner;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dvare.dynamic.exceptions.DynamicCompilerException;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.util.StringUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class DeviceTestTaskExecutor {

    private final BlockingQueue<DeviceTestTask> testTaskQueue = new LinkedBlockingQueue<>();

    private Thread executeTestTaskThread;
    private Device device;

    public DeviceTestTaskExecutor(Device device) {
        this.device = device;

        executeTestTaskThread = new Thread(() -> {
            DeviceTestTask deviceTestTask;
            while (true) {
                try {
                    deviceTestTask = testTaskQueue.take(); // 没有测试任务，线程阻塞在此
                } catch (InterruptedException e) {
                    // 调用executeTestTaskThread.interrupt()可以执行到这里
                    log.info("[{}]停止获取测试任务", device.getId());
                    break;
                }

                try {
                    executeTestTask(deviceTestTask);
                } catch (Throwable e) {
                    log.error("[{}]执行测试任务出错, deviceTestTaskId: {}", device.getId(), deviceTestTask.getId(), e);
                }
            }
        });
        executeTestTaskThread.start();
    }

    public void commitTestTask(DeviceTestTask deviceTestTask) {
        if (!testTaskQueue.offer(deviceTestTask)) {
            throw new RuntimeException(String.format("[%s]提交测试任务失败, deviceTestTaskId: %d", device.getId(), deviceTestTask.getId()));
        }
    }

    private void executeTestTask(DeviceTestTask deviceTestTask) {
        log.info("[{}]开始执行测试任务, deviceTestTaskId: {}", device.getId(), deviceTestTask.getId());

        // device变为使用中
        device.usingToServer(deviceTestTask.getTestPlan().getName());

        try {
            String className = "Test_" + UUIDUtil.getUUID();
            String code = TestNGCodeConverterFactory.create(deviceTestTask.getPlatform())
                    .convert(deviceTestTask, className);
            updateDeviceTestTaskCode(deviceTestTask.getId(), code);

            Class clazz = JavaCompiler.compile(className, code);

            Capabilities caps = null;
            String capsString = deviceTestTask.getCapabilities();
            if (StringUtils.hasText(capsString)) {
                try {
                    caps = new DesiredCapabilities(JSONObject.parseObject(capsString));
                } catch (Exception e) {
                    log.warn("[{}]parse caps fail, caps: {}, deviceTestTaskId: {}", device.getId(), capsString, deviceTestTask.getId(), e);
                }
            }

            device.freshDriver(caps, true);

            TestNGRunner.runTestCases(new Class[]{clazz}, deviceTestTask.getTestPlan().getFailRetryCount());
        } catch (TestNGCodeConvertException | DynamicCompilerException e) {
            log.error("[{}]executeTestTask err, deviceTestTaskId: {}", device.getId(), deviceTestTask.getId(), e);
            updateDeviceTestTaskErrMsg(deviceTestTask.getId(), ExceptionUtils.getStackTrace(e));
        } finally {
            device.quitDriver();
            device.idleToServer();
        }
    }

    private void updateDeviceTestTaskCode(Integer deviceTestTaskId, String code) {
        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(deviceTestTaskId);
        deviceTestTask.setCode(code);
        ServerClient.getInstance().updateDeviceTestTask(deviceTestTask);
    }

    private void updateDeviceTestTaskErrMsg(Integer deviceTestTaskId, String errMsg) {
        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(deviceTestTaskId);
        deviceTestTask.setStatus(DeviceTestTask.ERROR_STATUS);
        deviceTestTask.setErrMsg(errMsg);
        ServerClient.getInstance().updateDeviceTestTask(deviceTestTask);
    }
}
