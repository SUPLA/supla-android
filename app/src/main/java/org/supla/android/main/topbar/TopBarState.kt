package org.supla.android.main.topbar
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

import org.supla.core.shared.infrastructure.LocalizedString
import kotlin.collections.set
import kotlin.reflect.KClass

data class TopBarState(
  val title: LocalizedString = LocalizedString.Empty,
  val search: TopBarSearchState? = null,
  val action: TopBarAction? = null
)

data class TopBarSearchState(
  val query: String,
  val onQueryChange: (String) -> Unit
)

data class TopBarAction(
  val icon: TopBarIcon,
  val handlers: Map<KClass<out TopBarEvent>, () -> Unit> = emptyMap()
)

fun topBarAction(icon: TopBarIcon, handler: () -> Unit) =
  TopBarAction(icon, mapOf(icon.event::class to handler))

fun topBarAction(callback: TopBarActionBuilder.() -> Unit): TopBarAction {
  val builder = TopBarActionBuilder()
  callback.invoke(builder)

  return TopBarAction(icon = builder.icon!!, handlers = builder.handlers)
}

class TopBarActionBuilder {
  var icon: TopBarIcon? = null
  val handlers: MutableMap<KClass<out TopBarEvent>, () -> Unit> = mutableMapOf()

  inline fun <reified T : TopBarEvent> handle(noinline handler: () -> Unit) {
    handlers[T::class] = handler
  }
}
