package com.daxiang.model.action;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class Step {
    private Integer actionId;
    private Action action;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    private String name;
    private String evaluation;
    private Integer number;
    private List<ParamValue> paramValues;
}
