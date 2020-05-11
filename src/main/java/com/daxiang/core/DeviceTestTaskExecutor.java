package com.daxiang.core;

import com.alibaba.fastjson.JSONObject;
import com.daxiang.server.ServerClient;
import com.daxiang.core.testng.TestNGCodeConvertException;
import com.daxiang.core.testng.TestNGCodeConverter;
import com.daxiang.core.testng.TestNGRunner;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dvare.dynamic.exceptions.DynamicCompilerException;
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
                    log.info("[自动化测试][{}]停止获取测试任务", device.getId());
                    break;
                }

                try {
                    executeTestTask(deviceTestTask);
                } catch (Throwable e) {
                    log.error("[自动化测试][{}]执行测试任务出错, deviceTestTaskId: {}", device.getId(), deviceTestTask.getId(), e);
                }
            }
        });
        executeTestTaskThread.start();
    }

    public void commitTestTask(DeviceTestTask deviceTestTask) {
        if (!testTaskQueue.offer(deviceTestTask)) {
            throw new RuntimeException("提交测试任务失败, deviceTestTaskId: " + deviceTestTask.getId());
        }
    }

    private void executeTestTask(DeviceTestTask deviceTestTask) {
        log.info("[自动化测试][{}]开始执行测试任务, deviceTestTaskId: {}", device.getId(), deviceTestTask.getId());

        // 设备变为使用中
        device.usingToServer(deviceTestTask.getTestPlan().getName());

        try {
            String className = "Test_" + UUIDUtil.getUUID();
            String code = new TestNGCodeConverter().convert(deviceTestTask, className);
            updateDeviceTestTaskCode(deviceTestTask.getId(), code);

            Class clazz = JavaCompiler.compile(className, code);

            JSONObject caps = null;
            try {
                if (StringUtils.hasText(deviceTestTask.getCapabilities())) {
                    caps = JSONObject.parseObject(deviceTestTask.getCapabilities());
                }
            } catch (Exception e) {
                log.warn("parse capabilities fail, deviceTestTask: {}", deviceTestTask, e);
            }
            device.freshDriver(caps);

            TestNGRunner.runTestCases(new Class[]{clazz}, deviceTestTask.getTestPlan().getFailRetryCount());
        } catch (TestNGCodeConvertException | DynamicCompilerException e) {
            log.error("[自动化测试][{}]deviceTestTaskId: {}", device.getId(), deviceTestTask.getId(), e);
            updateDeviceTestTaskStatusAndErrMsg(deviceTestTask.getId(), DeviceTestTask.ERROR_STATUS, ExceptionUtils.getStackTrace(e));
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

    private void updateDeviceTestTaskStatusAndErrMsg(Integer deviceTestTaskId, Integer status, String errMsg) {
        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(deviceTestTaskId);
        deviceTestTask.setStatus(status);
        deviceTestTask.setErrMsg(errMsg);
        ServerClient.getInstance().updateDeviceTestTask(deviceTestTask);
    }
}
