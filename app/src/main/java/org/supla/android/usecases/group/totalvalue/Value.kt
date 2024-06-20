package org.supla.android.usecases.group.totalvalue
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

sealed class Value<T> {
  abstract val value: T
  abstract override fun toString(): String
}

class IntegerValue(override val value: Int) : Value<Int>() {
  override fun toString(): String = value.toString()
}

class FloatValue(override val value: Float) : Value<Float>() {
  override fun toString(): String = value.toString()
}

class BooleanValue(override val value: Boolean) : Value<Boolean>() {
  override fun toString(): String = if (value) "1" else "0" // Backwards compatibility with old code
}
