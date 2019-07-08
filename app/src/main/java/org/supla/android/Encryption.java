package org.supla.android;
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    private static SecretKey generateKey(String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        if (password == null) {
            password = "";
        }

        String alignment = "";
        for(int a=0;a<32-password.length();a++) {
            alignment+="0";
        }

        password+=alignment;
        password = password.substring(0, 32);

        return new SecretKeySpec(password.getBytes(), "AES");
    }

    public static byte[] encryptData(byte[] data, String password)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException,
            UnsupportedEncodingException, InvalidKeySpecException {
        /* Encrypt the message. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, generateKey(password));
        byte[] cipherText = cipher.doFinal(data);
        return cipherText;
    }

    public static byte[] decryptData(byte[] cipherText, String password)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeySpecException {
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, generateKey(password));
        return cipher.doFinal(cipherText);
    }

    public static byte[] decryptDataWithNullOnException(byte[] cipherText, String password) {
        byte[] result = null;
        try {
            result = decryptData(cipherText, password);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
        e.printStackTrace();
        }

        return result;
    }

    public static byte[] encryptDataWithNullOnException(byte[] data, String password) {
        byte[] result = null;
        try {
            result = encryptData(data, password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return result;
    }

}
