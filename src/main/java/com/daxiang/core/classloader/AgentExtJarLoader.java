package com.daxiang.core.classloader;

import com.daxiang.model.AgentExtJar;
import com.daxiang.server.ServerClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AgentExtJarLoader {

    public static final String JAR_DIR = "ext";

    private static final AgentExtJarLoader INSTANCE = new AgentExtJarLoader();

    private Map<String, File> loadedJarMap;
    private AgentClassLoader classLoader;

    private AgentExtJarLoader() {
        loadedJarMap = new HashMap<>();
        classLoader = new AgentClassLoader();
    }

    public static AgentExtJarLoader getInstance() {
        return INSTANCE;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public synchronized void load(File jar) {
        String artifactId = getJarNameWithoutVersion(jar);
        if (loadedJarMap.containsKey(artifactId)) {
            // 使用新的ClassLoader加载其他已经加载过的jar，否则无法做到热更新
            classLoader = new AgentClassLoader();
            loadedJarMap.remove(artifactId);
            reload();
        }

        loadJar(jar);
        loadedJarMap.put(artifactId, jar);
    }

    private String getJarNameWithoutVersion(File jar) {
        Matcher matcher = Pattern.compile("(.+)-([0-9].*)\\.jar").matcher(jar.getName());
        if (matcher.find()) {
            return matcher.group(1); // spring-boot
        }

        throw new IllegalArgumentException(jar.getName() + "文件名不合法");
    }

    private void reload() {
        log.info("reload");
        loadedJarMap.values().forEach(this::loadJar);
    }

    private void loadJar(File jar) {
        log.info("loadJar: {}", jar);
        classLoader.addJar(jar);
    }

    /**
     * 初始化ext jar
     * 1. 以server返回的ext jar为准，EXT_JAR_DIR多删少下载
     * 2. 加载jar
     */
    public void initExtJars() {
        File extJarDir = new File(JAR_DIR);
        if (!extJarDir.exists()) {
            extJarDir.mkdir();
        }

        Set<AgentExtJar> serverJars = ServerClient.getInstance().getAgentExtJars();
        log.info("server extJars: {}", serverJars);

        Set<AgentExtJar> localJars = Stream.of(extJarDir.listFiles()).map(file -> {
            AgentExtJar localJar = new AgentExtJar();
            try {
                String md5 = DigestUtils.md5DigestAsHex(FileUtils.readFileToByteArray(file));
                localJar.setMd5(md5);
            } catch (IOException e) {
                boolean deleteSuccess = file.delete();
                log.error("read localJar={} err, delete it success? {}", file, deleteSuccess, e);
                return null;
            }
            localJar.setFilename(file.getName());
            localJar.setFile(file);
            return localJar;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        log.info("local extJars: {}", localJars);

        for (AgentExtJar localJar : localJars) {
            if (serverJars.contains(localJar)) {
                load(localJar.getFile());
            } else {
                // 删除和服务端不匹配的文件
                boolean deleteSucess = localJar.getFile().delete();
                log.info("delete local extJar: {} success? {}", localJar, deleteSucess);
            }
        }

        // 下载本地没有的jar
        serverJars.stream().filter(serverJar -> !localJars.contains(serverJar)).forEach(serverJar -> {
            try {
                File localJarFile = new File(JAR_DIR, serverJar.getFilename());
                log.info("download {} from {}", localJarFile, serverJar.getDownloadUrl());
                FileUtils.copyURLToFile(new URL(serverJar.getDownloadUrl()), localJarFile);
                load(localJarFile);
            } catch (IOException e) {
                log.error("download {} err", serverJar.getDownloadUrl(), e);
            }
        });
    }
}
