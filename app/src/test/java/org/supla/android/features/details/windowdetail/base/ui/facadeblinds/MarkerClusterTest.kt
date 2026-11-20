package org.supla.android.features.details.windowdetail.base.ui.facadeblinds
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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindMarker

class MarkerClusterTest {

  @Test
  fun `should aggregate known values and ignore unknown values`() {
    val cluster = MarkerCluster()
    cluster.first(0f, ShadingBlindMarker(position = 20f, tiltValue = 25f))
    cluster.append(ShadingBlindMarker(position = 21f, tiltValue = null))
    cluster.append(ShadingBlindMarker(position = 22f, tiltValue = 85f))

    assertThat(cluster.percentageText).isEqualTo("25.0% - 85.0%")
    assertThat(cluster.count).isEqualTo(3)
  }

  @Test
  fun `should aggregate known angles and ignore markers without angles`() {
    val first = ShadingBlindMarker(position = 20f, tiltValue = 25f, tilt0Angle = 0f, tilt100Angle = 180f)
    val second = ShadingBlindMarker(position = 22f, tiltValue = 75f, tilt0Angle = 0f, tilt100Angle = 180f)
    val withoutAngles = ShadingBlindMarker(position = 21f, tiltValue = 50f)
    val cluster = MarkerCluster()
    cluster.first(0f, first)
    cluster.append(withoutAngles)
    cluster.append(second)

    assertThat(cluster.hasTilt).isTrue
    assertThat(cluster.minTilt).isEqualTo(first.tilt)
    assertThat(cluster.maxTilt).isEqualTo(second.tilt)
  }

  @Test
  fun `should show unknown when all merged values are unknown`() {
    val cluster = MarkerCluster()
    cluster.first(0f, ShadingBlindMarker(position = 20f, tiltValue = null))
    cluster.append(ShadingBlindMarker(position = 21f, tiltValue = null))

    assertThat(cluster.percentageText).isEqualTo("?")
    assertThat(cluster.count).isEqualTo(2)
  }
}
