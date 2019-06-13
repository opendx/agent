package com.daxiang.actions.macaca;

import com.daxiang.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;
import macaca.client.commands.Element;

/**
 * Created by jiangyitao.
 */
public class WaitForElement {

    private MacacaClient driver;

    public WaitForElement(MacacaClient driver) {
        this.driver = driver;
    }

    /**
     * 等待元素出现
     */
    public Object excute(Object findBy, Object value, Object timeoutSecond) throws Exception {
        String _findBy = (String) findBy;
        String _value = (String) value;
        long timeout = Integer.parseInt((String) timeoutSecond) * 1000;

        Element element = MacacaUtil.waitForElement(driver, _findBy, _value, timeout);
        return element;
    }
}

