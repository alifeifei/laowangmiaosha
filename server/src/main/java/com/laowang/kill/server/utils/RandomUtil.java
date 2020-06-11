package com.laowang.kill.server.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
    private static ThreadLocalRandom random = ThreadLocalRandom.current();
    public static String generateOrderCode(){
        return simpleDateFormat.format(new Date())+generateNumber(4);
    }

    public static String generateNumber(int num){
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < num; i++){
            stringBuffer.append(random.nextInt(9));
        }
        return stringBuffer.toString();
    }



}
