package org.supla.android.widget.extended
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

import kotlinx.serialization.Serializable
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.images.ImageId
import org.supla.core.shared.data.model.general.SuplaFunction

@Serializable
data class ExtendedValueWidgetState(
  val icon: ImageId,
  val caption: String,
  val function: SuplaFunction,
  val value: WidgetValue,
  val updateTime: Long
)

@Serializable
sealed interface WidgetValue {

  @Serializable
  data class ElectricityMeter(
    val totalEnergy: Energy,
    val phases: Map<Phase, Energy>
  ) : WidgetValue {

    @Serializable
    data class Energy(
      val forwarded: String,
      val reversed: String?
    )
  }

  @Serializable
  data object Empty : WidgetValue

  @Serializable
  data object Unknown : WidgetValue
}
