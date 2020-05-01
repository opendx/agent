package com.daxiang.core.pcweb;

import lombok.Data;

/**
 * Created by jiangyitao.
 */
@Data
public class BrowserJsonItem {
    protected String id;         // 由agent生成并写入
    protected String type;       // * 配置文件必填
    protected String driverPath; // * 配置文件必填
    protected String version;    // * 配置文件必填
    protected String name;       // 非必填
    protected String path;       // 浏览器路径，如: c:/chrome/chrome.exe
}
