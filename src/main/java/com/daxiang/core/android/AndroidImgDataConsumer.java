package com.daxiang.core.android;

import java.nio.ByteBuffer;

/**
 * Created by jiangyitao.
 */
public interface AndroidImgDataConsumer {
    void consume(ByteBuffer imgData);
}
