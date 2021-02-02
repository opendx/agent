package com.daxiang.core.testng;

import com.daxiang.model.devicetesttask.TestPlan;
import lombok.Data;

import static java.lang.Integer.*;

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

    public TestDescription(String deviceId, Integer deviceTestTaskId, Integer testcaseId,
                           Integer enableRecordVideo, Integer failRetryCount) {
        this.deviceId = deviceId;
        this.deviceTestTaskId = deviceTestTaskId;
        this.testcaseId = testcaseId;
        this.enableRecordVideo = enableRecordVideo;
        this.failRetryCount = failRetryCount;

        this.recordVideo = (enableRecordVideo == TestPlan.ENABLE_RECORD_VIDEO);
    }

    public static TestDescription create(String testDesc) {
        String[] testDescArr = testDesc.split("_");
        return new TestDescription(testDescArr[0], valueOf(testDescArr[1]),
                valueOf(testDescArr[2]), valueOf(testDescArr[3]), valueOf(testDescArr[4]));
    }

    public static Integer parseTestcaseId(String testDesc) {
        return valueOf(testDesc.split("_")[2]);
    }

    @Override
    public String toString() {
        return String.format("%s_%d_%d_%d_%d", deviceId, deviceTestTaskId, testcaseId,
                enableRecordVideo, failRetryCount);
    }
}
