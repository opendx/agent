package com.fgnb.android.stf.minitouch;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.net.Socket;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class Minitouch {


    private AndroidDevice androidDevice;

    private Socket socket;

    /** minitouch输出的宽度 这个值有可能不准确*/
    private int minitouchWidth;
    /** minitouch输出的高度 这个值有可能不准确*/
    private int minitouchHeight;
    /** minitouch在手机运行的pid */
    private int pid;

    private int minitouchPort;

    private OutputStream os;
    private PrintWriter pw;
    private InputStream inputStream;
    private BufferedReader br;

    private final static String INPUT_KEYEVENT = "input keyevent ";

    public Minitouch(AndroidDevice androidDevice,int minitouchPort){
        this.minitouchPort = minitouchPort;
        this.androidDevice = androidDevice;

        try {
            socket = new Socket("127.0.0.1",minitouchPort);
            log.info("[{}]开启socket连接minitouch,socket连接成功,端口{}",androidDevice.getId(),minitouchPort);
            os = socket.getOutputStream();
            pw = new PrintWriter(os);
            //获取 minitouch 输出的屏幕宽度 高度 pid
            inputStream = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line=br.readLine())!=null){
                if(line.startsWith("^")){
                    // ^ 10 1079 1919 2048
                    String[] split = line.split(" ");
                    minitouchWidth = Integer.parseInt(split[2]);
                    minitouchHeight = Integer.parseInt(split[3]);
                    log.info("[{}]minitouch输出设备宽度:{},高度:{}",androidDevice.getId(),minitouchWidth,minitouchHeight);
                }else if(line.startsWith("$")){
                    // $ 12310
                    pid = Integer.parseInt(line.split(" ")[1]);
                    log.info("[{}]minitouch在设备中运行的pid => {}",androidDevice.getId(),pid);
                    break;
                }
            }
            log.info("[{}]minitouch初始化完成",androidDevice.getId());
        } catch (IOException e) {
            log.error("初始化minitouch socket连接出错",e);
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    log.error("关闭minicap socket inputStream出错",e);
                }
            }
            if(br!=null){
                try {
                    br.close();
                } catch (IOException e1) {
                    log.error("关闭minicap socket bufferedReader出错",e);
                }
            }
        }
    }

    /**
     * 释放资源
     */
    public void releaseResources(){
        log.info("[{}]开始回收minitouch资源",androidDevice.getId());
        //1.关闭输入输出流
        closeStream();
        //2.关闭socket
        if(socket!=null && socket.isConnected()){
            try {
                log.info("[{}]关闭minitouch socket,port=>{}",androidDevice.getId(),minitouchPort);
                socket.close();
            } catch (IOException e) {
                log.error("[{}]关闭minitouch socket出错",androidDevice.getId());
            }
        }
        //3.关闭手机里的minitouch
        if(pid>0 && androidDevice.isConnected()){
            try {
                String cmd = "kill "+pid;
                log.info("[{}]关闭手机里的minitouch，{}",androidDevice.getId(),cmd);
                androidDevice.getIDevice().executeShellCommand(cmd,new NullOutputReceiver());
            } catch (Exception e) {
                log.error("[{}]关闭手机里的minitouch出错",androidDevice.getId(),e);
            }
        }
        //4.移除adb forward
        if(androidDevice.isConnected()) {
            try {
                log.info("[{}]removeForward : {} => localabstract:minitouch,", androidDevice.getId(), minitouchPort);
                androidDevice.getIDevice().removeForward(minitouchPort, "minitouch", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            } catch (Exception e) {
                log.error("[{}]removeForward出错", androidDevice.getId(), e);
            }
        }

        log.info("[{}]minitouch资源回收完成",androidDevice.getId());

    }

    private void closeStream() {
        if(inputStream!=null){
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("[{}]关闭minitouch输入流错误",androidDevice.getId(),e);
            }
        }
        if(br!=null){
            try {
                br.close();
            } catch (IOException e) {
                log.error("[{}]关闭minitouch BufferedReader错误",androidDevice.getId(),e);
            }
        }
        if(os!=null){
            try {
                os.close();
            } catch (IOException e) {
                log.error("[{}]关闭minitouch BufferedReader错误",androidDevice.getId(),e);
            }
        }
        if(pw!=null){
            pw.close();
        }
    }

    /**
     * input keyevent + keyCode
     * keyCode对照表https://blog.csdn.net/moyu123456789/article/details/71209893
     * @param keyCode
     */
    public void inputKeyevent(int keyCode){
        try {
            String cmd = INPUT_KEYEVENT+keyCode;
            androidDevice.getIDevice().executeShellCommand(cmd,new NullOutputReceiver());
            log.debug("[{}]minitouch commit => {}",androidDevice.getId(),cmd);
        } catch (Exception e){
            log.error("input keyevent error",e);
        }
    }

    /**
     * 按下
     * @param x
     * @param y
     */
    public void touchDown(int x,int y){
        String command = String.format("d 0 %s %s 50\n", x,y);
        commitcMinitouchCmd(command);
    }

    /**
     * 松手
     */
    public void touchUp() {
        String command = "u 0\n";
        commitcMinitouchCmd(command);
    }

    /**
     * 滑动
     */
    public void moveTo(int x,int y) {
        String command = String.format("m 0 %d %d 50\n", x, y);
        commitcMinitouchCmd(command);
    }


    /**
     * 提交minitouch命令
     * @param cmd
     */
    private void commitcMinitouchCmd(String cmd){
        if(os!=null && pw!=null){
            String commitCmd = cmd + "c\n";
            pw.write(commitCmd);
            pw.flush();
        }
    }

    public int getMinitouchWidth() {
        return minitouchWidth;
    }

    public int getMinitouchHeight() {
        return minitouchHeight;
    }

}
