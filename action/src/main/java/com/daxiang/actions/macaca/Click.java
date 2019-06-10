package com.daxiang.actions.macaca;

import com.daxiang.actions.utils.ImplicitlyWait;
import com.daxiang.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;
import macaca.client.commands.Element;

/**
 * Created by jiangyitao.
 */
public class Click {

    private MacacaClient macacaClient;

    public Click(MacacaClient macacaClient) {
        this.macacaClient = macacaClient;
    }

    public void excute(Object findBy, Object value) throws Exception {
        Element element = MacacaUtil.waitForElement(macacaClient, (String)findBy, (String)value, ImplicitlyWait.DEFAULT_WAIT_TIME_MS);
        element.click();
    }

}
