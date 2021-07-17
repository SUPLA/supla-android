package org.supla.android.db

import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

class ChannelTest: TestCase() {

    @Test
    fun testTemperatureCelsius() {
        val ch = mock(Channel::class.java)
        `when`(ch.temp).thenReturn(23.0)

        assertEquals(23.0, ch.temp)
        assertEquals("23℃", ch.humanReadableValue, "value not matched " + ch.humanReadableValue)
    }
}
