package org.supla.android;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

public class EncryptionTest {

    @Test
    public void testPasswordAlignment() {
        String sourceText = "ABCD";

        // Pasword is aligned to 32 characters
        byte[] encrypted = Encryption.encryptDataWithNullOnException(sourceText.getBytes(), "X");
        byte[] decrypted = Encryption.decryptDataWithNullOnException(encrypted, "X");

        assertArrayEquals(sourceText.getBytes(), decrypted);

        decrypted = Encryption.decryptDataWithNullOnException(encrypted, "Y");
        assertNull(decrypted);

        decrypted = Encryption.decryptDataWithNullOnException(encrypted,
                "X000000000000000000000000000000Y");
        assertNull(decrypted);

        decrypted = Encryption.decryptDataWithNullOnException(encrypted,
                "X0000000000000000000000000000000Y");

        assertArrayEquals(sourceText.getBytes(), decrypted);
    }
}