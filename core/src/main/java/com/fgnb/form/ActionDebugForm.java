package com.fgnb.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;

/**
 * Created by jiangyitao.
 */
@Data
public class ActionDebugForm {

    @NotBlank(message = "端口不能为空")
    private String port;

    @NotBlank(message = "设备id不能为空")
    private String deviceId;
    @NotBlank(message = "className不能为空")
    private String className;
    private HashMap params;

}
