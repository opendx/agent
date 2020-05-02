package com.daxiang.service;

import com.daxiang.core.pcweb.Browser;
import com.daxiang.core.pcweb.BrowserHolder;
import com.daxiang.model.Response;
import com.daxiang.server.ServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
@Service
public class BrowserService {

    @Autowired
    private ServerClient serverClient;

    public Response getStatus(String browserId) {
        return StringUtils.isEmpty(browserId) ? Response.success(BrowserHolder.getAll())
                : Response.success(BrowserHolder.get(browserId));
    }

    public void saveUsingBrowserToServer(Browser browser) {
        browser.setStatus(browser.USING_STATUS);
        serverClient.saveBrowser(browser);
    }

    public void saveIdleBrowserToServer(Browser browser) {
        browser.setStatus(Browser.IDLE_STATUS);
        serverClient.saveBrowser(browser);
    }
}
