package org.supla.android.data.model
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

import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.functions.Supplier
import java.util.Objects

class Optional<T : Any>(private val value: T? = null) {

  fun get(): T {
    if (value == null) {
      throw NoSuchElementException("No value present")
    }
    return value
  }

  val isPresent: Boolean
    get() = value != null

  val isEmpty: Boolean
    get() = value == null

  fun ifPresent(action: Consumer<in T>) {
    if (value != null) {
      action.accept(value)
    }
  }

  fun orElse(other: T): T {
    return value ?: other
  }

  fun orElseGet(supplier: Supplier<out T>): T {
    return value ?: supplier.get()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other !is Optional<*>) {
      return false
    }
    return value == other.value
  }

  override fun hashCode(): Int {
    return Objects.hashCode(value)
  }

  override fun toString(): String {
    return if (value != null) String.format("Optional[%s]", value) else "Optional.empty"
  }

  companion object {

    private val EMPTY: Optional<*> = Optional<Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> empty(): Optional<T> {
      return EMPTY as Optional<T>
    }

    fun <T : Any> of(value: T): Optional<T> {
      return Optional(value)
    }

    fun <T : Any> ofNullable(value: T?): Optional<T> {
      return if (value == null) empty() else of(value)
    }
  }
}
