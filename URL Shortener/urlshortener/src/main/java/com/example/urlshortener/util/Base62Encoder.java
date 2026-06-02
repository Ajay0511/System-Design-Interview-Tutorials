package com.example.urlshortener.util;


public class Base62Encoder {
    private static final String BASE62 =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encode(long value){
        if(value == 0){
            return "0";
        }
        StringBuilder stringBuilder = new StringBuilder();

        while(value > 0){
            stringBuilder.append(BASE62.charAt((int) value%62));
            value/=62;
        }

        return stringBuilder.reverse().toString();
    }
}
