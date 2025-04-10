package org.supla.android.automotive.screen
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

import android.graphics.drawable.Icon
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.CarColor.PRIMARY
import androidx.car.app.model.CarIcon
import androidx.car.app.model.CarText
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.infrastructure.TextToSpeechHelper
import org.supla.android.data.source.AndroidAutoItemRepository
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity
import org.supla.android.data.source.local.entity.complex.AndroidAutoDataEntity
import org.supla.android.data.source.remote.SuplaResultCode
import org.supla.android.events.UpdateEventsManager
import org.supla.android.extensions.TAG
import org.supla.android.extensions.ucFirst
import org.supla.android.images.ImageCache
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.android.lib.singlecall.ResultException
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase

class MainScreen(
  private val androidAutoItemRepository: AndroidAutoItemRepository,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getSceneIconUseCase: GetSceneIconUseCase,
  private val singleCallProvider: SingleCall.Provider,
  private val schedulers: SuplaSchedulers,
  private val preferences: Preferences,
  updateEventsManager: UpdateEventsManager,
  dateProvider: DateProvider,
  carContext: CarContext
) : Screen(carContext) {

  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  private val state = MainScreenState()

  private val disposables = CompositeDisposable()

  private val textToSpeechHelper = TextToSpeechHelper(carContext, dateProvider)

  private var observer = object : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
      textToSpeechHelper.onCreate()
    }

    override fun onResume(owner: LifecycleOwner) {
      load()
    }

    override fun onDestroy(owner: LifecycleOwner) {
      disposables.dispose()
      textToSpeechHelper.onDestroy()
    }
  }

  init {
    lifecycle.addObserver(observer)

    disposables.add(
      updateEventsManager.observeScenesUpdate()
        .subscribeOn(schedulers.io)
        .observeOn(schedulers.ui)
        .subscribe {
          load()
        }
    )
  }

  private fun load() {
    state.loading = true
    invalidate()
    disposables.add(
      androidAutoItemRepository.findAll().firstOrError()
        .subscribeOn(schedulers.io)
        .observeOn(schedulers.ui)
        .subscribeBy(
          onSuccess = { items ->
            state.items = items
            state.loading = false
            invalidate()
          }
        )
    )
  }

  override fun onGetTemplate(): Template {
    if (state.loading) {
      return GridTemplate.Builder()
        .setHeader(Header.Builder().setTitle(CarText.create(carContext.getString(R.string.app_name))).build())
        .setLoading(true)
        .build()
    } else if (state.items.isEmpty()) {
      return MessageTemplate.Builder(CarText.create(carContext.getString(R.string.android_auto_empty)))
        .setHeader(Header.Builder().setTitle(CarText.create(carContext.getString(R.string.app_name))).build())
        .build()
    } else {
      return GridTemplate.Builder()
        .setHeader(Header.Builder().setTitle(CarText.create(carContext.getString(R.string.app_name))).build())
        .setSingleList(getGridList())
        .build()
    }
  }

  private fun getGridList(): ItemList {
    val list = ItemList.Builder()
    state.items.forEach { item ->
      val gridItem = GridItem.Builder()
        .setTitle(item.androidAutoItemEntity.caption)

      val imageId = item.icon(getChannelIconUseCase, getSceneIconUseCase)
      if (imageId.userImage) {
        val bitmap = ImageCache.getBitmapForAuto(carContext, imageId, true)
        IconCompat.createFromIcon(carContext, Icon.createWithBitmap(bitmap))
          ?.let { icon ->
            gridItem.setImage(CarIcon.Builder(icon).build())
          }
      } else {
        IconCompat.createFromIcon(carContext, Icon.createWithResource(carContext, imageId.id))?.let { icon ->
          gridItem.setImage(CarIcon.Builder(icon).setTint(PRIMARY).build())
        }
      }

      if (state.errors.contains(item.androidAutoItemEntity.id)) {
        state.errors[item.androidAutoItemEntity.id]?.let { gridItem.setText(it) }
      } else if (state.executing.contains(item.androidAutoItemEntity.id)) {
        gridItem.setText(carContext.getString(R.string.android_auto_executing))
      } else {
        gridItem.setText(carContext.getString(item.androidAutoItemEntity.subjectType.nameRes).ucFirst())
      }

      gridItem.setOnClickListener { executeAction(item) }

      list.addItem(gridItem.build())
    }

    return list.build()
  }

  private fun executeAction(item: AndroidAutoDataEntity) {
    coroutineScope.launch {
      state.executing.add(item.androidAutoItemEntity.id)

      launch(Dispatchers.IO) {
        try {
          singleCallProvider.provide(item.androidAutoItemEntity.profileId).executeAction(
            ActionParameters(
              action = item.androidAutoItemEntity.action,
              subjectType = item.androidAutoItemEntity.subjectType,
              subjectId = item.androidAutoItemEntity.subjectId
            )
          )
        } catch (e: Exception) {
          Trace.e(TAG, "Could not execute action", e)
          val errorString = carContext.getString(getErrorMessage(e, item.androidAutoItemEntity))
          state.errors[item.androidAutoItemEntity.id] = errorString
          if (preferences.playAndroidAuto()) {
            textToSpeechHelper.speak(errorString)
          }
        }
        invalidate()

        delay(5000)

        state.executing.remove(item.androidAutoItemEntity.id)
        state.errors.remove(item.androidAutoItemEntity.id)
        invalidate()
      }
    }
  }

  private fun getErrorMessage(error: Exception, item: AndroidAutoItemEntity): Int {
    if (error is ResultException) {
      when (error.resultCode) {
        SuplaResultCode.CHANNEL_IS_OFFLINE -> return R.string.channel_offline
        SuplaResultCode.CHANNEL_NOT_FOUND -> return R.string.channel_not_found
        SuplaResultCode.INACTIVE ->
          if (item.subjectType == SubjectType.SCENE) {
            return R.string.scene_inactive
          }

        else -> return R.string.android_auto_error
      }
    }

    return R.string.scene_inactive
  }
}

private data class MainScreenState(
  var items: List<AndroidAutoDataEntity> = emptyList(),
  var loading: Boolean = true,
  val executing: MutableList<Long> = mutableListOf(),
  val errors: MutableMap<Long, String> = mutableMapOf()
)
