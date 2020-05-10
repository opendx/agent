package com.daxiang.core.testng;

import com.daxiang.core.Device;
import com.daxiang.core.DeviceHolder;
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
    private Device device;

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

        this.device = DeviceHolder.get(deviceId);
        this.recordVideo = (enableRecordVideo == TestPlan.ENABLE_RECORD_VIDEO);
    }

    public Integer setTestcaseIdByTestDesc(String testDesc) {
        testcaseId = Integer.valueOf(testDesc.split("_")[2]);
        return testcaseId;
    }
}
