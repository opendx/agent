package com.daxiang.core.testng.listener;

import com.daxiang.App;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.api.MasterApi;
import com.daxiang.model.Device;
import com.daxiang.model.action.Step;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import com.daxiang.model.devicetesttask.Testcase;
import com.daxiang.service.AndroidService;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class TestCaseTestListener extends TestListenerAdapter {

    private static final ThreadLocal<AndroidDevice> TL_ANDROID_DEVICE = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TL_DEVICE_TEST_TASK_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TL_TEST_CASE_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> TL_NEED_RECORD_VIDEO = new ThreadLocal<>();
    private static final ThreadLocal<FutureTask<String>> TL_RECORD_VIDEO_FUTURE_TASK = new ThreadLocal<>();
    private static final ThreadLocal<Thread> TL_RECORD_VIDEO_THREAD = new ThreadLocal<>();

    /**
     * 每个设备开始测试调用的方法，这里可能有多个线程同时调用
     *
     * @param testContext
     */
    @Override
    public void onStart(ITestContext testContext) {
        // deviceId_deviceTestTaskId_testcaseId
        String[] testDesc = testContext.getAllTestMethods()[0].getDescription().split("_");
        String deviceId = testDesc[0];
        Integer deviceTestTaskId = Integer.parseInt(testDesc[1]);
        Boolean needRecordVideo = true; // 这个版本先设置为需要录制视频，以后可能改成从前端传过来

        AndroidDevice androidDevice = (AndroidDevice) MobileDeviceHolder.get(deviceId);
        log.info("[{}][自动化测试]onStart, deviceTestTaskId：{}", deviceId, deviceTestTaskId);

        TL_ANDROID_DEVICE.set(androidDevice);
        TL_DEVICE_TEST_TASK_ID.set(deviceTestTaskId);
        TL_NEED_RECORD_VIDEO.set(needRecordVideo);

        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(deviceTestTaskId);
        deviceTestTask.setStartTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.RUNNING_STATUS);
        MasterApi.getInstance().updateDeviceTestTask(deviceTestTask);

        if (needRecordVideo) {
            try {
                // 启动minicap，视频录制从minicap获取屏幕数据
                androidDevice.getMinicap().start(androidDevice.getResolution(), 0);
            } catch (Exception e) {
                log.error("[{}]启动minicap失败", deviceId, e);
            }
        }
    }

    /**
     * 每个设备结束测试调用的方法，这里可能有多个线程同时调用
     *
     * @param testContext
     */
    @Override
    public void onFinish(ITestContext testContext) {
        AndroidDevice androidDevice = TL_ANDROID_DEVICE.get();
        String deviceId = androidDevice.getId();
        Integer deviceTestTaskId = TL_DEVICE_TEST_TASK_ID.get();
        Boolean needRecordVideo = TL_NEED_RECORD_VIDEO.get();

        log.info("[{}][自动化测试]onFinish, deviceTestTaskId: {}", deviceId, deviceTestTaskId);
        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(deviceTestTaskId);
        deviceTestTask.setEndTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.FINISHED_STATUS);
        MasterApi.getInstance().updateDeviceTestTask(deviceTestTask);

        if (needRecordVideo && androidDevice.getMinicap() != null) {
            // 测试结束，停止minicap
            androidDevice.getMinicap().stop();
        }
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
        AndroidDevice androidDevice = TL_ANDROID_DEVICE.get();
        String deviceId = androidDevice.getId();
        // deviceId_deviceTestTaskId_testcaseId
        Integer testcaseId = Integer.parseInt(tr.getMethod().getDescription().split("_")[2]);
        TL_TEST_CASE_ID.set(testcaseId);

        log.info("[{}][自动化测试]onTestStart, testcaseId: {}", deviceId, testcaseId);

        Testcase testcase = new Testcase();
        testcase.setId(testcaseId);
        testcase.setStartTime(new Date());
        MasterApi.getInstance().updateTestcase(deviceTestTaskId, testcase);

        if (needRecordVideo) {
            String videoPath = UUIDUtil.getUUID() + ".mp4";
            log.info("[{}][自动化测试]testcaseId: {}，开始录制视频: {}", deviceId, testcaseId, videoPath);

            Device device = androidDevice.getDevice();
            final FFmpegFrameRecorder videoRecorder = new FFmpegFrameRecorder(videoPath, device.getScreenWidth(), device.getScreenHeight());
            videoRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 编码
            videoRecorder.setFrameRate(30); // 帧率
            videoRecorder.setFormat("mp4");
            try {
                videoRecorder.start();
            } catch (FrameRecorder.Exception e) {
                log.error("[{}][自动化测试]testcaseId: {}，录制视频出错", deviceId, testcaseId, e);
            }

            File video = new File(videoPath);
            Callable<String> recordVideo = () -> {
                BlockingQueue<byte[]> imgQueue = androidDevice.getMinicap().getImgQueue();
                imgQueue.clear(); // 防止录制到上一条用例的视频数据
                try {
                    while (true) {
                        byte[] imgData;
                        try {
                            imgData = imgQueue.take();
                        } catch (InterruptedException e) {
                            log.info("[{}][自动化测试]testcaseId: {}，停止获取minicap数据", deviceId, testcaseId);
                            break;
                        }
                        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imgData)) {
                            videoRecorder.record(Java2DFrameUtils.toFrame(ImageIO.read(byteArrayInputStream)));
                        }
                    }
                    videoRecorder.stop();
                    log.info("[{}][自动化测试]testcaseId: {}，停止录制视频：{}", deviceId, testcaseId, videoPath);

                    return MasterApi.getInstance().uploadFile(video);
                } finally {
                    FileUtils.deleteQuietly(video);
                }
            };

            FutureTask<String> recordVideoFutureTask = new FutureTask<>(recordVideo);
            TL_RECORD_VIDEO_FUTURE_TASK.set(recordVideoFutureTask);

            Thread recordVideoThread = new Thread(recordVideoFutureTask);
            TL_RECORD_VIDEO_THREAD.set(recordVideoThread);

            recordVideoThread.start();
        }
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        AndroidDevice androidDevice = TL_ANDROID_DEVICE.get();
        Integer testcaseId = TL_TEST_CASE_ID.get();
        log.info("[{}][自动化测试]onTestSuccess: {}", androidDevice.getId(), testcaseId);

        Testcase testcase = new Testcase();
        testcase.setId(testcaseId);
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.PASS_STATUS);
        testcase.setVideoUrl(getVideoDownloadUrl());
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        AndroidDevice androidDevice = TL_ANDROID_DEVICE.get();
        Integer testcaseId = TL_TEST_CASE_ID.get();
        log.error("[{}][自动化测试]onTestFailure: {}", androidDevice.getId(), testcaseId, tr.getThrowable());

        Testcase testcase = new Testcase();
        testcase.setId(testcaseId);
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.FAIL_STATUS);
        testcase.setFailImgUrl(getScreenshotDownloadUrl());
        testcase.setFailInfo(tr.getThrowable().getMessage());
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
        AndroidDevice androidDevice = TL_ANDROID_DEVICE.get();
        Integer testcaseId = TL_TEST_CASE_ID.get();
        log.warn("[{}][自动化测试]onTestSkipped: {}", androidDevice.getId(), testcaseId, tr.getThrowable());

        Testcase testcase = new Testcase();
        testcase.setId(testcaseId);
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.SKIP_STATUS);
        testcase.setFailImgUrl(getScreenshotDownloadUrl());
        if (tr.getThrowable() == null) { // @BeforeClass或@BeforeMethod抛出异常导致的case跳过
            testcase.setFailInfo("@BeforeClass或@BeforeMethod执行失败");
        } else {
            testcase.setFailInfo(tr.getThrowable().getMessage());
        }
        testcase.setVideoUrl(getVideoDownloadUrl());
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    private String getScreenshotDownloadUrl() {
        try {
            return App.getBean(AndroidService.class).screenshotByMinicapAndUploadToMaster(TL_ANDROID_DEVICE.get());
        } catch (Exception e) {
            log.error("[{}][自动化测试]用例：{}，截图并上传到master失败", TL_ANDROID_DEVICE.get().getId(), TL_TEST_CASE_ID.get(), e);
            return null;
        }
    }

    private String getVideoDownloadUrl() {
        if (!TL_NEED_RECORD_VIDEO.get()) {
            return null;
        }
        try {
            // 停止录制视频,imgQueue.take()捕获到InterruptedException跳出循环
            TL_RECORD_VIDEO_THREAD.get().interrupt();
            return TL_RECORD_VIDEO_FUTURE_TASK.get().get();
        } catch (Exception e) {
            log.error("[{}][自动化测试]用例：{}，获取视频下载地址失败", TL_ANDROID_DEVICE.get().getId(), TL_TEST_CASE_ID.get(), e);
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