package com.daxiang.model.devicetesttask;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.daxiang.model.action.Action;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Created by jiangyitao.
 */
@Data
public class Testcase extends Action {

    public static final int FAIL_STATUS = 0;
    public static final int PASS_STATUS = 1;
    public static final int SKIP_STATUS = 2;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date endTime;
    /**
     * 失败截图
     */
    private String failImgPath;
    /**
     * 失败信息
     */
    private String failInfo;
    /**
     * 运行视频
     */
    private String videoPath;
    private String logPath;
    private Integer status;
}
