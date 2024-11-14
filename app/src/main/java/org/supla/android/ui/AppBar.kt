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
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.unit.Dp
import org.supla.android.R
import org.supla.android.core.branding.Configuration
import org.supla.android.extensions.toPx
import org.supla.android.extensions.visibleIf

class AppBar @JvmOverloads constructor(
  ctx: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : Toolbar(ctx, attrs, defStyleAttr) {

  private val toolbarTitle: TextView by lazy { findViewById(R.id.supla_toolbar_title) }
  private val toolbarIcon: AppCompatImageView by lazy { findViewById(R.id.supla_toolbar_icon) }

  override fun setTitle(title: CharSequence) {
    val iconLogo = Configuration.Toolbar.LOGO_INSTEAD_OFF_APP_NAME
    if (iconLogo != null && title == context.getString(R.string.app_name)) {
      setTitle(iconLogo)
    } else {
      setTitle(Title.Text(title.toString()))
    }
  }

  override fun setSubtitle(subtitle: CharSequence?) {
    subtitle?.let { setTitle(it) }
  }

  fun setTitle(title: Title) {
    toolbarTitle.visibleIf(title is Title.Text)
    toolbarIcon.visibleIf(title is Title.Icon)

    when (title) {
      is Title.Text ->
        toolbarTitle.text = title.text
      is Title.Icon -> {
        toolbarIcon.setImageResource(title.iconRes)
        with(toolbarIcon.layoutParams) {
          width = title.width.toPx().toInt()
          height = title.height.toPx().toInt()
        }
      }
    }
  }

  sealed interface Title {
    data class Text(val text: String) : Title
    data class Icon(
      val iconRes: Int,
      val width: Dp,
      val height: Dp
    ) : Title
  }
}
