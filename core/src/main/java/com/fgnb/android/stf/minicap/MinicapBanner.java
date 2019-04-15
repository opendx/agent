package com.fgnb.android.stf.minicap;

import lombok.Data;

@Data
public class MinicapBanner {
    private int version;
    private int length;
    private int pid;
    private int readWidth;
    private int readHeight;
    private int virtualWidth;
    private int virtualHeight;
    private int orientation;
    private int quirks;
    private int maxX;
    private int maxY;
    private int maxPoint;
    private int maxPress;
}
