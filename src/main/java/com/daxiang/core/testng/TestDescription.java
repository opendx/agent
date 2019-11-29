package com.daxiang.core.testng;

import lombok.Data;
import org.springframework.util.StringUtils;

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

    /**
     *
     * @param testDesc deviceId_deviceTestTaskId_testcaseId_enableRecordVideo_failRetryCount
     * @return
     */
    public TestDescription(String testDesc) {
        if (StringUtils.isEmpty(testDesc)) {
            throw new IllegalArgumentException("testDesc cannot be empty!");
        }

        String[] testDescArr = testDesc.split("_");

        this.deviceId = testDescArr[0];
        this.deviceTestTaskId = Integer.valueOf(testDescArr[1]);
        this.testcaseId = Integer.valueOf(testDescArr[2]);
        this.enableRecordVideo = Integer.valueOf(testDescArr[3]);
        this.failRetryCount = Integer.valueOf(testDescArr[4]);
    }
}
