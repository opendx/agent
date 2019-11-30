package com.daxiang.core.testng;

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

    public static final String TEST_DESCRIPTION = "test_desc";
    private static final MasterApi MASTER_API = MasterApi.getInstance();
    private static final ThreadLocal<Integer> CURRENT_TEST_CASE_ID = new ThreadLocal<>();

    /**
     * 每个设备开始测试调用的方法，这里可能有多个线程同时调用
     *
     * @param testContext
     */
    @Override
    public void onStart(ITestContext testContext) {
        TestDescription testDesc = new TestDescription(testContext.getAllTestMethods()[0].getDescription());
        log.info("[自动化测试][{}]onStart, deviceTestTaskId：{}, recordVideo: {}", testDesc.getDeviceId(), testDesc.getDeviceTestTaskId(), testDesc.getRecordVideo());
        testContext.setAttribute(TEST_DESCRIPTION, testDesc);

        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(testDesc.getDeviceTestTaskId());
        deviceTestTask.setStartTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.RUNNING_STATUS);
        MASTER_API.updateDeviceTestTask(deviceTestTask);
    }

    /**
     * 每个设备结束测试调用的方法，这里可能有多个线程同时调用
     *
     * @param testContext
     */
    @Override
    public void onFinish(ITestContext testContext) {
        TestDescription testDesc = (TestDescription) testContext.getAttribute(TEST_DESCRIPTION);
        log.info("[自动化测试][{}]onFinish, deviceTestTaskId: {}", testDesc.getDeviceId(), testDesc.getDeviceTestTaskId());

        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(testDesc.getDeviceTestTaskId());
        deviceTestTask.setEndTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.FINISHED_STATUS);
        MASTER_API.updateDeviceTestTask(deviceTestTask);
    }

    /**
     * 每个设备执行每条测试用例前调用的方法，这里可能有多个线程同时调用
     *
     * @param tr
     */
    @Override
    public void onTestStart(ITestResult tr) {
        TestDescription testDesc = (TestDescription) tr.getTestContext().getAttribute(TEST_DESCRIPTION);
        Integer testcaseId = TestDescription.parseTestcaseId(tr.getMethod().getDescription());
        testDesc.setTestcaseId(testcaseId);
        CURRENT_TEST_CASE_ID.set(testcaseId);
        log.info("[自动化测试][{}]onTestStart, testcaseId: {}", testDesc.getDeviceId(), testcaseId);

        Testcase testcase = new Testcase();
        testcase.setId(testcaseId);
        testcase.setStartTime(new Date());
        MASTER_API.updateTestcase(testDesc.getDeviceTestTaskId(), testcase);

        if (testDesc.getRecordVideo()) {
            try {
                log.info("[自动化测试][{}]testcaseId: {}, 开始录制视频...", testDesc.getDeviceId(), testcaseId);
                testDesc.getMobileDevice().startRecordingScreen();
            } catch (Exception e) {
                log.error("[自动化测试][{}]testcaseId: {}, 启动录制视频失败", testDesc.getDeviceId(), testcaseId, e);
                testDesc.setRecordVideo(false);
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        TestDescription testDesc = (TestDescription) tr.getTestContext().getAttribute(TEST_DESCRIPTION);
        log.info("[自动化测试][{}]onTestSuccess, testcaseId: {}", testDesc.getDeviceId(), testDesc.getTestcaseId());

        Testcase testcase = new Testcase();
        testcase.setId(testDesc.getTestcaseId());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.PASS_STATUS);
        testcase.setVideoUrl(getVideoDownloadUrl(testDesc));
        MASTER_API.updateTestcase(testDesc.getDeviceTestTaskId(), testcase);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        TestDescription testDesc = (TestDescription) tr.getTestContext().getAttribute(TEST_DESCRIPTION);
        log.error("[自动化测试][{}]onTestFailure, testcaseId: {}", testDesc.getDeviceId(), testDesc.getTestcaseId());

        Testcase testcase = new Testcase();
        testcase.setId(testDesc.getTestcaseId());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.FAIL_STATUS);
        testcase.setFailImgUrl(getScreenshotDownloadUrl(testDesc));
        testcase.setFailInfo(ExceptionUtils.getStackTrace(tr.getThrowable()));
        testcase.setVideoUrl(getVideoDownloadUrl(testDesc));
        MASTER_API.updateTestcase(testDesc.getDeviceTestTaskId(), testcase);
    }

    /**
     * 当@BeforeClass抛出异常后，所有@Test都不会执行，且所有@Test都会先调用onTestStart然后直接调用onTestSkipped
     * 当@BeforeMethod抛出异常后，当前将要执行的@Test不会执行，且会先调用onTestStart然后直接调用onTestSkipped
     * BeforeClass/BeforeMethod抛出异常后，onTestSkipped tr.getThrowable为null
     * 由Test抛出的SkipException，tr.getThrowable不为空，能获取到跳过的原因
     *
     * @param tr
     */
    @Override
    public void onTestSkipped(ITestResult tr) {
        TestDescription testDesc = (TestDescription) tr.getTestContext().getAttribute(TEST_DESCRIPTION);
        log.warn("[自动化测试][{}]onTestSkipped, testcaseId: {}", testDesc.getDeviceId(), testDesc.getTestcaseId());

        Testcase testcase = new Testcase();
        testcase.setId(testDesc.getTestcaseId());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.SKIP_STATUS);
        testcase.setFailImgUrl(getScreenshotDownloadUrl(testDesc));
        if (tr.getThrowable() == null) { // @BeforeClass或@BeforeMethod抛出异常导致的case跳过
            testcase.setFailInfo("前置任务执行失败");
        } else {
            testcase.setFailInfo(tr.getThrowable().getMessage());
        }
        testcase.setVideoUrl(getVideoDownloadUrl(testDesc));
        MASTER_API.updateTestcase(testDesc.getDeviceTestTaskId(), testcase);
    }

    private String getScreenshotDownloadUrl(TestDescription testDesc) {
        try {
            return testDesc.getMobileDevice().screenshotAndUploadToMaster();
        } catch (Exception e) {
            log.error("[自动化测试][{}]testcaseId: {}，截图并上传到master失败", testDesc.getDeviceId(), testDesc.getTestcaseId(), e);
            return null;
        }
    }

    private String getVideoDownloadUrl(TestDescription testDesc) {
        if (!testDesc.getRecordVideo()) {
            return null;
        }

        try {
            log.info("[自动化测试][{}]testcaseId: {}, 停止录制视频...", testDesc.getDeviceId(), testDesc.getTestcaseId());
            long startTime = System.currentTimeMillis();
            String downloadUrl = testDesc.getMobileDevice().stopRecordingScreenAndUploadToMaster();
            log.info("[自动化测试][{}]testcaseId: {}, 停止录制视频并上传到master完成，耗时: {} ms", testDesc.getDeviceId(), testDesc.getTestcaseId(), System.currentTimeMillis() - startTime);
            return downloadUrl;
        } catch (Exception e) {
            log.error("[自动化测试][{}]testcaseId: {}，stopRecordingScreenAndUploadToMaster err", testDesc.getDeviceId(), testDesc.getTestcaseId(), e);
            return null;
        }
    }

    /**
     * 提供给actions.ftl调用，记录用例步骤的执行开始/结束时间
     */
    public static void recordTestCaseStepTime(Integer deviceTestTaskId, Integer actionId, boolean isStart, Integer stepNumber) {
        Integer currentTestcaseId = CURRENT_TEST_CASE_ID.get();
        // 只记录当前正在执行的测试用例里的步骤
        if (!actionId.equals(currentTestcaseId)) {
            return;
        }

        Step step = new Step();
        if (isStart) {
            step.setStartTime(new Date());
        } else {
            step.setEndTime(new Date());
        }
        step.setNumber(stepNumber);

        Testcase testcase = new Testcase();
        testcase.setId(currentTestcaseId);
        testcase.setSteps(Arrays.asList(step));
        MASTER_API.updateTestcase(deviceTestTaskId, testcase);
    }
}