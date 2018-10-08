package com.fgnb.actions.macaca;

import com.fgnb.actions.Action;
import com.fgnb.actions.utils.ImplicitlyWait;
import com.fgnb.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;
import macaca.client.commands.Element;

/**
 * Created by jiangyitao.
 */
public class Click extends Action {

    public Click(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {
        String findBy = params[0];
        String value = params[1];
        Element element = MacacaUtil.waitForElement(macacaClient, findBy, value, ImplicitlyWait.DEFAULT_WAIT_TIME_MS);
        element.click();
        return null;
    }

}
