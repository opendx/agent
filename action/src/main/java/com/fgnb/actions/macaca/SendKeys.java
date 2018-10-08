package com.fgnb.actions.macaca;

import com.fgnb.actions.Action;
import com.fgnb.actions.utils.ImplicitlyWait;
import com.fgnb.actions.utils.MacacaUtil;
import macaca.client.MacacaClient;
import macaca.client.commands.Element;


/**
 * Created by jiangyitao.
 */
public class SendKeys extends Action {

    public SendKeys(MacacaClient macacaClient) {
        super(macacaClient);
    }

    @Override
    public String excute(String... params) throws Exception {
        String findBy = params[0];
        String value = params[1];
        String sendContent = params[2];

        Element element = MacacaUtil.waitForElement(macacaClient, findBy, value, ImplicitlyWait.DEFAULT_WAIT_TIME_MS);
        element.sendKeys(sendContent);
        return null;
    }
}
