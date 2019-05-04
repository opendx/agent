package com.fgnb.model.devicetesttask;


import com.fgnb.model.action.Action;
import com.fgnb.model.action.GlobalVar;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class DeviceTestTask {

    /** 未开始 */
    public static final Integer UNSTART_STATUS = 0;
    /** 运行中 */
    public static final Integer RUNNING_STATUS = 1;
    /** 完成 */
    public static final Integer FINISHED_STATUS = 2;

    private Integer id;
    private Integer testTaskId;
    private String testTaskName;
    private String deviceId;
    private Integer status;
    private Date startTime;
    private Date endTime;
    private List<GlobalVar> globalVars;
    private Action beforeSuite;
    private List<Testcase> testcases;
}