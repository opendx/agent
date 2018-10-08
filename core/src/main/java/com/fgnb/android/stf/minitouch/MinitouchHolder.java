package com.fgnb.android.stf.minitouch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangyitao.
 */
public class MinitouchHolder {


    private static Map<String,Minitouch> holder = new ConcurrentHashMap<>();

    public static void put(String deviceId,Minitouch minitouch){
        holder.put(deviceId,minitouch);
    }
    public static Minitouch get(String deviceId){
        return holder.get(deviceId);
    }

}
