package com.daxiang.core.testng;

import com.daxiang.core.mobile.MobileDevice;
import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.model.devicetesttask.TestPlan;
import lombok.Data;

/**
 * Created by jiangyitao.
 */
@Data
public class TestDescription {
    private String deviceId;
    private Integer deviceTestTaskId;
    private Integer testcaseId;
    private Integer enableRecordVideo;
    private Integer failRetryCount;

    private Boolean recordVideo;
    private MobileDevice mobileDevice;

    /**
     * @param testDesc deviceId_deviceTestTaskId_testcaseId_enableRecordVideo_failRetryCount
     * @return
     */
    public TestDescription(String testDesc) {
        String[] testDescArr = testDesc.split("_");

        this.deviceId = testDescArr[0];
        this.deviceTestTaskId = Integer.valueOf(testDescArr[1]);
        this.testcaseId = Integer.valueOf(testDescArr[2]);
        this.enableRecordVideo = Integer.valueOf(testDescArr[3]);
        this.failRetryCount = Integer.valueOf(testDescArr[4]);

        this.mobileDevice = MobileDeviceHolder.get(deviceId);
        this.recordVideo = (enableRecordVideo == TestPlan.ENABLE_RECORD_VIDEO);
    }

    public static Integer parseTestcaseId(String testDesc) {
        return Integer.valueOf(testDesc.split("_")[2]);
    }
}
