package com.fgnb.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class PageServiceDebugForm {

    @NotBlank(message = "端口不能为空")
    private String port;
    @NotBlank(message = "设备id不能为空")
    private String deviceId;
    @NotEmpty(message = "步骤不能为空")
    private List<PageServiceStep> steps;

}
