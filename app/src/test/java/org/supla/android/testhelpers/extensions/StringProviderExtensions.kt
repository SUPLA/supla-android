package org.supla.android.testhelpers.extensions
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

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.supla.android.core.ui.StringProvider

fun StringProvider.extractResId(): Int {
  val resIdSlot = slot<Int>()
  val context: Context = mockk {
    every { getString(capture(resIdSlot)) } returns ""
  }

  this(context)

  return resIdSlot.captured
}

fun StringProvider.extract(): List<Int> {
  val resIds = mutableListOf<Int>()
  val context: Context = mockk {
    every { getString(any(), any()) } answers {
      resIds.add(firstArg())
      ""
    }
    every { getString(any()) } answers {
      resIds.add(firstArg())
      ""
    }
  }

  this(context)

  return resIds
}
