package com.daxiang.model.request;

import com.daxiang.model.action.Action;
import com.daxiang.model.action.GlobalVar;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class ActionDebugRequest {
    @NotNull(message = "action不能为空")
    private Action action;
    private List<GlobalVar> globalVars;
    private String deviceId;
}
