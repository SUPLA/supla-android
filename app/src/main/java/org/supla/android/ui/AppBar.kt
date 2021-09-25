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
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Gravity
import androidx.databinding.DataBindingUtil
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.Toolbar.LayoutParams
import org.supla.android.databinding.AppBarBinding
import org.supla.android.R

class AppBar @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Toolbar(ctx, attrs, defStyleAttr) {


    private lateinit var binding: AppBarBinding;

    init {
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DataBindingUtil.inflate(inflater, R.layout.app_bar, this,
                                          false)
        val params = LayoutParams(Gravity.CENTER_HORIZONTAL)
        addView(binding.root, params)
    }

    override fun setTitle(title: CharSequence) {
        binding.title.text = title
    }
}
