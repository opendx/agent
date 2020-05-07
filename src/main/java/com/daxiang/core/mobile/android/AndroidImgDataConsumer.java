package com.daxiang.core.mobile.android;

import java.nio.ByteBuffer;

/**
 * Created by jiangyitao.
 */
public interface AndroidImgDataConsumer {
    void consume(ByteBuffer imgData);
}
