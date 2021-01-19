package com.daxiang.core.mobile.android.scrcpy;

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

    private String mobileId;

    private String startCmd;
    private String videoName;
    private boolean isRecording = false;
    private CountDownLatch countDownLatch;

    public ScrcpyVideoRecorder(String mobileId) {
        this.mobileId = mobileId;

        if (Terminal.IS_WINDOWS) {
            throw new IllegalStateException("暂不支持windows录屏");
        }

        String version;
        try {
            version = Terminal.execute("scrcpy -v");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (StringUtils.isEmpty(version) || !version.startsWith("scrcpy")) {
            throw new IllegalStateException("未找到scrcpy，需要将scrcpy配置到环境变量，更多信息: https://github.com/Genymobile/scrcpy");
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
                    mobileId,
                    videoName,
                    App.getProperty("androidRecordVideoBitRate"),
                    PortProvider.getScrcpyRecordVideoPort());
            log.info("[{}]start record video, cmd: {}", mobileId, startCmd);
            Terminal.executeAsync(startCmd, new PumpStreamHandler(new LogOutputStream() {
                @Override
                protected void processLine(String line, int i) {
                    log.info("[{}]scrcpy: {}", mobileId, line);
                    if (line.contains("Recording complete")) {
                        isRecording = false;
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
     * 1. kill scrcpy server来停止录制视频是最优方案。但大多数安卓Mobile只能通过ps（非ps -ef）获取到scrcpy server进程,
     * 此时的进程名为app_process, appium在Mobile里运行的进程也是app_process，所以可能会误杀appium在Mobile里运行的进程，不采用该方法
     * 2. ExecuteWatchdog.destroyProcess()会导致最后一部分视频无法写入，
     * 因为运行在pc的scrcpy进程被直接干掉，无法写入最终的视频，导致获取到破损的视频
     * 3. 在非windows操作系统下，scrcpy收到kill信号后，会写入最后一部分视频，目前采用该方法
     * 4. 无法在windows上使用，windows taskkill和ExecuteWatchdog.destroyProcess()一样
     */
    public synchronized File stop() throws IOException {
        if (!isRecording) {
            throw new IllegalStateException("video is not in recording");
        }

        log.info("[{}]stop record video: {}", mobileId, videoName);

        String killScrcpyCmd = String.format("ps -ef|grep '%s'|grep -v grep|awk '{print \"kill \"$2}'|sh", startCmd);
        Terminal.execute(killScrcpyCmd);

        int recordingCompleteTimeoutInMinutes = 10;
        boolean isRecordingComplete;
        try {
            isRecordingComplete = countDownLatch.await(recordingCompleteTimeoutInMinutes, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!isRecordingComplete) {
            throw new RuntimeException(String.format("[%s]录制未完成，超时时间：%d分钟", mobileId, recordingCompleteTimeoutInMinutes));
        }

        log.info("[{}]video: {} recording complete", mobileId, videoName);

        return new File(videoName);
    }
}
