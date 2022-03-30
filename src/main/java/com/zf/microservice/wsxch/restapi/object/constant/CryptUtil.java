package com.zf.microservice.wsxch.restapi.object.constant;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码学工具类
 */
public class CryptUtil {

    //region MD5

    public static String md5(String plainText) {
        try {
            byte[] plainData = plainText.getBytes();

            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            messageDigest.update(plainData);

            // 获得密文
            byte[] digestData = messageDigest.digest();

            // 把密文转换成十六进制的字符串形式
            int maxlength = digestData.length;
            char[] cryptData = new char[maxlength * 2];
            int index = 0;
            for (byte digest : digestData) {
                cryptData[index++] = HEX_DIGITS[digest >>> 4 & 0xf];
                cryptData[index++] = HEX_DIGITS[digest & 0xf];
            }

            return new String(cryptData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //endregion

    //region DES

    public static String desEncrypt(String source) throws Exception {
        return desEncrypt(source, DEFAULT_CRYPT_KEY);
    }

    public static String desDecrypt(String source) throws Exception {
        return desDecrypt(source, DEFAULT_CRYPT_KEY);
    }

    public static String desEncrypt(String source, String cryptKey) throws Exception {
        byte[] sourceData = source.getBytes();
        byte[] keyData = cryptKey.getBytes();

        byte[] targetData = desCrypt(Cipher.ENCRYPT_MODE, sourceData, keyData);

        return byte2hex(targetData);
    }

    public static String desDecrypt(String source, String cryptKey) throws Exception {
        byte[] sourceData = source.getBytes(UTF_8);
        byte[] keyData = cryptKey.getBytes(UTF_8);

        sourceData = hex2byte(sourceData);

        byte[] targetData = desCrypt(Cipher.DECRYPT_MODE, sourceData, keyData);

        return new String(targetData, UTF_8);
    }

    private static byte[] desCrypt(int mode, byte[] sourceData, byte[] keyData) throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        DESKeySpec keySpec = new DESKeySpec(keyData);

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM_DES);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
        cipher.init(mode, secretKey, secureRandom);

        return cipher.doFinal(sourceData);
    }

    //endregion

    //region AES

    public static String aesEncrypt(String source, String cryptKey) throws Exception {
        byte[] sourceData = source.getBytes(UTF_8);
        byte[] keyData = cryptKey.getBytes(UTF_8);

        byte[] targetData = aesCrypt(Cipher.ENCRYPT_MODE, sourceData, keyData);

        return new String(targetData, UTF_8);
    }

    public static String aesDecrypt(String source, String cryptKey) throws Exception {
        byte[] sourceData = source.getBytes(UTF_8);
        byte[] keyData = cryptKey.getBytes(UTF_8);

        byte[] targetData = aesCrypt(Cipher.DECRYPT_MODE, sourceData, keyData);

        return new String(targetData, UTF_8);
    }

    public static String aesEncrypt(String source) throws Exception {
        return aesEncrypt(source, DEFAULT_CRYPT_KEY);
    }

    public static String aesDecrypt(String target) throws Exception {
        return aesDecrypt(target, DEFAULT_CRYPT_KEY);
    }

    private static byte[] aesCrypt(int mode, byte[] sourceData, byte[] keyData) throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM_AES);
        generator.init(128);

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(mode, new SecretKeySpec(keyData, ALGORITHM_AES));

        if (Cipher.DECRYPT_MODE == mode) {
            sourceData = Base64.getDecoder().decode(sourceData);
        }

        byte[] targetData = cipher.doFinal(sourceData);

        if (Cipher.ENCRYPT_MODE == mode) {
            targetData = Base64.getEncoder().encode(targetData);
        }

        return targetData;
    }

    //endregion

    //region SETTINGS

    private static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0)
            throw new IllegalArgumentException("the length must be even.");
        byte[] b2 = new byte[b.length / 2];

        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }

        return b2;
    }

    private static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp = "";

        for (byte value : b) {
            stmp = (Integer.toHexString(value & 0XFF));
            if (stmp.length() == 1)
                hs.append("0").append(stmp);
            else
                hs.append(stmp);
        }

        return hs.toString().toUpperCase();
    }

    private static final char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    //endregion


    private static final String DEFAULT_CRYPT_KEY = "jZ7dYZziH15s%N4Ms";

    private static Charset UTF_8 = StandardCharsets.UTF_8;

    private static final String ALGORITHM_AES = "AES";
    private static final String ALGORITHM_DES = "DES";

}
