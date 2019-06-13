package com.daxiang.actions.macaca;

import com.daxiang.actions.utils.ImplicitlyWait;
import com.daxiang.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;
import macaca.client.commands.Element;

/**
 * Created by jiangyitao.
 */
public class ClearText {

    private MacacaClient driver;

    public ClearText(MacacaClient driver) {
        this.driver = driver;
    }

    /**
     * 清除文本
     */
    public Object excute(Object findBy, Object value) throws Exception {
        String _findBy = (String) findBy;
        String _value = (String) value;

        Element element = MacacaUtil.waitForElement(driver, _findBy, _value, ImplicitlyWait.DEFAULT_MILLISECOND);
        element.clearText();
        return element;
    }
}
