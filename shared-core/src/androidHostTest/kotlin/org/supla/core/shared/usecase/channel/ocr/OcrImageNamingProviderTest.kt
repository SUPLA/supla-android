package org.supla.core.shared.usecase.channel.ocr
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

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class OcrImageNamingProviderTest {

  @InjectMockKs
  private lateinit var provider: OcrImageNamingProvider

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get directory name`() {
    assertThat(provider.directory).isEqualTo("ocr_photos")
  }

  @Test
  fun `should get image name`() {
    // when
    val name = provider.imageName(1, 2)

    // then
    assertThat(name).isEqualTo("1_2.jpg")
  }

  @Test
  fun `should get cropped image name`() {
    // when
    val name = provider.imageCroppedName(1, 2)

    // then
    assertThat(name).isEqualTo("1_2_cropped.jpg")
  }
}
