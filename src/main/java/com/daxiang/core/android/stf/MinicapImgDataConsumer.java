package com.daxiang.core.android.stf;

import java.nio.ByteBuffer;

/**
 * Created by jiangyitao.
 */
public interface MinicapImgDataConsumer {
    void consume(ByteBuffer minicapImgData);
}
