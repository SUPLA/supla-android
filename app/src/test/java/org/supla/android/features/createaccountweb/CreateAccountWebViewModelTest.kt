package org.supla.android.features.createaccountweb

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
