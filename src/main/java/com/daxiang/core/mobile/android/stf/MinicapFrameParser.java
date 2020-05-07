package com.daxiang.core.mobile.android.stf;

import com.daxiang.utils.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by jiangyitao.
 */
class MinicapFrameParser {

    private byte[] buffer = new byte[1024 * 1024];
    private int frameSize;

    /**
     * 解析出图片数据
     * https://github.com/openstf/minicap#frame-binary-format
     *
     * @param inputStream
     * @return
     */
    public ByteBuffer parse(InputStream inputStream) throws IOException, MinicapFrameSizeException {
        frameSize = IOUtil.readUInt32LE(inputStream);
        if (frameSize <= 0) {
            throw new MinicapFrameSizeException("获取图片数据失败,frameSize: " + frameSize);
        }

        if (buffer.length < frameSize) {
            // 扩容
            buffer = new byte[frameSize];
        }

        for (int i = 0; i < frameSize; i++) {
            buffer[i] = (byte) inputStream.read();
        }

        return ByteBuffer.wrap(buffer, 0, frameSize);
    }
}
