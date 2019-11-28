package com.daxiang.core.testng.listener;

import com.daxiang.core.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.api.MasterApi;
import com.daxiang.model.action.Step;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import com.daxiang.model.devicetesttask.Testcase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class TestCaseTestListener extends TestListenerAdapter {

    private static final ThreadLocal<MobileDevice> TL_MOBILE_DEVICE = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TL_DEVICE_TEST_TASK_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TL_TEST_CASE_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> TL_NEED_RECORD_VIDEO = new ThreadLocal<>();

    /**
     * 每个设备开始测试调用的方法，这里可能有多个线程同时调用
     *
     * @param testContext
     */
    @Override
    public void onStart(ITestContext testContext) {
        // deviceId_deviceTestTaskId_testcaseId_enableRecordVideo
        String[] testDesc = testContext.getAllTestMethods()[0].getDescription().split("_");
        String deviceId = testDesc[0];
        Integer deviceTestTaskId = Integer.parseInt(testDesc[1]);
        Integer enableRecordVideo = Integer.parseInt(testDesc[3]);
        Boolean needRecordVideo = enableRecordVideo == DeviceTestTask.ENABLE_RECORD_VIDEO;

        MobileDevice mobileDevice = MobileDeviceHolder.get(deviceId);
        log.info("[自动化测试][{}]onStart, deviceTestTaskId：{}, needRecordVideo: {}", deviceId, deviceTestTaskId, needRecordVideo);

        TL_MOBILE_DEVICE.set(mobileDevice);
        TL_DEVICE_TEST_TASK_ID.set(deviceTestTaskId);
        TL_NEED_RECORD_VIDEO.set(needRecordVideo);

        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(deviceTestTaskId);
        deviceTestTask.setStartTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.RUNNING_STATUS);
        MasterApi.getInstance().updateDeviceTestTask(deviceTestTask);
    }

    /**
     * 每个设备结束测试调用的方法，这里可能有多个线程同时调用
     *
     * @param testContext
     */
    @Override
    public void onFinish(ITestContext testContext) {
        MobileDevice mobileDevice = TL_MOBILE_DEVICE.get();
        String deviceId = mobileDevice.getId();
        Integer deviceTestTaskId = TL_DEVICE_TEST_TASK_ID.get();

        log.info("[自动化测试][{}]onFinish, deviceTestTaskId: {}", deviceId, deviceTestTaskId);
        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(deviceTestTaskId);
        deviceTestTask.setEndTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.FINISHED_STATUS);
        MasterApi.getInstance().updateDeviceTestTask(deviceTestTask);
    }

    /**
     * 每个设备执行每条测试用例前调用的方法，这里可能有多个线程同时调用
     * 目前设计的：每条用例单独录制视频
     *
     * @param tr
     */
    @Override
    public void onTestStart(ITestResult tr) {
        Integer deviceTestTaskId = TL_DEVICE_TEST_TASK_ID.get();
        Boolean needRecordVideo = TL_NEED_RECORD_VIDEO.get();
        MobileDevice mobileDevice = TL_MOBILE_DEVICE.get();
        String deviceId = mobileDevice.getId();
        // deviceId_deviceTestTaskId_testcaseId_enableRecordVideo
        Integer testcaseId = Integer.parseInt(tr.getMethod().getDescription().split("_")[2]);
        TL_TEST_CASE_ID.set(testcaseId);

        log.info("[自动化测试][{}]onTestStart, testcaseId: {}", deviceId, testcaseId);

        Testcase testcase = new Testcase();
        testcase.setId(testcaseId);
        testcase.setStartTime(new Date());
        MasterApi.getInstance().updateTestcase(deviceTestTaskId, testcase);

        if (needRecordVideo) {
            try {
                log.info("[自动化测试][{}]testcaseId: {}, 开始录制视频...", deviceId, testcaseId);
                mobileDevice.startRecordingScreen();
            } catch (Exception e) {
                log.error("[自动化测试][{}]testcaseId: {}, 启动录制视频失败", deviceId, testcaseId, e);
                TL_NEED_RECORD_VIDEO.set(false);
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        MobileDevice mobileDevice = TL_MOBILE_DEVICE.get();
        Integer testcaseId = TL_TEST_CASE_ID.get();
        log.info("[自动化测试][{}]onTestSuccess, testcaseId: {}", mobileDevice.getId(), testcaseId);

        Testcase testcase = new Testcase();
        testcase.setId(testcaseId);
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.PASS_STATUS);
        testcase.setVideoUrl(getVideoDownloadUrl());
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        MobileDevice mobileDevice = TL_MOBILE_DEVICE.get();
        Integer testcaseId = TL_TEST_CASE_ID.get();
        log.error("[自动化测试][{}]onTestFailure, testcaseId: {}", mobileDevice.getId(), testcaseId, tr.getThrowable());

        Testcase testcase = new Testcase();
        testcase.setId(testcaseId);
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.FAIL_STATUS);
        testcase.setFailImgUrl(getScreenshotDownloadUrl());
        testcase.setFailInfo(ExceptionUtils.getStackTrace(tr.getThrowable()));
        testcase.setVideoUrl(getVideoDownloadUrl());
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    /**
     * 当@BeforeClass或@BeforeMethod抛出异常时，所有@Test都不会执行，且所有Test都会先调用onTestStart然后直接调用onTestSkipped，且onTestSkipped tr.getThrowable为null
     * Test抛出的SkipException，tr.getThrowable不为空，能获取到跳过的原因
     *
     * @param tr
     */
    @Override
    public void onTestSkipped(ITestResult tr) {
        MobileDevice mobileDevice = TL_MOBILE_DEVICE.get();
        Integer testcaseId = TL_TEST_CASE_ID.get();
        log.warn("[自动化测试][{}]onTestSkipped, testcaseId: {}", mobileDevice.getId(), testcaseId, tr.getThrowable());

        Testcase testcase = new Testcase();
        testcase.setId(testcaseId);
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.SKIP_STATUS);
        testcase.setFailImgUrl(getScreenshotDownloadUrl());
        if (tr.getThrowable() == null) { // @BeforeClass或@BeforeMethod抛出异常导致的case跳过
            testcase.setFailInfo("前置任务执行失败");
        } else {
            testcase.setFailInfo(tr.getThrowable().getMessage());
        }
        testcase.setVideoUrl(getVideoDownloadUrl());
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    private String getScreenshotDownloadUrl() {
        MobileDevice mobileDevice = TL_MOBILE_DEVICE.get();
        try {
            return mobileDevice.screenshotAndUploadToMaster();
        } catch (Exception e) {
            log.error("[自动化测试][{}]testcaseId: {}，截图并上传到master失败", mobileDevice.getId(), TL_TEST_CASE_ID.get(), e);
            return null;
        }
    }

    private String getVideoDownloadUrl() {
        if (!TL_NEED_RECORD_VIDEO.get()) {
            return null;
        }

        MobileDevice mobileDevice = TL_MOBILE_DEVICE.get();
        String deviceId = mobileDevice.getId();
        Integer testcaseId = TL_TEST_CASE_ID.get();

        try {
            log.info("[自动化测试][{}]testcaseId: {}, 停止录制视频...", deviceId, testcaseId);
            long startTime = System.currentTimeMillis();
            String downloadUrl = mobileDevice.stopRecordingScreenAndUploadToMaster();
            log.info("[自动化测试][{}]testcaseId: {}, 停止录制视频并上传到master完成，耗时: {} ms", deviceId, testcaseId, System.currentTimeMillis() - startTime);
            return downloadUrl;
        } catch (Exception e) {
            log.error("[自动化测试][{}]testcaseId: {}，stopRecordingScreenAndUploadToMaster err", deviceId, testcaseId, e);
            return null;
        }
    }

    /**
     * 提供给actions.ftl调用，记录用例步骤的执行开始/结束时间
     */
    public static void recordTestCaseStepTime(Integer actionId, String startOrEnd, Integer stepNumber) {
        Integer testcaseId = TL_TEST_CASE_ID.get();
        // 只记录当前正在执行的测试用例里的步骤
        if (!actionId.equals(testcaseId)) {
            return;
        }

        Step step = new Step();
        if ("start".equals(startOrEnd)) {
            step.setStartTime(new Date());
        } else if ("end".equals(startOrEnd)) {
            step.setEndTime(new Date());
        }
        step.setNumber(stepNumber);

        Testcase testcase = new Testcase();
        testcase.setId(testcaseId);
        testcase.setSteps(Arrays.asList(step));
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }
}