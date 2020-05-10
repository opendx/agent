package com.daxiang.core.pc.web;

import lombok.Data;

/**
 * Created by jiangyitao.
 */
@Data
public class BrowserJsonItem {
    private String id;         // 由agent生成并写入
    private String type;       // * 配置文件必填
    private String driverPath; // * 配置文件必填
    private String version;    // * 配置文件必填
    private String path;       // 浏览器路径，如: c:/chrome/chrome.exe
}
