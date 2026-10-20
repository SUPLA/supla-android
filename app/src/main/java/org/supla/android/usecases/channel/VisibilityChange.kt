package org.supla.android.usecases.channel
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

private const val VALUE_HIDDEN = 0
private const val VALUE_VISIBLE = 1
private const val VALUE_VISIBLE_PROCESSING = 2

enum class VisibilityChange(val newVisibility: Int, val applyForVisibility: Int) {
  PROCESSING_TO_HIDE(VALUE_HIDDEN, VALUE_VISIBLE_PROCESSING),
  VISIBLE_TO_PROCESSING(VALUE_VISIBLE_PROCESSING, VALUE_VISIBLE)
}
