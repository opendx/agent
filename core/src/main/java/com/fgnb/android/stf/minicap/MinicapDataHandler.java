package com.fgnb.android.stf.minicap;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.utils.ByteUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by jiangyitao.
 * minicap数据处理器
 */
@Slf4j
public class MinicapDataHandler {

    /** 连接/data/local/tmp/minicap 获取minicap输出数据的socket连接 */
    private Socket socket;
    /** 与手机minicap通信的的端口 */
    private int minicapPort;

    /** 设备id */
    private String deviceId;
    private AndroidDevice androidDevice;
    private IDevice iDevice;

    /** 原生的minicap图片数据存放队列 */
    private Queue<byte[]> nativeMinicapImageDataQueue = new ConcurrentLinkedQueue();

    /** minicaps原生数据输入流 */
    private DataInputStream dataInputStream;

    /** 经过处理的minicap图片数据存放队列 */
    private Queue<byte[]> processedMinicapImageDataQueue = new ConcurrentLinkedQueue();

    /** 将原生的minicap数据放入队列线程 */
    private Thread putNativeMinicapImageDataToQueueThread;
    /** 解析原生的minicap数据并放入队列线程 */
    private Thread parseNativeMinicapImageDataAndAddToQueueThread;

    /** minicap解析相关 start */
    MinicapBanner banner = new MinicapBanner();
    private int readBannerBytes = 0;
    private int bannerLength = 2;
    private int readFrameBytes = 0;
    private int frameBodyLength = 0;
    private byte[] frameBody = new byte[0];
    /** minicap解析相关 end */

    public MinicapDataHandler(String deviceId, int minicapPort){
        this.minicapPort = minicapPort;
        this.deviceId = deviceId;
        androidDevice = AndroidDeviceHolder.getAndroidDevice(deviceId);
        iDevice = androidDevice.getIDevice();
    }

    public Queue<byte[]> getProcessedMinicapImageDataQueue() {
        return processedMinicapImageDataQueue;
    }

    /**
     * 释放资源
     */
    public void releaseResources(){
        log.info("[{}]开始回收minicap资源",deviceId);
        //1.打断收集minicap数据线程
        if(putNativeMinicapImageDataToQueueThread != null
                && !putNativeMinicapImageDataToQueueThread.isInterrupted()
                && parseNativeMinicapImageDataAndAddToQueueThread.isAlive()){
            log.info("[{}]打断收集minicap数据线程",deviceId);
            putNativeMinicapImageDataToQueueThread.interrupt();
        }
        //2.打断解析minicap图片线程
        if(parseNativeMinicapImageDataAndAddToQueueThread != null
                && !parseNativeMinicapImageDataAndAddToQueueThread.isInterrupted()
                && parseNativeMinicapImageDataAndAddToQueueThread.isAlive()){
            log.info("[{}]打断解析minicap图片线程",deviceId);
            parseNativeMinicapImageDataAndAddToQueueThread.interrupt();
        }
        //3.关闭minicap输入流
        if(dataInputStream !=null){
            try {
                log.info("[{}]关闭minicap输入流",deviceId);
                dataInputStream.close();
            } catch (IOException e) {
                log.info("[{}]关闭minicap输入流出错",deviceId,e);
            }
        }
        //4.关闭socket
        if(socket!=null && socket.isConnected()){
            try {
                log.info("[{}]关闭minicap socket,port=>{}",deviceId,minicapPort);
                socket.close();
            } catch (IOException e) {
                log.info("[{}]关闭minicap socket出错",deviceId);
            }
        }
        //5.清空队列
        log.info("[{}]清空minicap processedMinicapImageDataQueue",deviceId);
        if(processedMinicapImageDataQueue !=null) {
            processedMinicapImageDataQueue.clear();
        }
        log.info("[{}]清空minicap nativeMinicapImageDataQueue",deviceId);
        if(nativeMinicapImageDataQueue!=null){
            nativeMinicapImageDataQueue.clear();
        }
        //6.关闭手机里的minicap  手机在连接状态下才关闭 手机断开后minicap不用杀就会关掉
        if(banner.getPid()>0 && androidDevice.isConnected()){
            try {
                String cmd = "kill "+banner.getPid();
                log.info("[{}]关闭手机里的minicap，{}",deviceId,cmd);
                iDevice.executeShellCommand(cmd,new NullOutputReceiver());
            } catch (Exception e) {
                log.error("[{}]关闭手机里的minicap失败",deviceId,e);
            }
        }
        //7.移除adb forward
        if(androidDevice.isConnected()) {
            try {
                log.info("[{}]removeForward : {} => localabstract:minicap,", deviceId, minicapPort);
                iDevice.removeForward(minicapPort, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            } catch (Exception e) {
                log.error("[{}]removeForward出错", deviceId, e);
            }
        }
        //8.归还端口
        log.info("[{}]归还minicap端口:{}",deviceId,minicapPort);
        MinicapPortProvider.pushAvailablePort(minicapPort);

        log.info("[{}]minicap资源回收完成",deviceId);

    }

    /**
     * 开启socket接收手机端minicap输出的数据
     * 并将数据放入nativeMinicapImageDataQueue队列
     * @throws Exception
     */
    public void putNativeMinicapImageDataToQueue() throws Exception{
        log.info("[{}]开始获取minicap输出的数据，本机端口{}",deviceId,minicapPort);
        try {
            socket = new Socket("127.0.0.1",minicapPort);
        } catch (IOException e) {
            log.error("connect minicap fail");
            throw e;
        }
        log.info("[{}]minicap socket连接成功,端口{}",deviceId,minicapPort);


        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            log.error("get minicap inputstream error",e);
            throw e;
        }
        //开启一个线程生产数据
        putNativeMinicapImageDataToQueueThread = new Thread(() -> {
            Thread thread = Thread.currentThread();
            log.info("[{}]开启一个线程将minicap输出的数据放入nativeMinicapImageDataQueue队列，thread id => {}",deviceId,thread.getId());
            while(true){
                int len = 0;
                byte[] buffer = null;
                try {
                    len = dataInputStream.available();
                } catch (IOException e) {
                    log.error("minicap stream available error",e);
                    return;
                }
                if(len!=0){
                    buffer = new byte[len];
                    try {
                        //将数据读到buffer里
                        dataInputStream.read(buffer);
                        //把读到的数据放入队列里
                        nativeMinicapImageDataQueue.offer(buffer);
                    } catch (IOException e) {
                        log.error("read minicap inputstream error",e);
                    }
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    log.info("[{}]thread id => {}，线程被打断",deviceId,thread.getId());
                    break;
                }
            }
            log.info("[{}]thread id => {},将minicap输出的数据放入nativeMinicapImageDataQueue队列线程被打断,本机端口{}接收minicap数据中断",deviceId,thread.getId(),minicapPort);
        });
        putNativeMinicapImageDataToQueueThread.start();
    }

    /**
     * 从nativeMinicapImageDataQueue队列拿到minicap输出的数据
     * 并解析出图片数据放入processedMinicapImageDataQueue队列
     * @return
     */
    public void parseNativeMinicapImageDataAndAddToQueue() throws Exception{

        parseNativeMinicapImageDataAndAddToQueueThread = new Thread(() ->{
            Thread thread = Thread.currentThread();
            log.info("[{}]开启一个线程解析原生minicap数据，并将图片数据放入processedMinicapImageDataQueue队列，thread id => {}",deviceId,thread.getId());

            while (true){
                byte[] binaryData = nativeMinicapImageDataQueue.poll();
                if(binaryData!=null){
                    int len = binaryData.length;
                    for (int cursor = 0; cursor < len;) {
                        int byte10 = binaryData[cursor] & 0xff;
                        if (readBannerBytes < bannerLength) {
                            cursor = parserBanner(cursor, byte10);
                        } else if (readFrameBytes < 4) {
                            // 第二次的缓冲区中前4位数字和为frame的缓冲区大小
                            frameBodyLength += (byte10 << (readFrameBytes * 8)) >>> 0;
                            cursor += 1;
                            readFrameBytes += 1;
                        } else {
                            //增加frame缓冲区的大小判断，防止出现负数的情况
                            if (len - cursor >= frameBodyLength && frameBodyLength>=0) {
                                byte[] subByte = ByteUtil.subByteArray(binaryData, cursor,
                                        cursor + frameBodyLength);
                                frameBody = ByteUtil.byteMerger(frameBody, subByte);
                                if ((frameBody.length>=1 && frameBody[0] != -1) || (frameBody.length>=2 && frameBody[1] != -40)) {
                                    log.error("Frame body does not start with JPG header");
                                    return;
                                }
                                byte[] finalBytes = ByteUtil.subByteArray(frameBody, 0,
                                        frameBody.length);
                                //将真正的图片字节数组放入队列
                                processedMinicapImageDataQueue.offer(finalBytes);
                                cursor += frameBodyLength;
                                frameBodyLength = 0;
                                readFrameBytes = 0;
                                frameBody = new byte[0];
                            } else if(len >= cursor) {
                                byte[] subByte = ByteUtil.subByteArray(binaryData, cursor,
                                        len);
                                frameBody = ByteUtil.byteMerger(frameBody, subByte);
                                frameBodyLength -= (len - cursor);
                                readFrameBytes += (len - cursor);
                                cursor = len;
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    log.info("[{}]thread id => {}，线程被打断",deviceId,thread.getId());
                    break;
                }
            }
            log.info("[{}]thread id => {},解析原生minicap数据并将图片数据放入processedMinicapImageDataQueue队列线程被打断,停止收集图片数据",deviceId,thread.getId(),minicapPort);
        });
        parseNativeMinicapImageDataAndAddToQueueThread.start();
    }


    private int parserBanner(int cursor, int byte10) {
        switch (readBannerBytes) {
            case 0:
                // version
                banner.setVersion(byte10);
                break;
            case 1:
                // length
                bannerLength = byte10;
                banner.setLength(byte10);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                // pid
                int pid = banner.getPid();
                pid += (byte10 << ((readBannerBytes - 2) * 8)) >>> 0;
                banner.setPid(pid);
                break;
            case 6:
            case 7:
            case 8:
            case 9:
                // real width
                int realWidth = banner.getReadWidth();
                realWidth += (byte10 << ((readBannerBytes - 6) * 8)) >>> 0;
                banner.setReadWidth(realWidth);
                break;
            case 10:
            case 11:
            case 12:
            case 13:
                // real height
                int realHeight = banner.getReadHeight();
                realHeight += (byte10 << ((readBannerBytes - 10) * 8)) >>> 0;
                banner.setReadHeight(realHeight);
                break;
            case 14:
            case 15:
            case 16:
            case 17:
                // virtual width
                int virtualWidth = banner.getVirtualWidth();
                virtualWidth += (byte10 << ((readBannerBytes - 14) * 8)) >>> 0;
                banner.setVirtualWidth(virtualWidth);

                break;
            case 18:
            case 19:
            case 20:
            case 21:
                // virtual height
                int virtualHeight = banner.getVirtualHeight();
                virtualHeight += (byte10 << ((readBannerBytes - 18) * 8)) >>> 0;
                banner.setVirtualHeight(virtualHeight);
                break;
            case 22:
                // orientation
                banner.setOrientation(byte10 * 90);
                break;
            case 23:
                // quirks
                banner.setQuirks(byte10);
                break;
        }

        cursor += 1;
        readBannerBytes += 1;

        if (readBannerBytes == bannerLength) {
            log.debug(banner.toString());
        }
        return cursor;
    }
}
