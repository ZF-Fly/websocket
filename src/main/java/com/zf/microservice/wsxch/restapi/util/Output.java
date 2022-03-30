package com.zf.microservice.wsxch.restapi.util;

import java.util.Date;

public class Output {

    public static void debug() {
        debug("");
    }

    public static void debug(String format, Object...args) {
        if (isDebug) {
            String timestamp = DateUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss.SSS");
            System.out.println(timestamp + "\tDEBUG\t" + String.format(format, args));
        }
    }

    public static void print(String format, Object... args) {
        System.out.println(">>> " + String.format(format, args));
    }

    private static boolean isDebug = true;

}
