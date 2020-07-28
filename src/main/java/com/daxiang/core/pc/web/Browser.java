package com.daxiang.core.pc.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Data
public class Browser extends BrowserJsonItem {

    private Integer platform;  // 1.windows 2.linux 3.macos
    private Integer status;
    private String username;
    private String agentIp;
    private Integer agentPort;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastOnlineTime;

}
