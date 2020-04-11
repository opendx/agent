package com.daxiang.utils;

import com.daxiang.App;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class HttpUtil {

    public static void downloadFile(String url, File file) throws IOException {
        byte[] bytes = App.getBean(RestTemplate.class).getForObject(url, byte[].class);
        FileUtils.writeByteArrayToFile(file, bytes, false);
    }

    public static File downloadFile(String url) throws IOException {
        return downloadFile(url, true);
    }

    public static File downloadFile(String url, boolean renameFile) throws IOException {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (renameFile) {
            if (fileName.contains(".")) {
                fileName = UUIDUtil.getUUID() + "." + StringUtils.unqualify(fileName);
            } else {
                fileName = UUIDUtil.getUUID();
            }
        }

        File file = new File(fileName);
        downloadFile(url, file);

        return file;
    }
}
