package org.supla.android.core.branding

import org.supla.android.R
import org.supla.android.ui.AppBar

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

object Configuration {
  object Menu {
    const val DEVICES_OPTION_VISIBLE = true
    const val Z_WAVE_OPTION_VISIBLE = true
    const val ABOUT_OPTION_VISIBLE = true
    const val HELP_OPTION_VISIBLE = true
  }

  const val ASK_FOR_RATE = true
  const val SHOW_LICENCE = true

  object Toolbar {
    val LOGO_INSTEAD_OFF_APP_NAME: AppBar.Title? = null
  }

  object About {
    val LOGO_RESOURCE = R.drawable.logo_light
    val COLOR_FILLER: Int? by lazy { R.color.on_background }
  }

  object Status {
    val LOGO_RESOURCE = R.drawable.logo_light
    val COLOR_FILLER: Int? by lazy { R.color.primary }
  }

  object LockScreen {
    val LOGO_RESOURCE = R.drawable.logo_with_name
  }

  object ProjectorScreen {
    val LOGO_RESOURCE = R.drawable.logo
    val COLOR_FILLER: Int? by lazy { R.color.primary }
    const val LOGO_WIDTH = 120f
    const val LOGO_HEIGHT = 137f
  }
}
