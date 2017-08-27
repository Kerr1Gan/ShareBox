package org.ecjtu.easyserver.server.util.hash;

/**
 * Created by KerriGan on 2017/8/20.
 */

public class HashUtil {
    public static Long BKDRHash(String str){
        int seed=131;
        Long hash=0L;
        int len=str.length();
        int index=0;
        while (index < len){
            hash = hash * seed + (long)(str.charAt(index++));
        }
        return hash & 0x7FFFFFFF;
    }
}
