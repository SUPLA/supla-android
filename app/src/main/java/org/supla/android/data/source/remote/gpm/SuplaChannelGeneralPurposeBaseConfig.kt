package org.supla.android.data.source.remote.gpm
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
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.ValuePrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.custom

open class SuplaChannelGeneralPurposeBaseConfig(
  @Transient override val remoteId: Int,
  @Transient override val func: Int?,
  @Transient override val crc32: Long,
  @Transient open val valueDivider: Int,
  @Transient open val valueMultiplier: Int,
  @Transient open val valueAdded: Long,
  @Transient open val valuePrecision: Int,
  @Transient open val unitBeforeValue: String,
  @Transient open val unitAfterValue: String,
  @Transient open val noSpaceBeforeValue: Boolean,
  @Transient open val noSpaceAfterValue: Boolean,
  @Transient open val keepHistory: Boolean,
  @Transient open val defaultValueDivider: Int,
  @Transient open val defaultValueMultiplier: Int,
  @Transient open val defaultValueAdded: Long,
  @Transient open val defaultValuePrecision: Int,
  @Transient open val defaultUnitBeforeValue: String,
  @Transient open val defaultUnitAfterValue: String,
  @Transient open val refreshIntervalMs: Int
) : SuplaChannelConfig(remoteId, func, crc32)

fun SuplaChannelGeneralPurposeBaseConfig?.toValueFormat(withUnit: Boolean = false): ValueFormat =
  ValueFormat(
    withUnit = withUnit,
    precision = custom(ValuePrecision.exact(this?.valuePrecision ?: 2)),
    customUnit = this?.unitAfterValue?.let { if (noSpaceAfterValue) it else " $it" },
    predecessor = this?.unitBeforeValue?.let { if (noSpaceBeforeValue) it else "$it " }
  )
