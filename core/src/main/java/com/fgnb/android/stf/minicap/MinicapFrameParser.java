package com.fgnb.android.stf.minicap;

import com.fgnb.utils.IOUtil;

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
            throw new NullPointerException("inputStream不能为空");
        }
        int frameSize = IOUtil.readUInt32LE(inputStream);
        if(frameSize < 0) {
            throw new MinicapFrameSizeException("获取图片数据失败,frameSize:" + frameSize);
        }
        byte[] img = new byte[frameSize];
        for (int i = 0; i < frameSize; i++) {
            img[i] = (byte) inputStream.read();
        }
        return img;
    }
}
