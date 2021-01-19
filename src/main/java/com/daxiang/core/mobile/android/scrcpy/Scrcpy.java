package com.daxiang.core.mobile.android.scrcpy;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.NullOutputReceiver;
import com.daxiang.App;
import com.daxiang.core.PortProvider;
import com.daxiang.core.mobile.android.AndroidDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class Scrcpy {

    private static final String LOCAL_SCRCPY_PATH = "vendor/scrcpy/scrcpy-server";
    private static final String REMOTE_SCRCPY_PATH = AndroidDevice.TMP_FOLDER + "/scrcpy-server.jar";

    private IDevice iDevice;
    private String mobileId;

    private int width;
    private int heigth;

    private int maxSize = 800;

    private int pid;

    private OutputStream controlOutputStream;

    private boolean isRunning = false;

    public Scrcpy(IDevice iDevice) {
        this.iDevice = iDevice;
        mobileId = iDevice.getSerialNumber();
    }

    public void setIDevice(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    public synchronized void start(Consumer<ByteBuffer> consumer) throws Exception {
        Assert.notNull(consumer, "consumer cannot be null");

        if (isRunning) {
            return;
        }

        // 由于scrcpy启动后会删除Mobile里的scrcpy，所以每次都需要重新push
        // Scrcpy.server - Server.java unlinkSelf()
        // 1.12.1 -> 1.17 已经移除了unlinkSelf()，先保留这个逻辑
        pushScrcpyToDevice();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        new Thread(() -> {
            try {
                // 2021.1.19: 1.12.1 -> 1.17
                String startCmd = String.format("CLASSPATH=%s app_process / com.genymobile.scrcpy.Server " +
                                "%s " +    // scrcpyVersion
                                "info " +  // logLevel
                                "%d " +    // maxSize
                                "%s " +    // bitRate
                                "60 " +    // maxFps
                                "-1 " +    // lockedVideoOrientation
                                "true " +  // tunnelForward
                                "- " +     // crop
                                "true " +  // sendFrameMeta
                                "true " +  // control
                                "0 " +     // displayId https://github.com/Genymobile/scrcpy/pull/1177/files
                                "true " +  // showTouches
                                "true " +  // stayAwake
                                /*
                                codecOptions: 适配broadway.js
                                "level": 0x100  format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3);
                                "profile": 0x01 format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
                                 */
                                "level=256,profile=1 " + // codecOptions
                                "-",       // encoderName https://github.com/Genymobile/scrcpy/pull/1827
                        REMOTE_SCRCPY_PATH,
                        App.getProperty("scrcpyVersion"),
                        maxSize,
                        App.getProperty("remoteScrcpyBitRate"));
                log.info("[{}]start scrcpy: {}", mobileId, startCmd);
                iDevice.executeShellCommand(startCmd, new MultiLineReceiver() {
                    @Override
                    public void processNewLines(String[] lines) {
                        for (String line : lines) {
                            log.info("[{}]scrcpy: {}", mobileId, line);
                            if (!StringUtils.isEmpty(line)) {
                                if (line.contains("pid:")) { // [server] INFO: pid:11151
                                    pid = Integer.parseInt(line.substring(19));
                                    log.info("[{}]scrcpy pid: {}", mobileId, pid);
                                } else if (line.contains("wait for connection")) {
                                    countDownLatch.countDown();
                                }
                            }
                        }
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }
                }, 0, TimeUnit.SECONDS);
                log.info("[{}]scrcpy已停止运行", mobileId);
                isRunning = false;
            } catch (Exception e) {
                throw new RuntimeException(String.format("[%s]启动scrcpy失败", mobileId), e);
            }
        }).start();

        int scrcpyStartTimeoutInSeconds = 30;
        boolean scrcpyStartSuccess = countDownLatch.await(scrcpyStartTimeoutInSeconds, TimeUnit.SECONDS);
        if (!scrcpyStartSuccess) {
            throw new RuntimeException(String.format("[%s]启动scrcpy失败，超时时间：%d秒", mobileId, scrcpyStartTimeoutInSeconds));
        }

        log.info("[{}]scrcpy启动完成", mobileId);
        isRunning = true;

        int localPort = PortProvider.getScrcpyAvailablePort();

        log.info("[{}]adb forward: {} -> remote scrcpy", mobileId, localPort);
        iDevice.createForward(localPort, "scrcpy", IDevice.DeviceUnixSocketNamespace.ABSTRACT);

        new Thread(() -> {
            Socket controlSocket = null;
            try (Socket screenSocket = new Socket("127.0.0.1", localPort);
                 InputStream screenStream = screenSocket.getInputStream()) {

                // Scrcpy.server - DesktopConnection.java
                // send one byte so the client may read() to detect a connection error
                // videoSocket.getOutputStream().write(0);
                if (screenStream.read() != 0) {
                    throw new RuntimeException(String.format("[%s]scrcpy connection error", mobileId));
                }

                log.info("[{}]connect scrcpy success", mobileId);

                controlSocket = new Socket("127.0.0.1", localPort);
                controlOutputStream = controlSocket.getOutputStream();

                // Scrcpy.server - DesktopConnection.java
                // deviceName 64
                // width 2
                // height 2
                for (int i = 0; i < 64; i++) {
                    screenStream.read();
                }

                width = screenStream.read() << 8 | screenStream.read();
                heigth = screenStream.read() << 8 | screenStream.read();
                log.info("[{}]scrcpy width: {} heigth: {}", mobileId, width, heigth);

                byte[] packet = new byte[1024 * 1024];
                int packetSize;

                while (isRunning) {
                    // Scrcpy.server - ScreenEncoder.java
                    // private final ByteBuffer headerBuffer = ByteBuffer.allocate(12);
                    // headerBuffer.putLong(presentationTimeUs); 8字节
                    // headerBuffer.putInt(packetSize); 4字节
                    for (int i = 0; i < 8; i++) {
                        screenStream.read();
                    }

                    packetSize = (screenStream.read() & 0xff) << 24
                            | (screenStream.read() & 0xff) << 16
                            | (screenStream.read() & 0xff) << 8
                            | (screenStream.read() & 0xff);
                    if (packetSize > packet.length) {
                        packet = new byte[packetSize];
                    }

                    for (int i = 0; i < packetSize; i++) {
                        packet[i] = (byte) screenStream.read();
                    }

                    consumer.accept(ByteBuffer.wrap(packet, 0, packetSize));
                }
            } catch (IndexOutOfBoundsException ign) {
            } catch (Exception e) {
                log.warn("[{}]处理scrcpy数据失败", mobileId, e);
            } finally {
                if (controlOutputStream != null) {
                    try {
                        controlOutputStream.close();
                    } catch (IOException e) {
                    }
                }
                if (controlSocket != null) {
                    try {
                        controlSocket.close();
                    } catch (IOException e) {
                    }
                }
            }
            log.info("[{}]已停止消费scrcpy图片数据", mobileId);

            // 移除adb forward
            try {
                log.info("[{}]移除adb forward: {} -> remote scrcpy", mobileId, localPort);
                iDevice.removeForward(localPort, "scrcpy", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            } catch (Exception e) {
                log.error("[{}]移除adb forward出错", mobileId, e);
            }
        }).start();
    }

    public synchronized void stop() {
        if (isRunning) {
            String cmd = "kill -9 " + pid;
            try {
                log.info("[{}]kill scrcpy: {}", mobileId, cmd);
                iDevice.executeShellCommand(cmd, new NullOutputReceiver());
            } catch (Exception e) {
                log.error("[{}]{}执行出错", mobileId, cmd, e);
            }
        }
    }

    private void pushScrcpyToDevice() throws Exception {
        log.info("[{}]push scrcpy to mobile, {} -> {}", mobileId, LOCAL_SCRCPY_PATH, REMOTE_SCRCPY_PATH);
        iDevice.pushFile(LOCAL_SCRCPY_PATH, REMOTE_SCRCPY_PATH);

        String chmodCmd = "chmod 777 " + REMOTE_SCRCPY_PATH;
        log.info("[{}]{} ", mobileId, chmodCmd);
        iDevice.executeShellCommand(chmodCmd, new NullOutputReceiver());
    }

    // android.view.MotionEvent
    private static final byte ACTION_DOWN = 0;
    private static final byte ACTION_UP = 1;
    private static final byte ACTION_MOVE = 2;

    public void touchDown(int x, int y, int screenWidth, int screenHeight) {
        commitTouchEvent(ACTION_DOWN, x, y, screenWidth, screenHeight);
    }

    public void touchUp(int x, int y, int screenWidth, int screenHeight) {
        commitTouchEvent(ACTION_UP, x, y, screenWidth, screenHeight);
    }

    public void moveTo(int x, int y, int screenWidth, int screenHeight) {
        commitTouchEvent(ACTION_MOVE, x, y, screenWidth, screenHeight);
    }

    // Scrcpy.server ControlMessage
    private static final byte TYPE_INJECT_TOUCH_EVENT = 2;
    private ByteBuffer touchEventBuffer = ByteBuffer.allocate(28);

    // Scrcpy.server ControlMessageReader.parseInjectTouchEvent
    private void commitTouchEvent(byte actionType, int x, int y, int screenWidth, int screenHeight) {
        // Scrcpy.server Device.computeVideoSize
        // 由于H264只接收8的倍数的宽高，所以scrcpy重新计算了video size
        // scrcpy输出的video size不能直接拿来用，否则会出现commitTouchEvent无效的问题
        if (screenHeight == maxSize) {
            screenWidth = heigth == maxSize ? width : heigth;
        } else if (screenWidth == maxSize) {
            screenHeight = width == maxSize ? heigth : width;
        }

        touchEventBuffer.rewind();

        touchEventBuffer.put(TYPE_INJECT_TOUCH_EVENT);
        touchEventBuffer.put(actionType);
        touchEventBuffer.putLong(-1L); // pointerId
        touchEventBuffer.putInt(x);
        touchEventBuffer.putInt(y);
        touchEventBuffer.putShort((short) screenWidth);
        touchEventBuffer.putShort((short) screenHeight);
        touchEventBuffer.putShort((short) 0xffff); // pressure
        touchEventBuffer.putInt(1); // buttons 鼠标左键: 1 << 0 | 右键: 1 << 1 | 中键: 1 << 2

        commit(touchEventBuffer.array());
    }

    private static final byte KEY_EVENT_ACTION_DOWN = 0;
    private static final byte KEY_EVENT_ACTION_UP = 1;

    // Scrcpy.server ControlMessageReader.parseInjectKeycode
    public void keyDown(int keycode, int metaState) {
        commitKeycode(keycode, metaState, KEY_EVENT_ACTION_DOWN);
    }

    public void keyUp(int keycode, int metaState) {
        commitKeycode(keycode, metaState, KEY_EVENT_ACTION_UP);
    }

    private static final byte TYPE_INJECT_KEYCODE = 0;
    private ByteBuffer keycodeBuffer = ByteBuffer.allocate(14);

    private void commitKeycode(int keycode, int metaState, byte keyDownOrUp) {
        keycodeBuffer.rewind();

        keycodeBuffer.put(TYPE_INJECT_KEYCODE);
        keycodeBuffer.put(keyDownOrUp);
        keycodeBuffer.putInt(keycode);
        keycodeBuffer.putInt(0); // repeat先简单处理 https://github.com/Genymobile/scrcpy/issues/1013
        keycodeBuffer.putInt(metaState);

        commit(keycodeBuffer.array());
    }

    private void commit(byte[] msg) {
        try {
            controlOutputStream.write(msg);
            controlOutputStream.flush();
        } catch (IOException e) {
            log.error("[{}]commit msg err", mobileId, e);
        }
    }
}
