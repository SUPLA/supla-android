package org.supla.android.usecases.channel.stringvalueprovider
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

import com.google.gson.Gson
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.extensions.guardLet
import org.supla.android.usecases.channel.ChannelValueStringProvider
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.channel.valueformatter.GpmValueFormatter
import org.supla.android.usecases.channel.valueprovider.GpmValueProvider
import org.supla.core.shared.data.model.general.SuplaFunction
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GpmValueStringProvider @Inject constructor(
  private val gpmValueProvider: GpmValueProvider,
  @Named(GSON_FOR_REPO) private val gson: Gson
) : ChannelValueStringProvider {

  override fun handle(channelWithChildren: ChannelWithChildren): Boolean =
    channelWithChildren.channel.function == SuplaFunction.GENERAL_PURPOSE_MEASUREMENT ||
      channelWithChildren.channel.function == SuplaFunction.GENERAL_PURPOSE_METER

  override fun value(channelWithChildren: ChannelWithChildren, valueType: ValueType, withUnit: Boolean): String {
    val value = gpmValueProvider.value(channelWithChildren, valueType)
    if (value.isNaN()) {
      return ValuesFormatter.NO_VALUE_TEXT
    }

    val (config) = guardLet(channelWithChildren.channel.configEntity?.toSuplaConfig(gson) as? SuplaChannelGeneralPurposeBaseConfig) {
      return String.format(Locale.getDefault(), "%.0f", value)
    }

    val formatter = GpmValueFormatter(config)
    return formatter.format(value, withUnit = withUnit)
  }
}
