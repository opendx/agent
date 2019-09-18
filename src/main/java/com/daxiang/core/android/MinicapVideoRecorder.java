package com.daxiang.core.android;

import com.android.ddmlib.*;
import com.daxiang.core.android.AndroidDevice;
import com.daxiang.core.android.AndroidUtil;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class MinicapVideoRecorder {

    private static final long SCREENSHOT_PERIOD_MS = 200;
    private static final long FPS = 1000 / SCREENSHOT_PERIOD_MS;

    private final AndroidDevice androidDevice;
    private final List<File> imgFileList = new ArrayList();
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public MinicapVideoRecorder(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
    }

    public void start() {
        IDevice iDevice = androidDevice.getIDevice();
        String resolution = androidDevice.getResolution();

        service.scheduleAtFixedRate(() -> {
            try {
                imgFileList.add(AndroidUtil.screenshotByMinicap(iDevice, resolution));
            } catch (Exception e) {
                log.error("[{}]screenshot by minicap err", iDevice.getSerialNumber(), e);
            }
        }, 0, SCREENSHOT_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    public File stop() {
        service.shutdown();
        try {
            // 由于shutdown之后，可能还有minicap截图在运行，等待所有截图完成后继续往下执行
            service.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("wait for minicap screenshot complete err", e);
        }

        if (imgFileList.isEmpty()) {
            throw new RuntimeException("no minicap screenshot file");
        }

        String videoPath = UUIDUtil.getUUID() + ".mp4";
        String deviceId = androidDevice.getId();

        // 截图重命名为{deviceId}_%d格式，方便ffmpeg合成视频
        for (int i = 0; i < imgFileList.size(); i++) {
            File newImgFile = new File(deviceId + "_" + i);
            imgFileList.get(i).renameTo(newImgFile);
            imgFileList.set(i, newImgFile);
        }

        // 合成视频
        // -threads 2 多线程加速合成
        // -r 帧数
        // -i 输入文件
        String cmd = "ffmpeg -threads 2 -f image2 -r " + FPS + " -i " + deviceId + "_%d" + " -vcodec libx264 " + videoPath;
        try {
            log.info("[{}]开始生成视频: {}", deviceId, cmd);
            long startTime = System.currentTimeMillis();
            Terminal.execute(cmd);
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                throw new RuntimeException("视频文件不存在，请检查ffmpeg是否已配置到环境变量");
            }
            log.info("[{}]视频已生成，耗时: {} ms", deviceId, System.currentTimeMillis() - startTime);
            return videoFile;
        } catch (IOException e) {
            throw new RuntimeException("execute cmd err: " + cmd, e);
        } finally {
            // 删除截图
            imgFileList.forEach(file -> file.delete());
        }
    }

}
