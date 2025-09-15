package org.supla.core.shared.infrastructure.logging
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

import android.util.Log

actual object Logger {
  actual fun d(tag: String, message: String, throwable: Throwable?) {
    Log.d(tag, message, throwable)
  }

  actual fun i(tag: String, message: String, throwable: Throwable?) {
    Log.i(tag, message, throwable)
  }

  actual fun w(tag: String, message: String, throwable: Throwable?) {
    Log.w(tag, message, throwable)
  }

  actual fun e(tag: String, message: String, throwable: Throwable?) {
    Log.e(tag, message, throwable)
  }
}
