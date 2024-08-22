package org.supla.android.ui.layouts
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

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.extensions.guardLet
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.list.CreateListItemUpdateEventDataUseCase

abstract class BaseSlideableContent<T : SlideableListItemData> : BaseAbstractComposeView {

  constructor(context: Context) : super(context, null, 0)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
    loadAttributes(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    loadAttributes(context, attrs)
  }

  var onInfoClick: () -> Unit = { }
  var onIssueClick: () -> Unit = { }
  var onTitleLongClick: () -> Unit = { }
  var onItemClick: () -> Unit = { }

  protected var data: T? by mutableStateOf(null)
  protected var hasLeftButton: Boolean = false
  protected var hasRightButton: Boolean = false

  abstract val createListItemUpdateEventDataUseCase: CreateListItemUpdateEventDataUseCase
  abstract val schedulers: SuplaSchedulers

  private var remoteId: Int? = null
  private var itemType: ItemType? = null
  private var updateDisposable: Disposable? = null

  val isOnline: Boolean
    get() = data?.online == true

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val (itemType) = guardLet(itemType) { return }
    val (remoteId) = guardLet(remoteId) { return }

    updateDisposable =
      createListItemUpdateEventDataUseCase(itemType, remoteId)
        .subscribeOn(schedulers.io)
        .observeOn(schedulers.ui)
        .subscribeBy(onNext = this::updateData)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    if (updateDisposable?.isDisposed == false) {
      updateDisposable?.dispose()
    }
  }

  fun bind(
    itemType: ItemType,
    remoteId: Int,
    data: T,
    onInfoClick: () -> Unit,
    onIssueClick: () -> Unit,
    onTitleLongClick: () -> Unit,
    onItemClick: () -> Unit
  ) {
    this.itemType = itemType
    this.remoteId = remoteId
    this.data = data
    this.onInfoClick = onInfoClick
    this.onIssueClick = onIssueClick
    this.onTitleLongClick = onTitleLongClick
    this.onItemClick = onItemClick
  }

  private fun loadAttributes(context: Context, attrs: AttributeSet?) {
    context.theme.obtainStyledAttributes(attrs, R.styleable.ThermostatListItemView, 0, 0).apply {
      try {
        hasLeftButton = getBoolean(R.styleable.ThermostatListItemView_hasLeftButton, false)
        hasRightButton = getBoolean(R.styleable.ThermostatListItemView_hasRightButton, false)
      } finally {
        recycle()
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun updateData(data: SlideableListItemData) {
    (data as? T)?.let { this.data = it }
  }
}
