package com.daxiang.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by jiangyitao.
 */
public class FileUtil {

    public static File downloadFile(String url) throws IOException {
        File file = new File(UUIDUtil.getUUID());
        FileUtils.copyURLToFile(new URL(url), file);
        return file;
    }
}
