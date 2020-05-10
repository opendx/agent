package com.daxiang.core.mobile;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Data
public class Mobile {

    public static final int REAL_MOBILE = 0;
    public static final int EMULATOR = 1;

    private String id;
    private String name;
    private Integer emulator;
    private String agentIp;
    private Integer agentPort;
    private String systemVersion;
    private String cpuInfo;
    private String memSize;
    private Integer screenWidth;
    private Integer screenHeight;
    private String imgPath;
    private Integer platform;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastOnlineTime;
    private String username;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}