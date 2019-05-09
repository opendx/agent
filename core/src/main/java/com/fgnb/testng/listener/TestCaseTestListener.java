package com.fgnb.testng.listener;

import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.AndroidUtils;
import com.fgnb.android.stf.minicap.Minicap;
import com.fgnb.api.MasterApi;
import com.fgnb.model.devicetesttask.DeviceTestTask;
import com.fgnb.model.devicetesttask.Testcase;
import com.fgnb.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Date;
import java.util.concurrent.BlockingQueue;


/**
 * Created by jiangyitao.
 */
@Slf4j
public class TestCaseTestListener extends TestListenerAdapter {

    private static final ThreadLocal<String> TL_ANDROID_DEVICE = new ThreadLocal<>();

    private static final ThreadLocal<Integer> TL_DEVICE_TEST_TASK_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> TL_TEST_CASE_ID = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> TL_RECORDING_VIDEO = new ThreadLocal<>();
    private static final ThreadLocal<Minicap> TL_MINICAP = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext testContext) {
        String[] testDesc = testContext.getAllTestMethods()[0].getDescription().split("_");
        AndroidDevice androidDevice = AndroidDeviceHolder.get(testDesc[0]);


        TL_DEVICE_TEST_TASK_ID.set(Integer.parseInt(testDesc[1]));
        TL_RECORDING_VIDEO.set(true);//这个版本先设置为需要录制视频，以后可能改成从前端传过来

        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(TL_DEVICE_TEST_TASK_ID.get());
        deviceTestTask.setStartTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.RUNNING_STATUS);
        MasterApi.getInstance().updateDeviceTestTask(deviceTestTask);

        if(TL_RECORDING_VIDEO.get()) {

            Minicap minicap = androidDevice.getMinicap();
            try {
                minicap.start(androidDevice.getResolution(),0);
                TL_MINICAP.set(minicap);
            } catch (Exception e) {
                log.error("[{}]启动minicap失败",TL_DEVICE_ID.get(),e);
            }
        }
    }

    @Override
    public void onFinish(ITestContext testContext) {
        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setId(TL_DEVICE_TEST_TASK_ID.get());
        deviceTestTask.setEndTime(new Date());
        deviceTestTask.setStatus(DeviceTestTask.FINISHED_STATUS);
        MasterApi.getInstance().updateDeviceTestTask(deviceTestTask);

        if(TL_RECORDING_VIDEO.get() && TL_MINICAP.get() != null) {
            TL_MINICAP.get().stop();
        }
    }

    @Override
    public void onTestStart(ITestResult tr) {
        final FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                "d:/1.mp4",720,1280);
        //编码
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        //帧率
        recorder.setFrameRate(30);
        recorder.setFormat("mp4");

        OpenCVFrameConverter.ToIplImage conveter = new OpenCVFrameConverter.ToIplImage();

        recorder.start();
        System.out.println("start");



        BlockingQueue<byte[]> imgQueue = androidDevice.getMinicap().getImgQueue();

        new Thread(() -> {
            long start = System.currentTimeMillis();
            while(System.currentTimeMillis() - start < 10000) {
                byte[] take = null;
                try {
                    take = imgQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(take)) {
                    BufferedImage read = ImageIO.read(byteArrayInputStream);
                    recorder.record(Java2DFrameUtils.toFrame(read));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                recorder.stop();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }

            System.out.println("stop");
        }).start();


        TL_TEST_CASE_ID.set(Integer.parseInt(tr.getMethod().getDescription().split("_")[2]));
        Testcase testcase = new Testcase();
        testcase.setId(TL_TEST_CASE_ID.get());
        testcase.setStartTime(new Date());
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        Testcase testcase = new Testcase();
        if(TL_RECORDING_VIDEO.get()) {
        }
        testcase.setId(TL_TEST_CASE_ID.get());
        testcase.setEndTime(new Date());
        testcase.setStatus(Testcase.PASS_STATUS);
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
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
        }
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
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
        }
        MasterApi.getInstance().updateTestcase(TL_DEVICE_TEST_TASK_ID.get(), testcase);
    }


    /**
     * 截图并上传到服务器
     */
    private String screenShotAndUploadToMaster() {
        String screenshotFilePath = UUIDUtil.getUUID() + ".jpg";
        File screenshotFile = new File(screenshotFilePath);
        try {
            AndroidDevice androidDevice = AndroidDeviceHolder.get(TL_DEVICE_ID.get());
            AndroidUtils.screenshotByMinicap(androidDevice.getIDevice(), screenshotFilePath,androidDevice.getResolution());
        } catch (Exception e) {
            log.error("[{}]截图失败",TL_DEVICE_ID.get());
            FileUtils.deleteQuietly(screenshotFile);
            return null;
        }
        try {
            return MasterApi.getInstance().uploadFile(screenshotFile);
        } catch (Exception e){
            log.error("[{}]上传截图失败",TL_DEVICE_ID.get());
            return null;
        } finally {
            FileUtils.deleteQuietly(screenshotFile);
        }
    }
}
