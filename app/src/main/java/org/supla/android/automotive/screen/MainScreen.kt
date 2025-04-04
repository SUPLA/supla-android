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
import org.supla.android.R
import org.supla.android.data.source.AndroidAutoItemRepository
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity
import org.supla.android.data.source.local.entity.complex.AndroidAutoDataEntity
import org.supla.android.events.UpdateEventsManager
import org.supla.android.images.ImageCache
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.SubjectType
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
  updateEventsManager: UpdateEventsManager,
  carContext: CarContext
) : Screen(carContext) {

  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  private val state = MainScreenState()

  private val disposables = CompositeDisposable()

  private var observer = object : DefaultLifecycleObserver {
    override fun onResume(owner: LifecycleOwner) {
      load()
    }

    override fun onDestroy(owner: LifecycleOwner) {
      disposables.dispose()
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
      val list = ItemList.Builder()
      state.items.forEach {
        val item = GridItem.Builder()
          .setTitle(it.androidAutoItemEntity.caption)

        val imageId = it.icon
        if (imageId.userImage) {
          IconCompat.createFromIcon(carContext, Icon.createWithBitmap(ImageCache.getBitmapForAuto(carContext, imageId, true)))
            ?.let { icon ->
              item.setImage(CarIcon.Builder(icon).build())
            }
        } else {
          IconCompat.createFromIcon(carContext, Icon.createWithResource(carContext, imageId.id))?.let { icon ->
            item.setImage(CarIcon.Builder(icon).setTint(PRIMARY).build())
          }
        }

        if (state.executing.contains(it.androidAutoItemEntity.id)) {
          item.setText(carContext.getString(R.string.android_auto_executing))
        } else {
          item.setText(carContext.getString(it.androidAutoItemEntity.subjectType.nameRes))
        }


        item.setOnClickListener {
          coroutineScope.launch {
            state.executing.add(it.androidAutoItemEntity.id)
            invalidate()

            launch(Dispatchers.IO) {
              singleCallProvider.provide(it.androidAutoItemEntity.profileId).executeAction(
                ActionParameters(
                  action = it.androidAutoItemEntity.action,
                  subjectType = it.androidAutoItemEntity.subjectType,
                  subjectId = it.androidAutoItemEntity.subjectId
                )
              )
              delay(5000)

              state.executing.remove(it.androidAutoItemEntity.id)
              invalidate()
            }
          }
        }

        list.addItem(item.build())
      }

      return GridTemplate.Builder()
        .setHeader(Header.Builder().setTitle(CarText.create(carContext.getString(R.string.app_name))).build())
        .setSingleList(list.build())
        .build()
    }
  }

  private val AndroidAutoDataEntity.icon: ImageId
    get() = when (androidAutoItemEntity.subjectType) {
      SubjectType.GROUP -> getChannelIconUseCase.forState(groupEntity!!, groupEntity.offlineState.value)
      SubjectType.SCENE -> getSceneIconUseCase(sceneEntity!!)
      SubjectType.CHANNEL -> getChannelIconUseCase.forState(channelEntity!!, channelEntity.offlineState.value)
    }
}

private data class MainScreenState(
  var items: List<AndroidAutoDataEntity> = emptyList(),
  var loading: Boolean = true,
  val executing: MutableList<Long> = mutableListOf()
)