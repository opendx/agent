package com.fgnb.android;

import com.fgnb.utils.NetUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by jiangyitao.
 */
public class PortProvider {

    private Set<Integer> availablePorts = Collections.synchronizedSet(new TreeSet<Integer>());

    private int startPort;
    private int endPort;

    public PortProvider(int startPort,int endPort){
        this.startPort = startPort;
        this.endPort = endPort;
        int port = startPort;
        //获取可用端口
        while(port<=endPort){
            if(NetUtil.isPortAvailable(port)){
                //如果端口没有被占用，那么就添加
                availablePorts.add(port);
            }
            port++;
        }
    }

    /**
     * 获取可用端口
     * @return
     */
    public synchronized int getAvailablePorts(){
        Iterator<Integer> it = availablePorts.iterator();
        if(it.hasNext()){
            Integer port = it.next();
            //从set中移除
            availablePorts.remove(port);
            return port.intValue();
        }else{
            return -1;
        }
    }

    /**
     * 归还端口号
     * @param port 端口号
     * @return boolean
     */
    public boolean pushAvailablePorts(int port){
        if(port>=startPort && port<=endPort){
            return availablePorts.add(port);
        }else{
            return false;
        }
    }

}
