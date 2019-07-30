package com.daxiang.core.android.stf;

import com.daxiang.utils.IOUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jiangyitao.
 */
public class MinicapFrameParser {

    /**
     * 解析出图片数据
     * https://github.com/openstf/minicap#frame-binary-format
     *
     * @param inputStream
     * @return
     */
    public static byte[] parse(InputStream inputStream) throws IOException, MinicapFrameSizeException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream不能为空");
        }
        int frameSize = IOUtil.readUInt32LE(inputStream);
        if (frameSize <= 0) {
            throw new MinicapFrameSizeException("获取图片数据失败,frameSize:" + frameSize);
        }
        // todo这里可能不需要每次都new
        byte[] img = new byte[frameSize];
        for (int i = 0; i < frameSize; i++) {
            img[i] = (byte) inputStream.read();
        }
        return img;
    }
}
