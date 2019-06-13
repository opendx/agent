package com.daxiang.actions.macaca;

import com.daxiang.actions.utils.ImplicitlyWait;
import com.daxiang.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;
import macaca.client.commands.Element;

/**
 * Created by jiangyitao.
 */
public class Click {

    private MacacaClient driver;

    public Click(MacacaClient driver) {
        this.driver = driver;
    }

    /**
     * 点击
     * @param findBy
     * @param value
     * @return
     * @throws Exception
     */
    public Object excute(Object findBy, Object value) throws Exception {
        String _findBy = (String) findBy;
        String _value = (String) value;
        Element element = MacacaUtil.waitForElement(driver, _findBy, _value, ImplicitlyWait.DEFAULT_MILLISECOND);
        element.click();
        return element;
    }
}
