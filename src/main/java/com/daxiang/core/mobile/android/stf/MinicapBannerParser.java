package com.daxiang.core.mobile.android.stf;

import com.daxiang.utils.IOUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jiangyitao.
 */
class MinicapBannerParser {

    /**
     * 解析出global header
     * https://github.com/openstf/minicap#global-header-binary-format
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public MinicapBanner parse(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream不能为空");
        }

        MinicapBanner minicapBanner = new MinicapBanner();
        minicapBanner.setVersion(inputStream.read());
        minicapBanner.setLength(inputStream.read());
        minicapBanner.setPid(IOUtil.readUInt32LE(inputStream));
        minicapBanner.setRealWidth(IOUtil.readUInt32LE(inputStream));
        minicapBanner.setRealHeight(IOUtil.readUInt32LE(inputStream));
        minicapBanner.setVirtualWidth(IOUtil.readUInt32LE(inputStream));
        minicapBanner.setVirtualHeight(IOUtil.readUInt32LE(inputStream));
        minicapBanner.setOrientation(inputStream.read());
        minicapBanner.setQuirks(inputStream.read());

        return minicapBanner;
    }

}
