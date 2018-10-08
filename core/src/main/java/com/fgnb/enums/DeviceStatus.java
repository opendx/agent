package com.fgnb.enums;

/**
 * Created by jiangyitao.
 */
public enum DeviceStatus {

    /* 0.离线 1.空闲中 2.使用中 */
    OFFLINE(0),IDLE(1),USING(2);

    private int status;

    DeviceStatus(int i) {
        this.status = i;
    }

    public int getStatus(){
        return status;
    }
}
