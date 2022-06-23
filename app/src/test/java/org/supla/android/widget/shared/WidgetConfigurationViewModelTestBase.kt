package org.supla.android.widget.shared

import android.database.Cursor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.supla.android.db.SuplaContract
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.testhelpers.toByteArray

open class WidgetConfigurationViewModelTestBase {
    protected fun mockCursorChannels(vararg mockedFunctions: Int): Cursor {
        val cursor: Cursor = mock {
            on { moveToFirst() } doReturn true
            on { getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_FUNC) } doReturn 123
        }
        if (mockedFunctions.size > 1) {
            val channelFncs = Array(mockedFunctions.size - 1) { 0 }
            for (i in 1 until mockedFunctions.size) {
                channelFncs[i - 1] = mockedFunctions[i]
            }
            whenever(cursor.getInt(123)).thenReturn(mockedFunctions[0], *channelFncs)

            val moveToNexts = Array(mockedFunctions.size - 1) { true }
            moveToNexts[moveToNexts.size - 1] = false
            whenever(cursor.moveToNext()).thenReturn(true, *moveToNexts)
        } else if (mockedFunctions.size == 1) {
            whenever(cursor.getInt(123)).thenReturn(mockedFunctions[0])
            whenever(cursor.moveToNext()).thenReturn(false)
        }

        whenever(cursor.getColumnIndex(SuplaContract.ChannelExtendedValueEntry.COLUMN_NAME_VALUE)).thenReturn(123)
        whenever(cursor.getBlob(123)).thenReturn(SuplaChannelExtendedValue().toByteArray())

        return cursor
    }
}