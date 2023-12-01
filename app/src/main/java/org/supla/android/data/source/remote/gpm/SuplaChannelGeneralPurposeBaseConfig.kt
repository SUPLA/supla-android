package org.supla.android.data.source.remote.gpm

import org.supla.android.data.source.remote.SuplaChannelConfig

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
