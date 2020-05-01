package com.daxiang.core.pcweb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
public class BrowserHolder {

    private static final Map<String, Browser> BROWSER_HOLDER = new ConcurrentHashMap<>();

    public static void add(String browserId, Browser browser) {
        BROWSER_HOLDER.put(browserId, browser);
    }

    public static Browser get(String browserId) {
        return BROWSER_HOLDER.get(browserId);
    }

    public static List<Browser> getAll() {
        return new ArrayList<>(BROWSER_HOLDER.values());
    }

}
