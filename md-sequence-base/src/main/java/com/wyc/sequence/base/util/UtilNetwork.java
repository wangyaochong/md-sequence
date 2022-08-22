package com.wyc.sequence.base.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class UtilNetwork {
    //获取本机ip地址
    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String localIp = getLocalIp();
        System.out.println(localIp);
    }

}
