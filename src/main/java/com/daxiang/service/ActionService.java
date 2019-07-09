package com.daxiang.service;

import com.daxiang.javacompile.JavaCompiler;
import com.daxiang.testng.TestNGRunner;
import com.daxiang.model.Response;
import com.daxiang.model.request.ActionDebugRequest;
import com.daxiang.testng.TestNGCodeConverter;
import com.daxiang.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class ActionService {

    /**
     * 调试action
     *
     * @param request
     * @return
     */
    public Response debug(ActionDebugRequest request) {
        String className = "Debug_" + UUIDUtil.getUUID();
        try {
            String code = new TestNGCodeConverter().setDeviceId(request.getDeviceId()).setPort(request.getPort()).setGlobalVars(request.getGlobalVars())
                    .convert(className, Arrays.asList(request.getAction()), "/codetemplate", "android.ftl");
            log.info("[调试action]: {}", code);
            if (StringUtils.isEmpty(code)) {
                return Response.fail("转换testng代码失败");
            }

            Class clazz = JavaCompiler.compile(className, code);
            String failMsg = TestNGRunner.debugAction(clazz);
            if (StringUtils.isEmpty(failMsg)) {
                return Response.success("执行成功");
            } else {
                return Response.fail(failMsg);
            }
        } catch (Exception e) {
            log.error("调试出错", e);
            return Response.fail(e.getMessage());
        }
    }
}
