package com.daxiang.core.android.scrcpy;

import com.android.ddmlib.IDevice;
import com.daxiang.App;
import com.daxiang.core.PortProvider;
import com.daxiang.core.android.AndroidUtil;
import com.daxiang.core.android.IDeviceExecuteShellCommandException;
import com.daxiang.utils.Terminal;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class ScrcpyVideoRecorder {

    private IDevice iDevice;
    private String deviceId;

    private String videoName;
    private boolean isRecording = false;
    private CountDownLatch countDownLatch;

    public ScrcpyVideoRecorder(IDevice iDevice) {
        this.iDevice = iDevice;
        this.deviceId = iDevice.getSerialNumber();

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
            String startCmd = String.format("scrcpy -s %s -Nr %s -b%sM -p %d",
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
     * 由于ExecuteWatchdog.destroyProcess()会导致最后一部分视频无法写入文件
     * 目前处理方法为kill手机内scrcpy server，由scrcpy写入最后一部分视频，得到完整视频
     */
    public synchronized File stop() {
        if (!isRecording) {
            throw new RuntimeException("video is not in recording");
        }

        log.info("[scrcpy][{}]stop record video: {}", deviceId, videoName);

        // shell     7460  7458  808868 34124 ffffffff b6e43a28 S app_process
        String scrcpyServerInfo;
        try {
            scrcpyServerInfo = AndroidUtil.executeShellCommand(iDevice, "ps |grep shell |grep app_process");
        } catch (IDeviceExecuteShellCommandException e) {
            throw new RuntimeException(e);
        }

        if (StringUtils.isEmpty(scrcpyServerInfo) || !scrcpyServerInfo.contains("app_process")) {
            throw new RuntimeException("cannot find scrcpy server ps info");
        }

        Matcher matcher = Pattern.compile("shell(\\s+)(\\d+)").matcher(scrcpyServerInfo);
        while (matcher.find()) {
            int pid = Integer.parseInt(matcher.group(2));
            try {
                AndroidUtil.executeShellCommand(iDevice, "kill -9 " + pid);
            } catch (IDeviceExecuteShellCommandException e) {
                throw new RuntimeException(e);
            }
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
