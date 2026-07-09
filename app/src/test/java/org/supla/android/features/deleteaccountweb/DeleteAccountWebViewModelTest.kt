package org.supla.android.features.deleteaccountweb

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.tools.SuplaSchedulers

class DeleteAccountWebViewModelTest : BaseViewModelTest<DeleteAccountWebViewState, DeleteAccountWebViewEvent, DeleteAccountWebViewModel>(
  mockSchedulers = MockSchedulers.NONE
) {

  @MockK
  override lateinit var schedulers: SuplaSchedulers

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
