package com.daxiang.actions.macaca;

import macaca.client.MacacaClient;


/**
 * Created by jiangyitao.
 */
public class Keys {

    private MacacaClient driver;

    public Keys(MacacaClient driver) {
        this.driver = driver;
    }

    /**
     * 4: back 3:home 更多请见https://github.com/alibaba/macaca/issues/487
     * @param key
     * @throws Exception
     */
    public void excute(Object key) throws Exception {
        String _key = (String)key;
        driver.keys(_key);
    }
}
