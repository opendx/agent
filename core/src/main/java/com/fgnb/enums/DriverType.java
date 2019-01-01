package com.fgnb.enums;

/**
 * Created by jiangyitao.
 */
public enum DriverType {

    /* 1.CHROME */
    CHROME(1);

    private int type;

    DriverType(int i) {
        this.type = i;
    }

    public int getType(){
        return type;
    }
}
