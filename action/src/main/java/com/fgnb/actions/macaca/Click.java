package com.fgnb.actions.macaca;

import com.fgnb.actions.utils.ImplicitlyWait;
import com.fgnb.actions.utils.MacacaUtil;
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

    public void excute(String findBy, String value) throws Exception {
        Element element = MacacaUtil.waitForElement(macacaClient, findBy, value, ImplicitlyWait.DEFAULT_WAIT_TIME_MS);
        element.click();
    }

}
