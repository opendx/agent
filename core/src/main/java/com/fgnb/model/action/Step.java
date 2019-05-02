package com.fgnb.model.action;

import lombok.Data;
import java.util.List;

/**
 * Created by jiangyitao.
 */
@Data
public class Step {
    private Integer actionId;
    private Action action;
    private String name;
    private String evaluation;
    private Integer number;
    private List<ParamValue> paramValues;
}
