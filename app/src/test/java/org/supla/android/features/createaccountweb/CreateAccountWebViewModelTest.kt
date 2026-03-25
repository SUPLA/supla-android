package org.supla.android.features.createaccountweb
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

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.core.BaseViewModelTest
import org.supla.android.tools.SuplaSchedulers

@RunWith(MockitoJUnitRunner::class)
class CreateAccountWebViewModelTest : BaseViewModelTest<CreateAccountWebViewState, CreateAccountWebViewEvent, CreateAccountWebViewModel>(
  mockSchedulers = MockSchedulers.NONE
) {

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: CreateAccountWebViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should load script when page is loaded`() {
    // given
    val url = "https://cloud.supla.org/register?lang=de"

    // when
    viewModel.urlLoaded(url)

    // then
    Assertions.assertThat(states).containsExactly(
      CreateAccountWebViewState(loading = false)
    )
    Assertions.assertThat(events).containsExactly(
      CreateAccountWebViewEvent.LoadRegistrationScript
    )
  }

  @Test
  fun `should not load script for other pages`() {
    // given
    val url = "https://cloud.supla.org/login"

    // when
    viewModel.urlLoaded(url)

    // then
    Assertions.assertThat(states).containsExactly(
      CreateAccountWebViewState(loading = false)
    )
    Assertions.assertThat(events).isEmpty()
  }
}
