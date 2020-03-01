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
    private static final ThreadLocal<String> CONFIG_FAIL_ERR_INFO = new ThreadLocal<>();

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

        CURRENT_TEST_CASE_ID.remove();
        CONFIG_FAIL_ERR_INFO.remove();

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

        // 当前置任务执行失败，或依赖的用例执行失败，tr.getThrowable() != null，此时不需要开启视频录制，因为testng会马上调用onTestSkip
        if (tr.getThrowable() == null && testDesc.getRecordVideo()) {
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
        testcase.setVideoPath(uploadVideo(testDesc));
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
        testcase.setFailImgPath(uploadScreenshot(testDesc));
        testcase.setFailInfo(ExceptionUtils.getStackTrace(tr.getThrowable()));
        testcase.setVideoPath(uploadVideo(testDesc));
        MASTER_API.updateTestcase(testDesc.getDeviceTestTaskId(), testcase);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        TestDescription testDesc = (TestDescription) tr.getTestContext().getAttribute(TEST_DESCRIPTION);
        log.warn("[自动化测试][{}]onTestSkipped, testcaseId: {}", testDesc.getDeviceId(), testDesc.getTestcaseId());

        Testcase testcase = new Testcase();
        testcase.setId(testDesc.getTestcaseId());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.SKIP_STATUS);

        if (CONFIG_FAIL_ERR_INFO.get() != null) { // 前置任务执行失败
            testcase.setFailInfo(CONFIG_FAIL_ERR_INFO.get());
        } else if (tr.getThrowable() != null) { // dependsOnMethods执行失败。实际前置任务执行失败，也是!=null，但为了获取更加详细的信息，已经在上面的if作了处理
            testcase.setFailInfo(tr.getThrowable().getMessage());
        } else { // 正常情况下的跳过，throw SkipException导致
            testcase.setFailInfo(tr.getThrowable().getMessage());
            testcase.setFailImgPath(uploadScreenshot(testDesc));
            testcase.setVideoPath(uploadVideo(testDesc));
        }

        MASTER_API.updateTestcase(testDesc.getDeviceTestTaskId(), testcase);
    }

    /**
     * 前置任务执行出错时，将进入该方法
     * 进入该方法后，后续的testcase都将跳过。将直接调用onTestStart -> onTestSkipped，不会调用@Test
     *
     * @param tr
     */
    @Override
    public void onConfigurationFailure(ITestResult tr) {
        TestDescription testDesc = (TestDescription) tr.getTestContext().getAttribute(TEST_DESCRIPTION);
        log.error("[自动化测试][{}]{}执行失败", testDesc.getDeviceId(), tr.getName(), tr.getThrowable());

        CONFIG_FAIL_ERR_INFO.set(tr.getName() + "执行失败!\n\n" + ExceptionUtils.getStackTrace(tr.getThrowable()));
    }

    private String uploadScreenshot(TestDescription testDesc) {
        try {
            return testDesc.getMobileDevice().screenshotAndUploadToMaster().getFilePath();
        } catch (Exception e) {
            log.error("[自动化测试][{}]testcaseId: {}，截图并上传到master失败", testDesc.getDeviceId(), testDesc.getTestcaseId(), e);
            return null;
        }
    }

    private String uploadVideo(TestDescription testDesc) {
        if (!testDesc.getRecordVideo()) {
            return null;
        }

        try {
            log.info("[自动化测试][{}]testcaseId: {}, 停止录制视频...", testDesc.getDeviceId(), testDesc.getTestcaseId());
            long startTime = System.currentTimeMillis();
            String uploadFileName = testDesc.getMobileDevice().stopRecordingScreenAndUploadToMaster().getFilePath();
            log.info("[自动化测试][{}]testcaseId: {}, 停止录制视频并上传到master完成，耗时: {} ms", testDesc.getDeviceId(), testDesc.getTestcaseId(), System.currentTimeMillis() - startTime);
            return uploadFileName;
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