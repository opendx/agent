package com.fgnb.testng.listener;

import com.fgnb.App;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.api.MasterApi;
import com.fgnb.model.devicetesttask.DeviceTestTask;
import com.fgnb.model.devicetesttask.Testcase;
import com.fgnb.service.AndroidService;
import com.fgnb.utils.UUIDUtil;
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
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
    private static final ThreadLocal<Boolean> TL_NEED_RECORDING_VIDEO = new ThreadLocal<>();
    private static final ThreadLocal<FutureTask<String>> TL_RECORDING_VIDEO_FUTURE_TASK = new ThreadLocal<>();
    private static final ThreadLocal<Thread> TL_RECORDING_VIDEO_THREAD = new ThreadLocal<>();

    /**
     * 每个设备开始测试调用的方法，这里可能有多个线程同时调用
     *
     * @param testContext
     */
    @Override
    public void onStart(ITestContext testContext) {
        // deviceId_deviceTestTaskId_testcaseId
        String[] testDesc = testContext.getAllTestMethods()[0].getDescription().split("_");
        AndroidDevice androidDevice = AndroidDeviceHolder.get(testDesc[0]);
        log.info("[{}][自动化测试]开始执行任务：{}", androidDevice.getId(), testDesc[1]);

        TL_ANDROID_DEVICE.set(androidDevice);
        TL_DEVICE_TEST_TASK_ID.set(Integer.parseInt(testDesc[1]));

        TL_NEED_RECORDING_VIDEO.set(true);// 这个版本先设置为需要录制视频，以后可能改成从前端传过来

        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(TL_DEVICE_TEST_TASK_ID.get());
        deviceTestTask.setStartTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.RUNNING_STATUS);
        MasterApi.getInstance().updateDeviceTestTask(deviceTestTask);

        if (TL_NEED_RECORDING_VIDEO.get()) {
            try {
                // 启动minicap，视频录制从minicap获取屏幕数据
                androidDevice.getMinicap().start(androidDevice.getResolution(), 0);
            } catch (Exception e) {
                log.error("[{}]启动minicap失败", androidDevice.getId(), e);
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
        log.info("[{}][自动化测试]执行任务完成：{}", TL_ANDROID_DEVICE.get().getId(), TL_DEVICE_TEST_TASK_ID.get());
        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(TL_DEVICE_TEST_TASK_ID.get());
        deviceTestTask.setEndTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.FINISHED_STATUS);
        MasterApi.getInstance().updateDeviceTestTask(deviceTestTask);

        if (TL_NEED_RECORDING_VIDEO.get() && TL_ANDROID_DEVICE.get().getMinicap() != null) {
            // 测试结束，停止minicap
            TL_ANDROID_DEVICE.get().getMinicap().stop();
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
        // deviceId_deviceTestTaskId_testcaseId
        TL_TEST_CASE_ID.set(Integer.parseInt(tr.getMethod().getDescription().split("_")[2]));
        log.info("[{}][自动化测试]开始执行用例：{}", TL_ANDROID_DEVICE.get().getId(), TL_TEST_CASE_ID.get());

        Testcase testcase = new Testcase();
        testcase.setId(TL_TEST_CASE_ID.get());
        testcase.setStartTime(new Date());
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);

        if (TL_NEED_RECORDING_VIDEO.get()) {
            String videoPath = UUIDUtil.getUUID() + ".mp4";
            log.info("[{}][自动化测试]用例：{}，开始录制视频：{}", TL_ANDROID_DEVICE.get().getId(), TL_TEST_CASE_ID.get(), videoPath);

            final FFmpegFrameRecorder videoRecorder = new FFmpegFrameRecorder(videoPath, TL_ANDROID_DEVICE.get().getDevice().getScreenWidth(), TL_ANDROID_DEVICE.get().getDevice().getScreenHeight());
            videoRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); //编码
            videoRecorder.setFrameRate(30); //帧率
            videoRecorder.setFormat("mp4");
            try {
                videoRecorder.start();
            } catch (FrameRecorder.Exception e) {
                log.error("[{}][自动化测试]用例：{}，开始录制视频出错", TL_ANDROID_DEVICE.get().getId(), TL_TEST_CASE_ID.get(), e);
            }

            File video = new File(videoPath);
            BlockingQueue<byte[]> imgQueue = TL_ANDROID_DEVICE.get().getMinicap().getImgQueue();
            String deviceId = TL_ANDROID_DEVICE.get().getId();
            String testcaseId = String.valueOf(TL_TEST_CASE_ID.get());
            Callable<String> recordingVideo = () -> {
                try {
                    while (true) {
                        byte[] imgData;
                        try {
                            imgData = imgQueue.take();
                        } catch (InterruptedException e) {
                            log.info("[{}][自动化测试]用例：{}，停止获取minicap数据", deviceId, testcaseId);
                            break;
                        }
                        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imgData)) {
                            BufferedImage read = ImageIO.read(byteArrayInputStream);
                            videoRecorder.record(Java2DFrameUtils.toFrame(read));
                        }
                    }
                    videoRecorder.stop();
                    log.info("[{}][自动化测试]用例：{}，停止录制视频：{}", deviceId, testcaseId, videoPath);

                    return MasterApi.getInstance().uploadFile(video);
                } finally {
                    FileUtils.deleteQuietly(video);
                }
            };

            FutureTask<String> futureTask = new FutureTask<>(recordingVideo);
            TL_RECORDING_VIDEO_FUTURE_TASK.set(futureTask);

            Thread recordingVideoThread = new Thread(futureTask);
            TL_RECORDING_VIDEO_THREAD.set(recordingVideoThread);

            recordingVideoThread.start();
        }
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        Testcase testcase = new Testcase();
        testcase.setId(TL_TEST_CASE_ID.get());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.PASS_STATUS);
        testcase.setVideoUrl(getVideoDownloadUrl());
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        Testcase testcase = new Testcase();
        testcase.setId(TL_TEST_CASE_ID.get());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.FAIL_STATUS);
        testcase.setFailImgUrl(getScreenshotDownloadUrl());
        testcase.setFailInfo(tr.getThrowable().getMessage());
        testcase.setVideoUrl(getVideoDownloadUrl());
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        Testcase testcase = new Testcase();
        testcase.setId(TL_TEST_CASE_ID.get());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.SKIP_STATUS);
        testcase.setFailImgUrl(getScreenshotDownloadUrl());
        testcase.setFailInfo(tr.getThrowable().getMessage());
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
        if (!TL_NEED_RECORDING_VIDEO.get()) {
            return null;
        }
        //停止录制视频,imgQueue.take()捕获到InterruptedException跳出循环
        TL_RECORDING_VIDEO_THREAD.get().interrupt();
        try {
            return TL_RECORDING_VIDEO_FUTURE_TASK.get().get();
        } catch (Exception e) {
            log.error("[{}][自动化测试]用例：{}，获取视频下载地址失败", TL_ANDROID_DEVICE.get().getId(), TL_TEST_CASE_ID.get(), e);
            return null;
        }
    }
}