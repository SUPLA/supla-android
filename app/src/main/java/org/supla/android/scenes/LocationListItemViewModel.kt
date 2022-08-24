package org.supla.android.scenes
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

import androidx.databinding.Bindable
import androidx.databinding.BaseObservable
import androidx.databinding.PropertyChangeRegistry
import org.supla.android.db.Location
import org.supla.android.data.source.local.LocationDao
import org.supla.android.BR

class LocationListItemViewModel(private val locationDao: LocationDao, 
                                private val location: Location): BaseObservable() {
    
    val locationName: String = location.caption
    private var _collapsed  = location.collapsed and 0x4 == 0x4
    var collapsed: Boolean 
        @Bindable get() = _collapsed
        set(value) {
            _collapsed = value
            if(value) {
                location.collapsed = (location.collapsed or 0x4)
            } else {
                location.collapsed = (location.collapsed and 0x4.inv())
            }
            locationDao.update(location)
            notifyPropertyChanged(BR.collapsed)
        }
}
