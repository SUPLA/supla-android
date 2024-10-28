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
import androidx.annotation.StringRes
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
fun String.localized(): LocalizedString = LocalizedString.Constant(this)

sealed interface LocalizedString {
  operator fun invoke(context: Context): String
  fun provider(): StringProvider = { context -> this(context) }

  data object Empty : LocalizedString {
    override fun invoke(context: Context): String = ""
  }

  data class Constant(private val string: String) : LocalizedString {
    override fun invoke(context: Context): String = string
  }
}

private data class LocalizedStringIdOnly(private val stringRes: Int) : LocalizedString {
  override fun invoke(context: Context): String = context.getString(stringRes)
}

private data class LocalizedStringDsd(
  private val stringRes: Int,
  private val arg1: Int,
  private val arg2: LocalizedString,
  private val arg3: Int
) : LocalizedString {
  override fun invoke(context: Context): String = context.getString(stringRes, arg1, arg2(context).trim(), arg3)
}

fun localizedString(@StringRes stringRes: Int?): LocalizedString = stringRes?.let { LocalizedStringIdOnly(it) } ?: LocalizedString.Empty
fun localizedString(@StringRes stringRes: Int, arg1: Int, arg2: LocalizedString, arg3: Int): LocalizedString =
  LocalizedStringDsd(stringRes, arg1, arg2, arg3)
