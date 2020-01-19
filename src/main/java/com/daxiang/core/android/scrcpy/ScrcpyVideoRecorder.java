package com.daxiang.core.android.scrcpy;

import com.daxiang.App;
import com.daxiang.core.PortProvider;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class ScrcpyVideoRecorder {

    private String deviceId;

    private String startCmd;
    private String videoName;
    private boolean isRecording = false;
    private CountDownLatch countDownLatch;

    public ScrcpyVideoRecorder(String deviceId) {
        this.deviceId = deviceId;

        try {
            String version = Terminal.execute("scrcpy -v");
            if (StringUtils.isEmpty(version) || !version.startsWith("scrcpy")) {
                throw new RuntimeException("未找到scrcpy，需要将scrcpy配置到环境变量，更多信息: https://github.com/Genymobile/scrcpy");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void start() {
        if (isRecording) {
            return;
        }

        countDownLatch = new CountDownLatch(1);

        try {
            videoName = UUIDUtil.getUUID() + ".mp4";
            startCmd = String.format("scrcpy -s %s -Nr %s -b%sM -p %d",
                    deviceId,
                    videoName,
                    App.getProperty("androidRecordVideoBitRate"),
                    PortProvider.getScrcpyRecordVideoPort());
            log.info("[scrcpy][{}]start record video, cmd: {}", deviceId, startCmd);
            Terminal.executeAsync(startCmd, new PumpStreamHandler(new LogOutputStream() {
                @Override
                protected void processLine(String line, int i) {
                    log.info("[scrcpy][{}]{}", deviceId, line);
                    if (line.contains("Recording complete")) {
                        countDownLatch.countDown();
                    }
                }
            }));
            isRecording = true;
        } catch (IOException e) {
            isRecording = false;
            throw new RuntimeException(e);
        }
    }

    /**
     * 由于ExecuteWatchdog.destroyProcess()会导致最后一部分视频无法写入
     * scrcpy收到kill信号后，会写入最后一部分视频
     */
    public synchronized File stop() throws IOException {
        if (!isRecording) {
            throw new RuntimeException("video is not in recording");
        }

        log.info("[scrcpy][{}]stop record video: {}", deviceId, videoName);

        if (Terminal.IS_WINDOWS) {
            // todo windows
        } else {
            String killScrcpyCmd = String.format("ps -ef|grep '%s'|grep -v grep|awk '{print \"kill \"$2}'|sh", startCmd);
            Terminal.execute(killScrcpyCmd);
        }

        try {
            // 等待视频写入完成，最多等3min
            countDownLatch.await(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("[scrcpy][{}]video: {} recording complete", deviceId, videoName);
        isRecording = false;

        return new File(videoName);
    }
}
