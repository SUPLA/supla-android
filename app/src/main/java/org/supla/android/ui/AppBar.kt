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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.databinding.AppBarBinding

class AppBar @JvmOverloads constructor(
  ctx: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : Toolbar(ctx, attrs, defStyleAttr) {

  private val binding: AppBarBinding

  init {
    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    binding = DataBindingUtil.inflate(
      inflater,
      R.layout.app_bar,
      this,
      false
    )
    val params = LayoutParams(Gravity.CENTER_HORIZONTAL)

    arrayOf(binding.title, binding.subtitle).forEach {
      it.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular())
    }
    addView(binding.root, params)
  }

  override fun setTitle(title: CharSequence) {
    binding.title.text = title
    binding.title.visibility = View.VISIBLE
    binding.subtitle.visibility = View.INVISIBLE
  }

  override fun setSubtitle(subtitle: CharSequence) {
    binding.subtitle.visibility = View.VISIBLE
    binding.title.visibility = View.INVISIBLE
    binding.subtitle.text = subtitle
  }
}
