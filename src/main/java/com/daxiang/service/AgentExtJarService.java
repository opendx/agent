package com.daxiang.service;

import com.daxiang.core.classloader.AgentExtJarLoader;
import com.daxiang.exception.AgentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class AgentExtJarService {

    public void loadJar(String jarUrl) {
        if (StringUtils.isEmpty(jarUrl)) {
            throw new AgentException("jarUrl不能为空");
        }

        String jarName = FilenameUtils.getName(jarUrl);
        File jarFile = new File(AgentExtJarLoader.JAR_DIR, jarName);

        try {
            FileUtils.copyURLToFile(new URL(jarUrl), jarFile);
        } catch (IOException e) {
            log.error("download jar fail, jarUrl={}", jarUrl, e);
            throw new AgentException(e.getMessage());
        }

        AgentExtJarLoader.getInstance().load(jarFile);
    }
}
