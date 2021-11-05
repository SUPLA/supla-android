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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import android.view.Gravity
import android.os.Build
import android.content.res.TypedArray

import org.supla.android.SuplaApp
import org.supla.android.R

class SegmentedButton @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(ctx, attrs, defStyleAttr) {
    
    private val elevation: Float

    init {
        val attrsArray = intArrayOf(android.R.attr.src,
                                    android.R.attr.text);
        val ta = ctx.obtainStyledAttributes(attrs, attrsArray)
        val drawable = ta.getDrawable(0)
        val innerView: View
        if(drawable != null) {
            val imgView = ImageView(ctx, attrs, defStyleAttr)
            innerView = imgView
        } else {
            val txtView = TextView(ctx, attrs, defStyleAttr)
            txtView.setTypeface(SuplaApp.getApp().getTypefaceOpenSansRegular())
            txtView.setText(ta.getString(1))
            txtView.setGravity(Gravity.CENTER)
            innerView = txtView
        }

        val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                          FrameLayout.LayoutParams.MATCH_PARENT,
                                          Gravity.CENTER)
        addView(innerView, 0, lp)

        elevation = ctx.getResources().getDimensionPixelSize(R.dimen.segmented_button_elevation).toFloat()
    }

    override fun setSelected(sel: Boolean) {
        super.setSelected(sel)

        if(Build.VERSION.SDK_INT >= 21) {
            setElevation(if(sel) elevation else 0f)
        }
    }
}
