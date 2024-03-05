package org.supla.android.ui.views.buttons.animatable.upanddown
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun UpDownControlButtonScope(content: @Composable UpDownControlButtonScope.() -> Unit) {
  val scope by remember { mutableStateOf(UpDownControlButtonScope()) }
  content(scope)
}

class UpDownControlButtonScope {
  fun handleEvents(
    onClick: (() -> Unit)? = null,
    onTouchDown: (() -> Unit)? = null,
    onTouchUp: (() -> Unit)? = null
  ) = object : OnEventHandler {
    override fun onClick() = onClick

    override fun onTouchDown() = onTouchDown

    override fun onTouchUp() = onTouchUp
  }

  interface OnEventHandler {
    fun onClick(): (() -> Unit)?
    fun onTouchDown(): (() -> Unit)?
    fun onTouchUp(): (() -> Unit)?
  }
}
