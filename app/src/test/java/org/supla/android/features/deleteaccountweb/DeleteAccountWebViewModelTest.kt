package org.supla.android.features.deleteaccountweb
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

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.tools.SuplaThreading

class DeleteAccountWebViewModelTest : BaseViewModelTest<DeleteAccountWebViewState, DeleteAccountWebViewEvent, DeleteAccountWebViewModel>(
  mockSchedulers = MockSchedulers.NONE
) {

  @MockK
  override lateinit var threading: SuplaThreading

  @InjectMockKs
  override lateinit var viewModel: DeleteAccountWebViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should replace server address using user address`() {
    // given
    val url = "https://{SERVER_ADDRESS}/test"
    val server = "test.supla.org"

    // when
    val result = viewModel.getUrl(url, server)

    // then
    assertThat(result).isEqualTo("https://test.supla.org/test")
  }

  @Test
  fun `should replace server address using default address`() {
    // given
    val url = "https://{SERVER_ADDRESS}/test"

    // when
    val resultNull = viewModel.getUrl(url, null)
    val resultEmpty = viewModel.getUrl(url, "")

    // then
    assertThat(resultNull).isEqualTo("https://cloud.supla.org/test")
    assertThat(resultEmpty).isEqualTo("https://cloud.supla.org/test")
  }

  @Test
  fun `should close when close button clicked`() {
    // given
    val closeUrl = "https://cloud.supla.org/dfgdsfdsf?lang=de&ack=true"

    // when
    viewModel.urlLoaded(closeUrl)

    // then
    assertThat(states).containsExactly(
      DeleteAccountWebViewState(loading = false)
    )
    assertThat(events).containsExactly(
      DeleteAccountWebViewEvent.CloseClicked
    )
  }

  @Test
  fun `should skip other clicked urls`() {
    // given
    val closeUrl = "https://cloud.supla.org/dfsdfsvsd"

    // when
    viewModel.urlLoaded(closeUrl)

    // then
    assertThat(states).containsExactly(
      DeleteAccountWebViewState(loading = false)
    )
    assertThat(events).isEmpty()
  }
}
