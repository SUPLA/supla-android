package org.supla.android.usecases.thermostat
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

import org.supla.android.features.thermostatdetail.thermostatgeneral.ThermostatTemperature
import org.supla.android.images.ImageCache
import org.supla.android.usecases.channel.ChannelWithChildren
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateTemperaturesListUseCase @Inject constructor() {

  operator fun invoke(channelWithChildren: ChannelWithChildren): List<ThermostatTemperature> =
    mutableListOf<ThermostatTemperature>().apply {
      val sortedChildren = channelWithChildren.children
        .filter { it.relationType.isThermometer() }
        .sortedBy { item -> item.relationType.value }

      for (child in sortedChildren) {
        add(
          ThermostatTemperature(
            thermometerRemoteId = child.channel.remoteId,
            iconProvider = { ImageCache.getBitmap(it, child.channel.imageIdx) },
            temperature = child.channel.humanReadableValue.toString()
          )
        )
      }
    }
}
