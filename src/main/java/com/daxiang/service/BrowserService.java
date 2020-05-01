package com.daxiang.service;

import com.daxiang.core.pcweb.BrowserHolder;
import com.daxiang.model.Response;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by jiangyitao.
 */
@Service
public class BrowserService {

    public Response getAll() {
        return Response.success(BrowserHolder.getAll());
    }

    public Response findById(String id) {
        if (!StringUtils.hasText(id)) {
            return Response.fail("id不能为空");
        }
        return Response.success(BrowserHolder.get(id));
    }
}
