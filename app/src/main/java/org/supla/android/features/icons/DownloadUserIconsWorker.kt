package org.supla.android.features.icons

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.rx3.await
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.RoomUserIconRepository
import org.supla.android.data.source.local.entity.UserIconEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.usecases.icon.FindIconsToDownloadUseCase
import org.supla.android.usecases.icon.LoadUserIconsIntoCacheUseCase
import org.supla.android.widget.WidgetManager
import org.supla.core.shared.data.model.rest.UserIconDto
import timber.log.Timber

private const val MAX_ICONS_PER_REQUEST = 4

@HiltWorker
class DownloadUserIconsWorker @AssistedInject constructor(
  private val loadUserIconsIntoCacheUseCase: LoadUserIconsIntoCacheUseCase,
  private val findIconsToDownloadUseCase: FindIconsToDownloadUseCase,
  private val suplaCloudServiceProvider: SuplaCloudService.Provider,
  private val userIconRepository: RoomUserIconRepository,
  private val profileRepository: ProfileRepository,
  private val widgetManager: WidgetManager,
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {
  override suspend fun doWork(): Result {
    val service = runCatching { suplaCloudServiceProvider.provide() }.getOrNull()
    if (service == null) {
      Timber.e("Could not retrieve service, scheduling retry.")
      return Result.retry()
    }
    val profile = profileRepository.findActiveProfileKtx()
    if (profile == null || profile.id == null) {
      Timber.i("No profile found - skipping")
      return Result.success()
    }

    Timber.i("User icons download started for profile (id: ${profile.id}, name: ${profile.name})")

    var anyIconAdded = false
    val iconIdsPackages = findIconsToDownloadUseCase(profile.id).chunked(MAX_ICONS_PER_REQUEST)

    for (iconIds in iconIdsPackages) {
      try {
        val userIcons = service.getUserIcons(iconIds.joinToString(","))

        for (userIcon in userIcons) {
          if (userIcon.id <= 0) {
            continue
          }
          userIconRepository.save(userIcon.entity(profile.id))
          anyIconAdded = true
        }
      } catch (ex: Exception) {
        Timber.e(ex, "User icons download failed")
      }
    }

    if (anyIconAdded) {
      Timber.i("New icons loaded, triggering cache reload")
      try {
        loadUserIconsIntoCacheUseCase().await()
      } catch (ex: Exception) {
        Timber.e(ex, "Icons cache load failed")
      }
    }

    return Result.success()
  }

  companion object {
    private val WORK_ID = DownloadUserIconsWorker::class.java.simpleName

    fun start(context: Context) {
      WorkManager.getInstance(context).enqueueUniqueWork(WORK_ID, ExistingWorkPolicy.KEEP, build())
    }

    private fun build(): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<DownloadUserIconsWorker>()
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .build()
  }
}

private fun UserIconDto.entity(profileId: Long): UserIconEntity =
  UserIconEntity(
    id = null,
    remoteId = id,
    image1 = image(0),
    image2 = image(1),
    image3 = image(2),
    image4 = image(3),
    image1Dark = imageDark(0),
    image2Dark = imageDark(1),
    image3Dark = imageDark(2),
    image4Dark = imageDark(3),
    profileId = profileId
  )
