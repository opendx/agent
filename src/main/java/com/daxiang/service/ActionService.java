package com.daxiang.service;

import com.daxiang.core.JavaCompiler;
import com.daxiang.core.testng.TestNGCodeConvertException;
import com.daxiang.core.testng.TestNGCodeConverterFactory;
import com.daxiang.core.testng.TestNGRunner;
import com.daxiang.model.Response;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import com.daxiang.model.devicetesttask.Testcase;
import com.daxiang.model.request.ActionDebugRequest;
import com.daxiang.utils.UUIDUtil;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.dvare.dynamic.exceptions.DynamicCompilerException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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
        DeviceTestTask deviceTestTask = new DeviceTestTask();
        BeanUtils.copyProperties(request, deviceTestTask);

        Testcase testcase = new Testcase();
        BeanUtils.copyProperties(request.getAction(), testcase);
        deviceTestTask.setTestcases(Arrays.asList(testcase));

        String className = "Debug_" + UUIDUtil.getUUID();
        String code;
        try {
            code = TestNGCodeConverterFactory.create(deviceTestTask.getPlatform())
                    .convert(deviceTestTask, className);
        } catch (TestNGCodeConvertException e) {
            log.error("[{}]转换代码失败", request.getDeviceId(), e);
            return Response.fail(e.getMessage());
        }

        Response response = compileAndDebug(className, code);
        response.setData(ImmutableMap.of("code", code));
        return response;
    }

    /**
     * 编译调试运行
     */
    public Response compileAndDebug(String className, String code) {
        try {
            Class clazz = JavaCompiler.compile(className, code);
            return TestNGRunner.debugAction(clazz);
        } catch (DynamicCompilerException e) {
            log.error("编译{}失败: {}", className, e.getMessage());
            return Response.fail(e.getMessage());
        }
    }

}
