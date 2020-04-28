package com.rzm.downloadmodle.down;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {
    private final static char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	public static String md5(String s) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes("UTF-8"));
			return toHexString(digest.digest());
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return "";
	}

	public static String md5(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(data);
			return toHexString(digest.digest());
		} catch (NoSuchAlgorithmException e) {
		}
		return "";
	}

    public static String md5(File file) {
        FileInputStream fis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[2014];
            int len = 0;
            while((len=fis.read(buffer)) > 0) {
                digest.update(buffer, 0, len);
            }
            return toHexString(digest.digest());
        } catch (Exception e) {
            return "";
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }
    public static String toHexString(byte[] array) {
        if (array == null) {
            return "";
        }
        return toHexString(array, 0, array.length);
    }
    public static String toHexString(byte[] array, int offset, int length) {
        char[] buf = new char[length * 2];

        int bufIndex = 0;
        for (int i = offset; i < offset + length; i++) {
            byte b = array[i];
            buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
            buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
        }

        return new String(buf);
    }
}
