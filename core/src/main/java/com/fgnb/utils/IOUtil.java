package com.fgnb.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jiangyitao.
 */
public class IOUtil {

    public static int readUInt32LE(InputStream inputStream) throws IOException {
        return (inputStream.read() << 0 >>> 0) + (inputStream.read() << 8 >>> 0) + (inputStream.read() << 16 >>> 0) + (inputStream.read() << 24 >>> 0);
    }
}
