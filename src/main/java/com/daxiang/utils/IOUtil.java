package com.daxiang.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jiangyitao.
 */
public class IOUtil {

    public static int readUInt32LE(InputStream in) throws IOException {
        return (in.read() & 0xff) | (in.read() & 0xff) << 8 | (in.read() & 0xff) << 16 | (in.read() & 0xff) << 24;
    }
}
