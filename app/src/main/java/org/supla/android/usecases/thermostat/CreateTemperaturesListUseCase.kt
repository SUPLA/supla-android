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

import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import org.supla.android.R
import org.supla.android.data.ValuesFormatter
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.extensions.valuesFormatter
import org.supla.android.features.thermostatdetail.thermostatgeneral.MeasurementValue
import org.supla.android.images.ImageCache
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.channel.ChannelWithChildren
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateTemperaturesListUseCase @Inject constructor() {

  operator fun invoke(channelWithChildren: ChannelWithChildren): List<MeasurementValue> =
    mutableListOf<MeasurementValue>().apply {
      val sortedChildren = channelWithChildren.children
        .filter { it.relationType.isThermometer() }
        .sortedBy { item -> item.relationType.value }

      if (sortedChildren.none { it.relationType.isMainThermometer() }) {
        add(
          MeasurementValue(
            remoteId = -1,
            iconProvider = { context -> ResourcesCompat.getDrawable(context.resources, R.drawable.ic_unknown_channel, null)!!.toBitmap() },
            valueStringProvider = { ValuesFormatter.NO_VALUE_TEXT }
          )
        )
      }

      for (child in sortedChildren) {
        add(child.channel.toTemperatureValue())
        if (child.channel.func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
          add(child.channel.toHumidityValue())
        }
      }
    }
}

private fun Channel.toTemperatureValue(): MeasurementValue =
  MeasurementValue(
    remoteId = remoteId,
    iconProvider = { ImageCache.getBitmap(it, imageIdx) },
    valueStringProvider = {
      if (value.onLine) {
        it.valuesFormatter.getTemperatureString(value.getTemp(func))
      } else {
        ValuesFormatter.NO_VALUE_TEXT
      }
    }
  )

private fun Channel.toHumidityValue(): MeasurementValue =
  MeasurementValue(
    remoteId = remoteId,
    iconProvider = { ImageCache.getBitmap(it, getImageIdx(ChannelBase.WhichOne.Second)) },
    valueStringProvider = {
      if (value.onLine) {
        it.valuesFormatter.getHumidityString(value.humidity)
      } else {
        ValuesFormatter.NO_VALUE_TEXT
      }
    }
  )
