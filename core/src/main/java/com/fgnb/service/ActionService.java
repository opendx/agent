package com.fgnb.service;

import com.fgnb.javacompile.JavaCompiler;
import com.fgnb.testng.TestNGRunner;
import com.fgnb.model.Response;
import com.fgnb.model.request.ActionDebugRequest;
import com.fgnb.testng.TestNGCodeConverter;
import com.fgnb.utils.UUIDUtil;
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
            String testNGCode = new TestNGCodeConverter().setDeviceId(request.getDeviceId()).setPort(request.getPort()).setGlobalVars(request.getGlobalVars())
                    .convert(className, Arrays.asList(request.getAction()), "/codetemplate", "android.ftl");
            log.info("[调试action]: {}", testNGCode);
            if (StringUtils.isEmpty(testNGCode)) {
                return Response.fail("转换testng代码失败");
            }
            Class clazz = JavaCompiler.compile(className, testNGCode);
            String failMsg = TestNGRunner.debugAction(clazz);
            if (StringUtils.isEmpty(failMsg)) {
                return Response.success("执行成功");
            } else {
                return Response.fail(failMsg);
            }
        } catch (Exception e) {
            log.error("调试出错", e);
            return Response.fail("调试出错：" + e.getMessage());
        }
    }
}
