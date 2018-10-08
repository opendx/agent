package com.fgnb.enums;

/**
 * Created by jiangyitao.
 */
public enum DeviceStfStatus {

    /* 0.失败 1.成功 */
    FAIL(0),SUCCESS(1);

    private int status;

    DeviceStfStatus(int i) {
        this.status = i;
    }

    public int getStatus(){
        return status;
    }
}
