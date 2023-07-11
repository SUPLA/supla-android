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
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
  private static SecretKey generateKey(String password)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    // *Password
    // This is not a good implementation but sufficient for current use

    if (password == null) {
      password = "";
    }

    String alignment = "";
    for (int a = 0; a < 32 - password.length(); a++) {
      alignment += "0";
    }

    password += alignment;
    password = password.substring(0, 32);

    return new SecretKeySpec(password.getBytes(), "AES");
  }

  public static byte[] encryptData(byte[] data, String password)
      throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
          InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException,
          UnsupportedEncodingException, InvalidKeySpecException,
          InvalidAlgorithmParameterException {
    /* Encrypt the message. */

    Cipher cipher;
    cipher = Cipher.getInstance("AES/GCM/NoPadding");

    byte[] iv = new byte[12];
    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(iv);

    cipher.init(Cipher.ENCRYPT_MODE, generateKey(password), new GCMParameterSpec(128, iv));
    byte[] encryptedData = cipher.doFinal(data);
    ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
    byteBuffer.put(iv);
    byteBuffer.put(encryptedData);
    return byteBuffer.array();
  }

  public static byte[] decryptData(byte[] cipherText, String password, boolean deprecatedAlg)
      throws Exception {
    Cipher cipher;
    if (deprecatedAlg) {
      cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, generateKey(password));
      return cipher.doFinal(cipherText);
    }

    if (cipherText.length < 29) {
      throw new Exception("Invalid cipher length!");
    }

    cipher = Cipher.getInstance("AES/GCM/NoPadding");

    ByteBuffer byteBuffer = ByteBuffer.wrap(cipherText);
    byte[] iv = new byte[12];
    byteBuffer.get(iv);

    cipherText = new byte[byteBuffer.remaining()];
    byteBuffer.get(cipherText);

    cipher.init(Cipher.DECRYPT_MODE, generateKey(password), new GCMParameterSpec(128, iv));

    return cipher.doFinal(cipherText);
  }

  public static byte[] decryptData(byte[] cipherText, String password) throws Exception {
    return decryptData(cipherText, password, false);
  }

  public static byte[] decryptDataWithNullOnException(
      byte[] cipherText, String password, boolean deprecatedAlg) {
    byte[] result = null;
    try {
      result = decryptData(cipherText, password, deprecatedAlg);
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
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  public static byte[] decryptDataWithNullOnException(byte[] cipherText, String password) {
    return decryptDataWithNullOnException(cipherText, password, false);
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
    } catch (InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    }

    return result;
  }
}
