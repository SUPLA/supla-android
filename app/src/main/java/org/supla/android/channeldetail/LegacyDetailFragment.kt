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

import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.os.Bundle
import org.supla.android.listview.DetailLayout
import org.supla.android.db.ChannelBase

class LegacyDetailFragment(private val innerView: DetailLayout): DetailFragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return innerView
    }

    override fun onStart() {
        super.onStart()

        val container = innerView.parent as ViewGroup
        val parms = FrameLayout.LayoutParams(container.width,
                                             container.height)
        innerView.layoutParams = parms
        innerView.setVisibility(View.VISIBLE)
        container.requestLayout()
        innerView.forceLayout()
    }

    override var channelBase: ChannelBase 
        get() = innerView.channelBase
        set(value) {
            innerView.setData(value)
        }

    override fun getChannelFromDatabase(): ChannelBase {
        return innerView.getChannelFromDatabase()
    }

    override fun getRemoteId(): Int {
        return innerView.getRemoteId()
    }

    override fun onBackPressed(): Boolean {
        return innerView.onBackPressed()
    }

    override fun detailWillHide(offline: Boolean): Boolean {
        return innerView.detailWillHide(offline)
    }

    override fun onDetailShow() {
        innerView.onDetailShow()
    }

    override fun onDetailHide() {
        innerView.onDetailHide()
    }

    override fun onChannelDataChanged() {
        innerView.OnChannelDataChanged()
    }
}
