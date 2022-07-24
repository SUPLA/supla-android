package org.supla.android.channeldetail

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


import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView

import org.supla.android.db.ChannelBase


class DetailContainer(val channelBase: ChannelBase,
                      private val fragmentContainer: FragmentContainerView,
                      private val detailFragment: DetailFragment) {

    fun getChannelFromDatabase(): ChannelBase {
        return detailFragment.getChannelFromDatabase()
    }

    fun getRemoteId(): Int {
        return detailFragment.getRemoteId()
    }

    fun matches(cbase: ChannelBase): Boolean {
        return cbase.getId() == channelBase.getId()
    }

    fun onBackPressed(): Boolean {
        return getFragment().onBackPressed()
    }

    fun detailWillHide(offline: Boolean): Boolean {
        return getFragment().detailWillHide(offline)
    }

    fun onDetailShow() {
        getFragment().onDetailShow()
    }

    fun onDetailHide() {
        getFragment().onDetailHide()
    }

    fun getFragment(): DetailFragment = detailFragment

    fun getContainerView(): FragmentContainerView = fragmentContainer

    fun setMargin(margin: Int) {
        val params = fragmentContainer.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(margin, 0, -margin, 0)
        fragmentContainer.layoutParams = params
    }

    fun onChannelDataChanged() {
        getFragment().onChannelDataChanged()
    }
}
