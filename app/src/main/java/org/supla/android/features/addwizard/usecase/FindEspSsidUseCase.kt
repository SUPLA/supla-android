package org.supla.android.features.addwizard.usecase
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

import org.supla.android.core.infrastructure.WiFiScanner
import org.supla.android.features.addwizard.model.Esp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindEspSsidUseCase @Inject constructor(
  private val wiFiScanner: WiFiScanner
) {
  suspend operator fun invoke(): Result =
    when (val ssids = wiFiScanner.scan()) {
      is WiFiScanner.Result.NotAllowed -> Result.Cached(ssids.cashed.filter { Esp.isKnownNetworkName(it) })
      is WiFiScanner.Result.Success -> {
        val filtered = ssids.ssids.filter { Esp.isKnownNetworkName(it) }
        when (filtered.size) {
          0 -> Result.Empty
          1 -> Result.Single(filtered.first())
          else -> Result.Multiple(filtered)
        }
      }
    }

  sealed interface Result {
    data object Empty : Result
    data class Single(val ssid: String) : Result
    data class Multiple(val ssids: List<String>) : Result
    data class Cached(val ssids: List<String>) : Result
  }
}
