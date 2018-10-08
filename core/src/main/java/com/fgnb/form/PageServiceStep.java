package com.fgnb.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;

/**
 * Created by jiangyitao.
 */
@Data
public class PageServiceStep {
    @NotBlank(message = "className不能为空")
    private String className;
    private HashMap params;
}
