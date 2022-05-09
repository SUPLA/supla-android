package org.supla.android.cfg

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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.supla.android.SuplaApp

class ProfilesViewModelFactory: ViewModelProvider.Factory {

        override fun <T: ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ProfilesViewModel::class.java)) {
                val pm = SuplaApp.getApp().profileManager
                return ProfilesViewModel(pm) as T
            } else {
                throw IllegalArgumentException("unknown view model class")
            }
        }
}
