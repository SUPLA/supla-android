package org.supla.android.testhelpers

import org.supla.android.lib.SuplaChannelExtendedValue
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

fun SuplaChannelExtendedValue.toByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
    objectOutputStream.writeObject(this)
    objectOutputStream.flush()
    val result = byteArrayOutputStream.toByteArray()
    byteArrayOutputStream.close()
    objectOutputStream.close()
    return result
}