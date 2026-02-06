package org.supla.android.features.nfc.call.screens
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

import android.app.Activity
import android.content.Context
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import org.supla.android.features.nfc.call.EditMissingAction
import org.supla.android.features.nfc.call.SaveNewNfcTag
import javax.inject.Inject

@ActivityScoped
class Navigator @Inject constructor(@param:ActivityContext private val activityContext: Context) {
  private val backStack = MutableStateFlow<NavBackStack<NavKey>?>(null)

  fun finish() = (activityContext as? Activity)?.finish()

  fun navigateToEditMissingAction(id: Long) {
    this.backStack.value?.removeLastOrNull()
    this.backStack.value?.add(EditMissingAction(id))
  }

  fun navigateToSaveNewNfcTag(uuid: String, readOnly: Boolean) {
    this.backStack.value?.removeLastOrNull()
    this.backStack.value?.add(SaveNewNfcTag(uuid, readOnly))
  }

  fun bind(backStack: NavBackStack<NavKey>) {
    this.backStack.value = backStack
  }

  fun unbind(backStack: NavBackStack<NavKey>) {
    if (this.backStack.value == backStack) {
      this.backStack.value = null
    }
  }
}
