package com.daxiang.service;

import com.daxiang.core.javacompile.JavaCompiler;
import com.daxiang.core.testng.TestNGRunner;
import com.daxiang.model.Response;
import com.daxiang.model.devicetesttask.DeviceTestTask;
import com.daxiang.model.devicetesttask.Testcase;
import com.daxiang.model.request.ActionDebugRequest;
import com.daxiang.core.testng.TestNGCodeConverter;
import com.daxiang.utils.UUIDUtil;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.dvare.dynamic.exceptions.DynamicCompilerException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

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
        try {
            String className = "Debug_" + UUIDUtil.getUUID();

            DeviceTestTask deviceTestTask = new DeviceTestTask();
            BeanUtils.copyProperties(request, deviceTestTask);

            deviceTestTask.setTestcases(Arrays.asList(request.getAction()).stream().map(a -> {
                Testcase testcase = new Testcase();
                BeanUtils.copyProperties(a, testcase);
                return testcase;
            }).collect(Collectors.toList()));

            String code = new TestNGCodeConverter()
                    .convert(deviceTestTask, className, "/codetemplate", "mobile.ftl");
            log.info("[调试action][{}]code: {}", request.getDeviceId(), code);

            return compileAndDebug(className, code);
        } catch (Exception e) {
            log.error("调试出错", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 编译调试运行
     */
    public Response compileAndDebug(String className, String code) {
        try {
            Class clazz = JavaCompiler.compile(className, code);
            return TestNGRunner.debugAction(clazz, code);
        } catch (DynamicCompilerException e) {
            return Response.fail(e.getMessage(), ImmutableMap.of("code", code));
        }
    }

}
