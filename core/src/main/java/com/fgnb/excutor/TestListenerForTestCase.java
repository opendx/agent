package com.fgnb.excutor;

import com.alibaba.fastjson.JSONObject;
import com.fgnb.android.AndroidDevice;
import com.fgnb.android.AndroidDeviceHolder;
import com.fgnb.android.AndroidUtils;
import com.fgnb.api.MasterApi;
import com.fgnb.App;
import com.fgnb.utils.ShellExecutor;
import com.fgnb.utils.UUIDUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.*;

import static io.restassured.RestAssured.given;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class TestListenerForTestCase extends TestListenerAdapter {

    //ui-server resources配置文件中 eg.http://192.168.1.101:8081
    private static final String UI_SERVER_BASE_URL = App.getProperty("server");

    //设备开始测试
    private static final String TEST_TASK_DEVICE_START = UI_SERVER_BASE_URL + "/testTaskDevice/start";
    //设备测试结束
    private static final String TEST_TASK_DEVICE_FINISH = UI_SERVER_BASE_URL + "/testTaskDevice/finish";

    //测试用例开始
    private static final String TEST_CASE_RECORD_ADD = UI_SERVER_BASE_URL + "/testTaskReportTestCaseRecord/add";
    //测试用例失败/跳过/成功
    private static final String TEST_CASE_RECORD_UPDATE = UI_SERVER_BASE_URL + "/testTaskReportTestCaseRecord/update";

    //用例开始执行 在测试报告里的记录id
    private static final ThreadLocal<Integer> RECORD_ID = new ThreadLocal<>();
    
    //每个设备 图片视频存放目录
    private static final ThreadLocal<String> IMG_VIDEO_PATH = new ThreadLocal<>();

    //是否保持一直截图 来合成视频
    private static final ThreadLocal<KeepScreenShotFlag> KEEP_SCREENSHOT_FLAG = new ThreadLocal<>();
    public static class KeepScreenShotFlag {
        boolean flag;
        public KeepScreenShotFlag(boolean flag){
            this.flag = flag;
        }

        public boolean getFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }
    }

    private void handleTestTaskDevice(ITestContext testContext,String url) {
        ITestNGMethod testNGMethod = testContext.getAllTestMethods()[0];
        //testTaskId@&@testTaskReportId@&@deviceId@&@testCaseId@&@testCaseName
        String description = testNGMethod.getDescription();
        String[] _description = description.split("@&@");
        log.info("onStart/onFinish request => {testTaskId:{},deviceId:{}}",_description[0],_description[2]);
        Response response = given().param("testTaskId", _description[0]).param("deviceId", _description[2]).get(url);
        log.info("onStart/onFinish response <= {} ",response.asString());
    }

    @Override
    public void onStart(ITestContext testContext) {
        handleTestTaskDevice(testContext,TEST_TASK_DEVICE_START);
    }

    @Override
    public void onFinish(ITestContext testContext) {
        handleTestTaskDevice(testContext,TEST_TASK_DEVICE_FINISH);
    }

    @Override
    public void onTestStart(ITestResult tr) {

        //testTaskId@&@testTaskReportId@&@deviceId@&@testCaseId@&@testCaseName
        String description = tr.getMethod().getDescription();
        String[] _description = description.split("@&@");
        log.info("[{}]onTestStart description-> {}",_description[2],_description);

        //以设备id为名 创建目录（截图，视频存放路径）
        File deviceIdDir = new File(_description[2]);
        if(!deviceIdDir.exists()){
            deviceIdDir.mkdir();
        }
        //清空目录下的文件
        File[] files = deviceIdDir.listFiles();
        for(File file :files){
            file.delete();
        }
        //每个设备 图片视频存放目录
        IMG_VIDEO_PATH.set(_description[2]);

        //开启一个截图线程
        //持续截图标志
        KeepScreenShotFlag keepScreenShotFlag = new KeepScreenShotFlag(true);
        KEEP_SCREENSHOT_FLAG.set(keepScreenShotFlag);

        new Thread(()->{
            int i = 0;
            log.info("[{}]开始持续截图",_description[2]);
            while(true){
                if(keepScreenShotFlag == null || keepScreenShotFlag.getFlag() == false){
                    log.info("[{}]停止持续截图",_description[2]);
                    break;
                }
                takeScreenShot(_description[2]+"/"+i+".jpg",_description[2]);
                i++;
                //1秒截一次图
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("testTaskReportId",_description[1]);
        jsonObject.put("deviceId",_description[2]);
        jsonObject.put("testCaseId",_description[3]);
        jsonObject.put("testCaseName",_description[4]);

        log.info("onTestStart request => {}",jsonObject);
        Response response = given().contentType(ContentType.JSON).body(jsonObject).post(TEST_CASE_RECORD_ADD);
        log.info("onTestStart response <= {}",response.asString());
        Integer recordId = response.path("data.testTaskReportTestCaseRecordId");
        log.info("[{}]onTestStart recordId-> {}",_description[2],recordId);

        RECORD_ID.set(recordId);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {

        String videoUrl = generateVideoAndUpload();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("testTaskReportTestCaseRecordId",RECORD_ID.get());
        RECORD_ID.remove();
        jsonObject.put("videoUrl",videoUrl);
        //1.代表成功
        jsonObject.put("status",1);
        log.info("onTestSuccess request => {}",jsonObject);
        Response response = given().contentType(ContentType.JSON).body(jsonObject).post(TEST_CASE_RECORD_UPDATE);
        log.info("onTestSuccess response <= {}",response.asString());
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        String videoUrl = generateVideoAndUpload();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("testTaskReportTestCaseRecordId",RECORD_ID.get());
        RECORD_ID.remove();
        jsonObject.put("videoUrl",videoUrl);
        //失败截图
        String description = tr.getMethod().getDescription();
        String[] _description = description.split("@&@");
        String imgUrl = takeScreenShotAndUploadImg(_description[2]);
        jsonObject.put("imgUrl",imgUrl);
        //错误信息
        jsonObject.put("errorInfo",tr.getThrowable().getMessage());
        //0.代表失败
        jsonObject.put("status",0);
        log.info("onTestFailure request => {}",jsonObject);
        Response response = given().contentType(ContentType.JSON).body(jsonObject).post(TEST_CASE_RECORD_UPDATE);
        log.info("onTestFailure response <= {}",response.asString());
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        String videoUrl = generateVideoAndUpload();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("testTaskReportTestCaseRecordId",RECORD_ID.get());
        RECORD_ID.remove();
        jsonObject.put("videoUrl",videoUrl);
        //截图
        String description = tr.getMethod().getDescription();
        String[] _description = description.split("@&@");
        String imgUrl = takeScreenShotAndUploadImg(_description[2]);
        jsonObject.put("imgUrl",imgUrl);

        if(tr.getThrowable()==null) {
            //beforeSuite出错导致的
            jsonObject.put("errorInfo","初始化错误");
        }else{
            jsonObject.put("errorInfo",tr.getThrowable().getMessage());
        }
        //2.代表跳过
        jsonObject.put("status",2);
        log.info("onTestSkipped request => {}",jsonObject);
        Response response = given().contentType(ContentType.JSON).body(jsonObject).post(TEST_CASE_RECORD_UPDATE);
        log.info("onTestSkipped response <= {}",response.asString());
    }

    /**
     * 生成视频并上传到服务器
     * @return
     */
    private String generateVideoAndUpload() {
        //停止截图
        KEEP_SCREENSHOT_FLAG.get().setFlag(false);
        KEEP_SCREENSHOT_FLAG.remove();
        //合成视频
        //图片视频目录名字
        String imgAndVideoDir = IMG_VIDEO_PATH.get();
        if(!StringUtils.isEmpty(imgAndVideoDir)){
            File imgAndVideoDirFile = new File(imgAndVideoDir);
            File[] files = imgAndVideoDirFile.listFiles();
            if(files == null || files.length == 0){
                //没有图片
                return null;
            }
            //合成视频
            String dirPath = imgAndVideoDirFile.getAbsolutePath();
            String videoPath = dirPath +File.separator+UUIDUtil.getUUID()+".mp4";

            //-r 帧数 -i 输入文件 不用libx264编码前端<video>标签可能无法播放
            String cmd = "vendor/ffmpeg/bin/ffmpeg.exe -f image2 -r 1 -i "
                    + dirPath
                    +File.separator+"%d.jpg -vcodec libx264 "
                    +videoPath;
            log.info("[{}]合并视频命令：{}",imgAndVideoDir,cmd);

            try {
                ShellExecutor.exec(cmd);
            } catch (IOException e) {
                log.error("执行合成视频命令出错",e);
                return null;
            }
            log.info("[ffmpeg]视频合成完成，开始上传视频");
            //视频合成完毕 上传视频
            String videoUrl;
            try {
                videoUrl = App.getBean(MasterApi.class)
                        .uploadFile(new File(videoPath));
                log.info("视频上传成功，下载URL -> {}",videoUrl);
                return videoUrl;
            } catch (Exception e) {
                log.error("上传视频失败",e);
                return null;
            }
        }
        return null;
    }

    /**
     * 截图
     * @return 图片path
     */
    private String takeScreenShot(String imgFilePath, String deviceId){
        try{
            AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
            AndroidUtils.screenshotByMinicap(androidDevice.getIDevice(),imgFilePath,androidDevice.getResolution());
            return imgFilePath;
        }catch (Exception e){
            log.error("截图失败",e);
            return null;
        }
    }
    /**
     * 图片下载地址
     * @param deviceId
     * @return
     */
    private String takeScreenShotAndUploadImg(String deviceId){
        String imgFilePath = null;
        try{
            AndroidDevice androidDevice = AndroidDeviceHolder.get(deviceId);
            imgFilePath = UUIDUtil.getUUID()+".jpg";
            AndroidUtils.screenshotByMinicap(androidDevice.getIDevice(),imgFilePath,androidDevice.getResolution());
            MasterApi uiServerApi = App.getBean(MasterApi.class);
            return uiServerApi.uploadFile(new File(imgFilePath));
        }catch (Exception e){
            log.error("上传图片失败",e);
            return null;
        }finally {
            //删除生成的图片
            if(imgFilePath!=null){
                try {
                    new File(imgFilePath).delete();
                }catch (Exception e){
                    //ignore
                }
            }
        }
    }

}
