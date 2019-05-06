package com.fgnb.testng.listener;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.AndroidUtils;
import com.fgnb.api.MasterApi;
import com.fgnb.App;
import com.fgnb.model.devicetesttask.DeviceTestTask;
import com.fgnb.model.devicetesttask.Testcase;
import com.fgnb.utils.ShellExecutor;
import com.fgnb.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.Date;


/**
 * Created by jiangyitao.
 */
@Slf4j
public class TestCaseTestListener extends TestListenerAdapter {

    private static final MasterApi masterApi = App.getBean(MasterApi.class);

    private static final ThreadLocal<String> TL_DEVICE_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TL_DEVICE_TEST_TASK_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TL_TEST_CASE_ID = new ThreadLocal<>();

    private static final Long SCREEN_SHOT_INTERVAL_MS = 1000L;
    private static final ThreadLocal<Boolean> TL_RECORDING_VIDEO = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> TL_KEEP_SCREEN_SHOT = new ThreadLocal<>();
    private static final ThreadLocal<File> TL_TEST_CASE_IMG_AND_VIDEO_FOLDER = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext testContext) {
        String[] testDesc = testContext.getAllTestMethods()[0].getDescription().split("_");
        TL_DEVICE_ID.set(testDesc[0]);
        TL_DEVICE_TEST_TASK_ID.set(Integer.parseInt(testDesc[1]));
        TL_RECORDING_VIDEO.set(true);//这个版本先设置为需要录制视频，以后可能改成从前端传过来

        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(TL_DEVICE_TEST_TASK_ID.get());
        deviceTestTask.setStartTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.RUNNING_STATUS);
        masterApi.updateDeviceTestTask(deviceTestTask);
    }

    @Override
    public void onFinish(ITestContext testContext) {
        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(TL_DEVICE_TEST_TASK_ID.get());
        deviceTestTask.setEndTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.FINISHED_STATUS);
        masterApi.updateDeviceTestTask(deviceTestTask);

        if(TL_RECORDING_VIDEO.get()) {
            //删除录制视频生成的文件夹
            if(TL_TEST_CASE_IMG_AND_VIDEO_FOLDER.get().exists()) {
                try {
                    FileUtils.deleteDirectory(TL_TEST_CASE_IMG_AND_VIDEO_FOLDER.get());
                } catch (IOException e) {
                    log.error("删除{}出错", TL_TEST_CASE_IMG_AND_VIDEO_FOLDER.get().getAbsolutePath(),e);
                }
            }
        }
    }

    @Override
    public void onTestStart(ITestResult tr) {
        TL_TEST_CASE_ID.set(Integer.parseInt(tr.getMethod().getDescription().split("_")[2]));
        Testcase testcase = new Testcase();
        testcase.setId(TL_TEST_CASE_ID.get());
        testcase.setStartTime(new Date());
        masterApi.updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);

        if (TL_RECORDING_VIDEO.get()) { // 需要录制测试视频
            File deviceIdFolder = new File(TL_DEVICE_ID.get());
            if (!deviceIdFolder.exists()) {
                deviceIdFolder.mkdir();
            }
            TL_TEST_CASE_IMG_AND_VIDEO_FOLDER.set(deviceIdFolder);
            //清除上一个测试用例生成的文件
            try {
                FileUtils.cleanDirectory(deviceIdFolder);
            } catch (IOException e) {
                log.error("[{}]清除文件夹{}失败", TL_DEVICE_ID.get(), deviceIdFolder.getAbsolutePath(), e);
            }
            TL_KEEP_SCREEN_SHOT.set(true);
            new Thread(() -> {
                int i = 0;
                log.info("[{}]开始持续截图...",TL_DEVICE_ID.get());
                while (TL_KEEP_SCREEN_SHOT.get()) {
                    AndroidDevice androidDevice = AndroidDeviceHolder.get(TL_DEVICE_ID.get());
                    try {
                        AndroidUtils.screenshotByMinicap(androidDevice.getIDevice(), deviceIdFolder.getAbsolutePath() + File.separator + i + ".jpg", androidDevice.getResolution());
                    } catch (Exception e) {
                        log.error("[{}]截图出错", androidDevice.getId(), e);
                    }
                    try {
                        Thread.sleep(SCREEN_SHOT_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        log.error("[{}]sleep error", androidDevice.getId(), e);
                    }
                    i++;
                }
                log.info("[{}]停止持续截图",TL_DEVICE_ID.get());
            }).start();
        }
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        Testcase testcase = new Testcase();
        if(TL_RECORDING_VIDEO.get()) {
            testcase.setVideoUrl(generateVideoAndUploadToMaster());
        }
        testcase.setId(TL_TEST_CASE_ID.get());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.PASS_STATUS);
        masterApi.updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        Testcase testcase = new Testcase();
        testcase.setId(TL_TEST_CASE_ID.get());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.FAIL_STATUS);
        testcase.setFailImgUrl(screenShotAndUploadToMaster());
        testcase.setFailInfo(tr.getThrowable().getMessage());
        if(TL_RECORDING_VIDEO.get()) {
            testcase.setVideoUrl(generateVideoAndUploadToMaster());
        }
        masterApi.updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        Testcase testcase = new Testcase();
        testcase.setId(TL_TEST_CASE_ID.get());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.SKIP_STATUS);
        testcase.setFailImgUrl(screenShotAndUploadToMaster());
        testcase.setFailInfo(tr.getThrowable().getMessage());
        if(TL_RECORDING_VIDEO.get()) {
            testcase.setVideoUrl(generateVideoAndUploadToMaster());
        }
        masterApi.updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    /**
     * 生成视频并上传到服务器
     */
    private String generateVideoAndUploadToMaster() {
        // 停止持续截图
        TL_KEEP_SCREEN_SHOT.set(false);

        // 合成视频
        String imgAndVideoFolderAbsolutePath = TL_TEST_CASE_IMG_AND_VIDEO_FOLDER.get().getAbsolutePath();
        String videoPath = imgAndVideoFolderAbsolutePath + File.separator + UUIDUtil.getUUID() + ".mp4";
        // -r:帧数  -i:输入文件
        // 不用libx264编码前端<video>标签可能无法播放
        // todo 后续兼容linux mac平台，这里先写死ffmpeg.exe合成视频
        String cmd = "vendor/ffmpeg/bin/ffmpeg.exe -f image2 -r 1 -i " + imgAndVideoFolderAbsolutePath + File.separator + "%d.jpg -vcodec libx264 " + videoPath;
        try {
            ShellExecutor.exec(cmd);
        } catch (Exception e) {
            log.error("[{}]合成视频失败，cmd: {}",TL_DEVICE_ID.get(),cmd);
            return null;
        }

        try {
            return masterApi.uploadFile(new File(videoPath));
        } catch (Exception e) {
            log.error("[{}]上传视频失败，videoPath: {}",TL_DEVICE_ID.get(),videoPath);
            return null;
        }
    }

    /**
     * 截图并上传到服务器
     */
    private String screenShotAndUploadToMaster() {
        String screenshotFilePath = UUIDUtil.getUUID() + ".jpg";
        try {
            AndroidDevice androidDevice = AndroidDeviceHolder.get(TL_DEVICE_ID.get());
            AndroidUtils.screenshotByMinicap(androidDevice.getIDevice(), screenshotFilePath,androidDevice.getResolution());
        } catch (Exception e) {
            log.error("[{}]截图失败",TL_DEVICE_ID.get());
            return null;
        }
        File screenshotFile = new File(screenshotFilePath);
        try {
            return masterApi.uploadFile(screenshotFile);
        } catch (Exception e){
            log.error("[{}]上传截图失败",TL_DEVICE_ID.get());
            return null;
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }
}
