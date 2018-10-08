package com.fgnb.enums;

/**
 * Created by jiangyitao.
 */
public enum DeviceType {

    /* 1.android 2.ios */
    ANDROID(1),IOS(2);

    private int type;

    DeviceType(int i) {
        this.type = i;
    }

    public int getType(){
        return type;
    }
}
