package com.daxiang.controller;

import com.daxiang.core.MobileDeviceHolder;
import com.daxiang.core.ios.IosDevice;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Controller
public class IosIproxyController {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * iproxy进行端口转发后，web无法通过agent ip + wdaMjpegServerPort访问到屏幕数据，只能通过localhost获取
     * 这个接口充当代理的角色，通过这个接口内部访问localhost获取屏幕数据，转发给web显示
     * @param deviceId
     * @param response
     */
    @GetMapping("/iproxy/{deviceId}")
    public void getWdaMjpegStream(@PathVariable String deviceId, HttpServletResponse response) {
        if (StringUtils.isEmpty(deviceId)) {
            return;
        }

        IosDevice iosDevice = (IosDevice) MobileDeviceHolder.get(deviceId);
        String url = "http://localhost:" + iosDevice.getMjpegServerPort();

        restTemplate.execute(
                URI.create(url),
                HttpMethod.GET,
                (ClientHttpRequest request) -> {
                },
                responseExtractor -> {
                    response.setContentType("multipart/x-mixed-replace; boundary=--BoundaryString");
                    try {
                        IOUtils.copy(responseExtractor.getBody(), response.getOutputStream());
                    } catch (Exception e) {

                    }
                    return null;
                }
        );
    }
}
