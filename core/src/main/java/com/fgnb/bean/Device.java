package com.fgnb.bean;

import lombok.Data;

import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Data
public class Device {
    /* 设备id */
    private String deviceId;
    /* 设备名 */
    private String deviceName;
    /* 设备ip */
    private String phoneIp;
    /* agent ip */
    private String agentIp;
    /* agent 端口 */
    private Integer agentPort;
    /* 系统版本 */
    private String systemVersion;
    /* api版本 */
    private String apiLevel;
    /* cpu架构 */
    private String cpuAbi;
    /* cpu */
    private String cpuInfo;
    /* 内存大小：GB */
    private String memSize;
    /* 分辨率 */
    private String resolution;
    /* 图片地址 */
    private String imgUrl;
    /*
     * 设备类型 1.android 2.ios
     */
    private Integer deviceType;
    /* 0.离线 1.空闲中 2.使用中 */
    private Integer status;

    /* 0.失败 1.成功 */
    private Integer stfStatus;

    /* 0.失败 1.成功 */
    private Integer macacaStatus;

    /* 最近的一次在线时间 */
    private Date lastOnlineTime;
    /* 最近的一次离线时间 */
    private Date lastOfflineTime;
    /* 使用人 */
    private String userName;

    private Date createTime;
}
