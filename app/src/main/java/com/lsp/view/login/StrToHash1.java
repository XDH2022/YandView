package com.lsp.view.login;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StrToHash1 {
    public static String shaEncrypt(String strSrc){
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(bt);
            strDes = bytes2Hex(md.digest());

        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;


    }
    private static String bytes2Hex(byte[] bts){
        StringBuilder des = new StringBuilder();
        String tmp = null;
        for (int i =0;i<bts.length;i++){
            tmp = Integer.toHexString(bts[i] & 0xFF);
            if (tmp.length()==1){
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }
}
