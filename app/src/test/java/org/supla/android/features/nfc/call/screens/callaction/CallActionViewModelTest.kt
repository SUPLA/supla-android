package org.supla.android.features.nfc.call.screens.callaction
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

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.MainDispatcherRule
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.infrastructure.NativeLoader
import org.supla.android.core.infrastructure.UriProxy
import org.supla.android.core.infrastructure.suplaclient.SingleCallProvider
import org.supla.android.data.source.NfcCallRepository
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.local.entity.NfcCallResult
import org.supla.android.data.source.local.entity.NfcTagEntity
import org.supla.android.data.source.local.entity.complex.NfcTagDataEntity
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.tools.SuplaSchedulers
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase

class CallActionViewModelTest :
  BaseViewModelTest<CallActionScreenState, CallActionViewEvent, CallActionViewModel>(MockSchedulers.MOCKK) {

  @get:Rule
  override val mainDispatcherRule = MainDispatcherRule()

  @MockK
  private lateinit var singleCallProvider: SingleCallProvider

  @MockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @MockK
  private lateinit var nfcCallRepository: NfcCallRepository

  @MockK
  private lateinit var nfcTagRepository: NfcTagRepository

  @MockK
  private lateinit var uriProxy: UriProxy

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: CallActionViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should set illegal intent error when url is missing`() {
    // when
    viewModel.onLaunchWithUrl(null, false)

    // then
    assertThat(states).containsExactly(
      CallActionScreenState(
        step = TagProcessingStep.Failure(TagProcessingStep.FailureType.IllegalIntent)
      )
    )
  }

  @Test
  fun `should set unknown error when url is wrong`() {
    // when
    viewModel.onLaunchWithUrl("https://google.com", false)

    // then
    assertThat(states).containsExactly(
      CallActionScreenState(
        step = TagProcessingStep.Failure(TagProcessingStep.FailureType.UnknownUrl)
      )
    )
  }

  @Test
  fun `should set tag not found when there is no tag id available`() {
    // given
    val tagUuid = "123"
    val url = "https://supla.org/tag/$tagUuid"
    val uri: Uri = mockk {
      every { host } returns "supla.org"
      every { pathSegments } returns listOf("tag", tagUuid)
    }

    every { uriProxy.toUri(url) } returns uri
    every { dateProvider.currentTimestamp() } returns 1L
    coEvery { nfcTagRepository.findByUuidWithDependencies(tagUuid) } returns null

    // when
    viewModel.onLaunchWithUrl(url, false)

    // then
    assertThat(states).containsExactly(
      CallActionScreenState(step = TagProcessingStep.Processing),
      CallActionScreenState(step = TagProcessingStep.Failure(TagProcessingStep.FailureType.TagNotFound(tagUuid)))
    )
  }

  @Test
  fun `should set missing configuration when there is no configuration for tag`() {
    // given
    val tagId = 2L
    val tagUuid = "123"
    val tagName = "name"
    val url = "https://supla.org/tag/$tagUuid"
    val uri: Uri = mockk {
      every { host } returns "supla.org"
      every { pathSegments } returns listOf("tag", tagUuid)
    }
    val tag: NfcTagDataEntity = mockk {
      every { tagEntity } returns mockk {
        every { id } returns tagId
        every { name } returns tagName
        every { actionId } returns null
        every { configuration } returns null
      }

      every { name(any()) } returns LocalizedString.Empty
    }

    every { uriProxy.toUri(url) } returns uri
    every { dateProvider.currentTimestamp() } returns 1L
    coEvery { nfcTagRepository.findByUuidWithDependencies(tagUuid) } returns tag
    coEvery { nfcCallRepository.insert(tagId, NfcCallResult.ACTION_MISSING) } returns 1L

    // when
    viewModel.onLaunchWithUrl(url, false)

    // then

    assertThat(states).contains(
      CallActionScreenState(step = TagProcessingStep.Processing),
      CallActionScreenState(
        step = TagProcessingStep.Failure(TagProcessingStep.FailureType.TagNotConfigured(tagId)),
        tagData = CallActionScreenState.TagData(tagName, null, LocalizedString.Empty)
      )
    )
  }

  @Test
  fun `should set success`() {
    // given
    mockkObject(NativeLoader)
    every { NativeLoader.loadLibrary(any()) } just Runs

    val tagId = 2L
    val tagUuid = "123"
    val tagName = "name"
    val profileId = 1L
    val url = "https://supla.org/tag/$tagUuid"
    val uri: Uri = mockk {
      every { host } returns "supla.org"
      every { pathSegments } returns listOf("tag", tagUuid)
    }
    val tag: NfcTagDataEntity = mockk {
      every { tagEntity } returns mockk {
        every { id } returns tagId
        every { name } returns tagName
        every { actionId } returns null
        every { configuration } returns NfcTagEntity.Configuration(profileId, SubjectType.CHANNEL, 1, ActionId.TOGGLE)
      }

      every { name(any()) } returns LocalizedString.Empty
    }

    val singleCall: SingleCall = mockk {
      every { executeAction(any()) } returns SingleCall.Result.Success
    }
    every { uriProxy.toUri(url) } returns uri
    every { dateProvider.currentTimestamp() } returnsMany listOf(1L, 500L)
    every { singleCallProvider.provide(profileId) } returns singleCall
    coEvery { nfcTagRepository.findByUuidWithDependencies(tagUuid) } returns tag
    coEvery { nfcCallRepository.insert(tagId, NfcCallResult.SUCCESS) } returns 1L

    // when
    viewModel.onLaunchWithUrl(url, false)

    // then
    assertThat(states).containsExactly(
      CallActionScreenState(step = TagProcessingStep.Processing),
      CallActionScreenState(
        step = TagProcessingStep.Processing,
        tagData = CallActionScreenState.TagData(tagName, null, LocalizedString.Empty)
      ),
      CallActionScreenState(
        step = TagProcessingStep.Success,
        tagData = CallActionScreenState.TagData(tagName, null, LocalizedString.Empty)
      )
    )

    verify(exactly = 1) {
      uriProxy.toUri(url)
      singleCallProvider.provide(profileId)
    }
    coVerify(exactly = 1) {
      nfcTagRepository.findByUuidWithDependencies(tagUuid)
      nfcCallRepository.insert(tagId, NfcCallResult.SUCCESS)
    }
    verify(exactly = 2) { dateProvider.currentTimestamp() }

    confirmVerified(singleCallProvider, getCaptionUseCase, nfcCallRepository, nfcTagRepository, uriProxy, dateProvider)
  }

  @Test
  fun `should handle nfc tag only once`() {
    // given
    mockkObject(NativeLoader)
    every { NativeLoader.loadLibrary(any()) } just Runs

    val tagId = 2L
    val tagUuid = "123"
    val tagName = "name"
    val profileId = 1L
    val url = "https://supla.org/tag/$tagUuid"
    val uri: Uri = mockk {
      every { host } returns "supla.org"
      every { pathSegments } returns listOf("tag", tagUuid)
    }
    val tag: NfcTagDataEntity = mockk {
      every { tagEntity } returns mockk {
        every { id } returns tagId
        every { name } returns tagName
        every { actionId } returns null
        every { configuration } returns NfcTagEntity.Configuration(profileId, SubjectType.CHANNEL, 1, ActionId.TOGGLE)
      }

      every { name(any()) } returns LocalizedString.Empty
    }

    val singleCall: SingleCall = mockk {
      every { executeAction(any()) } returns SingleCall.Result.Success
    }
    every { uriProxy.toUri(url) } returns uri
    every { dateProvider.currentTimestamp() } returnsMany listOf(1L, 500L)
    every { singleCallProvider.provide(profileId) } returns singleCall
    coEvery { nfcTagRepository.findByUuidWithDependencies(tagUuid) } returns tag
    coEvery { nfcCallRepository.insert(tagId, NfcCallResult.SUCCESS) } returns 1L

    // when
    viewModel.onLaunchWithUrl(url, false)
    viewModel.onLaunchWithUrl(url, false)

    // then
    assertThat(states).containsExactly(
      CallActionScreenState(step = TagProcessingStep.Processing),
      CallActionScreenState(
        step = TagProcessingStep.Processing,
        tagData = CallActionScreenState.TagData(tagName, null, LocalizedString.Empty)
      ),
      CallActionScreenState(
        step = TagProcessingStep.Success,
        tagData = CallActionScreenState.TagData(tagName, null, LocalizedString.Empty)
      )
    )

    verify(exactly = 2) { uriProxy.toUri(url) }
    verify(exactly = 1) { singleCallProvider.provide(profileId) }
    coVerify(exactly = 1) {
      nfcTagRepository.findByUuidWithDependencies(tagUuid)
      nfcCallRepository.insert(tagId, NfcCallResult.SUCCESS)
    }
    verify(exactly = 2) { dateProvider.currentTimestamp() }

    confirmVerified(singleCallProvider, getCaptionUseCase, nfcCallRepository, nfcTagRepository, uriProxy, dateProvider)
  }
}
