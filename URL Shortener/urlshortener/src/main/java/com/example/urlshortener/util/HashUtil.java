package com.example.urlshortener.util;

import java.security.MessageDigest;
import java.util.HexFormat;

public class HashUtil {
    public static String md5digest(String input){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            return HexFormat.of()
            .formatHex(digest);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
