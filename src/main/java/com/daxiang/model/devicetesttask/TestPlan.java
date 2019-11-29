package com.daxiang.model.devicetesttask;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlan implements Serializable {
    public static final int ENABLE_RECORD_VIDEO = 1;

    private Integer id;
    private String name;
    private String description;
    private Integer enableRecordVideo;
    private Integer failRetryCount;
}