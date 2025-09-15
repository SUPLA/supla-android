package org.supla.android.usecases.channel.valueformatter
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

import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatSpecification
import org.supla.core.shared.usecase.channel.valueformatter.formatters.GpmValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValuePrecision

fun GpmValueFormatter.Companion.staticFormatter(config: SuplaChannelConfig?): GpmValueFormatter {
  val gpmConfig = config as? SuplaChannelGeneralPurposeBaseConfig

  return GpmValueFormatter(
    defaultFormatSpecification = ValueFormatSpecification(
      precision = ValuePrecision.exact(gpmConfig?.valuePrecision ?: 2),
      unit = gpmConfig?.unitAfterValue?.let {
        if (it.isEmpty()) {
          null
        } else if (gpmConfig.noSpaceAfterValue) {
          it
        } else {
          " $it"
        }
      },
      predecessor = gpmConfig?.unitBeforeValue?.let {
        if (it.isEmpty()) {
          null
        } else if (gpmConfig.noSpaceBeforeValue) {
          it
        } else {
          "$it "
        }
      }
    )
  )
}
