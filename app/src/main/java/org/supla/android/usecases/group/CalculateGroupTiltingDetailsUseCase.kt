package org.supla.android.usecases.group
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
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType
import javax.inject.Inject
import javax.inject.Singleton

sealed interface TiltingDetails {
  val tilt0Angle: Int?
  val tilt100Angle: Int?
  val tiltControlType: SuplaTiltControlType?

  data class Similar(
    override val tilt0Angle: Int,
    override val tilt100Angle: Int,
    override val tiltControlType: SuplaTiltControlType
  ) : TiltingDetails

  object Different : TiltingDetails {
    override val tilt0Angle: Int? = null
    override val tilt100Angle: Int? = null
    override val tiltControlType: SuplaTiltControlType? = null
  }

  object Unknown : TiltingDetails {
    override val tilt0Angle: Int? = null
    override val tilt100Angle: Int? = null
    override val tiltControlType: SuplaTiltControlType? = null
  }
}

@Singleton
class CalculateGroupTiltingDetailsUseCase @Inject constructor() {

  operator fun invoke(configs: List<SuplaChannelConfig?>): TiltingDetails {
    return configs.fold(null as TiltingDetails?) { acc, config ->
      val details = config?.toTiltingDetails()

      if (acc == TiltingDetails.Unknown || details == null) {
        TiltingDetails.Unknown
      } else if (acc == TiltingDetails.Different) {
        TiltingDetails.Different
      } else if (acc == null) {
        details
      } else if (acc != details) {
        TiltingDetails.Different
      } else {
        details
      }
    } ?: TiltingDetails.Unknown
  }

  private fun SuplaChannelConfig.toTiltingDetails(): TiltingDetails? =
    (this as? SuplaChannelFacadeBlindConfig)?.let { config ->
      TiltingDetails.Similar(config.tilt0Angle, config.tilt100Angle, config.type)
    }
}
