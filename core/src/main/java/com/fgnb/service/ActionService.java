package com.fgnb.service;

import com.fgnb.javacompile.JavaCompiler;
import com.fgnb.model.Platform;
import com.fgnb.testng.TestNGRunner;
import com.fgnb.model.Response;
import com.fgnb.model.request.ActionDebugRequest;
import com.fgnb.testng.TestNGCodeConverter;
import com.fgnb.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        String testNGCode;

        if (request.getAction().getPlatform() == Platform.Android) {
            try {
                testNGCode = new TestNGCodeConverter()
                        .setActionTree(request.getAction())
                        .setClassName(className)
                        .setIsBeforeSuite(false)
                        .setDeviceId(request.getDeviceId())
                        .setPort(request.getPort())
                        .setGlobalVars(request.getGlobalVars())
                        .setBasePackagePath("/codetemplate")
                        .setFtlFileName("android.ftl")
                        .convert();
                log.info("[调试action]: {}", testNGCode);
                if (StringUtils.isEmpty(testNGCode)) {
                    return Response.fail("转换testng代码失败");
                }
            } catch (Exception e) {
                log.error("转换testng代码出错", e);
                return Response.fail("转换testng代码出错：" + e.getMessage());
            }
        } else {
            return Response.fail("平台暂不支持");
        }

        try {
            Class clazz = JavaCompiler.compile(className, testNGCode);
            String failMsg = TestNGRunner.debugAction(clazz);
            if (StringUtils.isEmpty(failMsg)) {
                return Response.success("执行成功");
            } else {
                return Response.fail(failMsg);
            }
        } catch (Exception e) {
            log.error("调试action出错", e);
            return Response.fail(e.getMessage());
        }
    }
}
