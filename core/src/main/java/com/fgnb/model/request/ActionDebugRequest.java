package com.fgnb.model.request;

import com.fgnb.model.action.Action;
import com.fgnb.model.action.GlobalVar;
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
    @NotNull(message = "端口不能为空")
    private Integer port;
}
