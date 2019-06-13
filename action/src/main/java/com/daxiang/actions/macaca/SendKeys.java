package com.daxiang.actions.macaca;

import com.daxiang.actions.utils.ImplicitlyWait;
import com.daxiang.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;
import macaca.client.commands.Element;


/**
 * Created by jiangyitao.
 */
public class SendKeys {

    private MacacaClient driver;

    public SendKeys(MacacaClient driver) {
        this.driver = driver;
    }

    /**
     * 输入
     */
    public Object excute(Object findBy, Object value, Object content) throws Exception {
        String _findBy = (String) findBy;
        String _value = (String) value;
        String _content = (String) content;

        Element element = MacacaUtil.waitForElement(driver, _findBy, _value, ImplicitlyWait.DEFAULT_MILLISECOND);
        element.sendKeys(_content);
        return element;
    }
}
