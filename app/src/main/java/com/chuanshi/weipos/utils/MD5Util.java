package com.chuanshi.weipos.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5Util
 *
 * @created 2018/1/29
 */
public class MD5Util {

    private static final String TAG = "MD5Util";

    public static String toHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Character.forDigit((bytes[i] & 0XF0) >> 4, 16));
            sb.append(Character.forDigit(bytes[i] & 0X0F, 16));
        }
        return sb.toString();
    }

    public static String md5(String src) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            if (md != null && src != null) {
                md.update(src.getBytes("utf-8"));
                return toHexString(md.digest());
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return src;
    }

    /**
     * 是否相等
     *
     * @return
     */
    public static boolean isMD5equal(String md5, String filePath) {
        String fileMd5 = getFileMD5String(filePath);
        Log.d(TAG, "md5-----"+md5+"---isMD5equal: " + fileMd5 + "---filePath--" + filePath);
        if (!TextUtils.isEmpty(fileMd5) && md5.equals(fileMd5)) {
            Log.d(TAG, "!StringUtils.isEmpty(fileMd5) && md5.equals(fileMd5): " + fileMd5);
            return true;
        }
        return false;
    }

    public static String getFileMD5String(String filePath) {
        File file = new File(filePath);
        try {
            return getFileMD5String(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileMD5String(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        MessageDigest messagedigest = null;
        FileInputStream in = new FileInputStream(file);
        try {
            FileChannel ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            messagedigest = MessageDigest.getInstance("MD5");
            messagedigest.update(byteBuffer);
            return byteArrayToHex(messagedigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return null;
    }

    public static String byteArrayToHex(byte[] byteArray) {

        // 首先初始化一个字符数组，用来存放每个16进制字符

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


        // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））

        char[] resultCharArray = new char[byteArray.length * 2];


        // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去

        int index = 0;

        for (byte b : byteArray) {

            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];

            resultCharArray[index++] = hexDigits[b & 0xf];

        }
        // 字符数组组合成字符串返回
        return new String(resultCharArray);
    }

}
