package org.supla.android.ui
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
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout


class SegmentedButtonGroup @JvmOverloads constructor(
        ctx: Context, attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
): LinearLayout(ctx, attrs, defStyleAttr) {

    private var _idx = 0
    private var _listener: ((Int)->Unit)? = null

    var position: Int
        get() = _idx
        set(value) {
            if(_idx == value) {
                return
            }
            _idx = value

            for(i in 0..getChildCount()-1) {
                val v = getChildAt(i)
                v.setSelected(_idx == i)
            }

            _listener?.invoke(_idx)
        }
    
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        child.setClickable(true)
        val pos = if(index >= 0) index else getChildCount()-1
        child.setSelected(pos == _idx)
        child.setOnClickListener() {
            position = pos
        }
    }

    fun setOnPositionChangedListener(listener: ((Int)->Unit)?) {
        _listener = listener
    }
}
