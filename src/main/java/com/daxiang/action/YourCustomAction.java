package com.daxiang.action;

import com.daxiang.core.action.annotation.Action;
import com.daxiang.core.action.annotation.Param;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Created by jiangyitao.
 * id: 5000 - 10000
 */
public class YourCustomAction {

    @Action(id = 5000, name = "随机字符串（数字 & 字母）")
    public static String randomAlphanumeric(@Param(description = "字符串长度") int count) {
        return RandomStringUtils.randomAlphanumeric(count);
    }

}
