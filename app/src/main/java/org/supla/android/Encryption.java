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
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.GCMParameterSpec;

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

	private final static String ENCRYPTION_ALG = "AES/GCM/NoPadding";
	private final static int GCM_IV_LENGTH = 12;
	private final static int GCM_TAG_LENGTH = 16;

    private static byte[] IV = null;

	private static Cipher getCipher(String type, int opmode, SecretKey key)
		throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
			   IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeySpecException {
		Cipher cipher;
		if(ENCRYPTION_ALG.equals(type)) {
            if(IV == null) {
                Preferences prefs = null;
                try {
                    prefs = new Preferences(SuplaApp.getApp());
                } catch(NullPointerException e) {
                    // Ignore NPE on Preferences construction,
                    // primarily to avoid mocking within
                    // singleton pattern for unit tests.
                }
                if(prefs != null) {
                    IV = prefs.getIV();
                }
                
                if(IV == null) {
                    IV = new byte[GCM_IV_LENGTH];
                    SecureRandom rnd = new SecureRandom();
                    rnd.nextBytes(IV);
                    if(prefs != null) {
                        prefs.setIV(IV);
                    }
                }
            }
			cipher = Cipher.getInstance(type);
			GCMParameterSpec parm = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);
			cipher.init(opmode, key, parm);
		} else {
			cipher = Cipher.getInstance(type);
			cipher.init(opmode, key);
		}

		return cipher;
	}

    public static byte[] encryptData(byte[] data, String password)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException,
            UnsupportedEncodingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        /* Encrypt the message. */
        Cipher cipher = Encryption.getCipher(Encryption.ENCRYPTION_ALG, Cipher.ENCRYPT_MODE,
								  generateKey(password));
        byte[] cipherText = cipher.doFinal(data);
        return cipherText;
    }

    public static byte[] decryptData(byte[] cipherText, String password)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, UnsupportedEncodingException, InvalidKeySpecException,
            InvalidAlgorithmParameterException {
	    String[] algorithms = { ENCRYPTION_ALG, "AES/ECB/PKCS5Padding" /*legacy*/ };
	    for(int i = 0; i < algorithms.length; i++) {
            Cipher cipher = getCipher(algorithms[i], Cipher.DECRYPT_MODE,
                    generateKey(password));
            try {
                return cipher.doFinal(cipherText);
            } catch (Exception e) {
                if (i > 0) throw e;
                /* Otherwise continue to legacy algorithm */
            }
        }
        return null;
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
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return result;
    }

}
