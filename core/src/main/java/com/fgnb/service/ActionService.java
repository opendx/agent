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
        String testClassName = "DebugClass_" + UUIDUtil.getUUID();
        String testNGCode;
        try {
            testNGCode = new TestNGCodeConverter()
                    .setActionTree(request.getAction())
                    .setTestClassName(testClassName)
                    .setIsBeforeSuite(false)
                    .setPlatform(request.getAction().getPlatform())
                    .setDeviceId(request.getDeviceId())
                    .setPort(request.getPort())
                    .setGlobalVars(request.getGlobalVars())
                    .setBasePackagePath("/codetemplate")
                    .setFtlFileName("testngCode.ftl")
                    .convert();
            if (StringUtils.isEmpty(testNGCode)) {
                return Response.fail("转换testng代码失败");
            }
        } catch (Exception e) {
            log.error("转换testng代码出错", e);
            return Response.fail("转换testng代码出错：" + e.getMessage());
        }

        log.info("[调试action]: {}", testNGCode);
        try {
            Class clazz = JavaCompiler.compile(testClassName, testNGCode);
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
