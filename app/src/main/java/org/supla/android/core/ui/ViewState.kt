package org.supla.android.core.ui
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
import org.supla.android.events.LoadingTimeoutManager

open class ViewState

open class LoadableViewState(
  open val loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState()
) : ViewState()

typealias StringProvider = (context: Context) -> String

fun stringProviderOf(string: String): StringProvider = { string }
fun stringProviderOf(resourceId: Int): StringProvider = { it.getString(resourceId) }
fun stringProvider(provider: (context: Context) -> String): StringProvider {
  return { context -> provider(context) }
}

fun StringProvider?.valueOrEmpty(): StringProvider = this ?: { "" }
fun StringProvider?.valueOrEmpty(context: Context) = this?.let { it(context) } ?: ""
