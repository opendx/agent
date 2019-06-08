package com.fgnb.model.action;

import lombok.Data;


/**
 * Created by jiangyitao.
 */
@Data
public class LocalVar {
    /**
     * 被引用时的前缀
     */
    public static final String QUOTE_PREFIX = "@{";
    /**
     * 被引用时的后缀
     */
    public static final String QUOTE_SUFFIX = "}";

    private String name;
    private String value;
}
