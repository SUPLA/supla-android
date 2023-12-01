package org.supla.android.ui.views.list.items
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
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.databinding.LiIncThermostatItemBigBinding
import org.supla.android.databinding.LiIncThermostatItemBinding
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.scaled
import org.supla.android.extensions.toPx
import org.supla.android.extensions.visibleIf
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.ui.views.list.ListItemScaffoldView
import kotlin.math.roundToInt

class ThermostatListItemView : ListItemScaffoldView {

  private lateinit var binding: BindingContainer

  constructor(context: Context) : super(context, null, 0) {
  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
  }

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
  }

  override fun onViewInitialized() {
    binding = if (scale > 1) {
      BindingContainer(big = LiIncThermostatItemBigBinding.inflate(LayoutInflater.from(context), this))
    } else {
      BindingContainer(normal = LiIncThermostatItemBinding.inflate(LayoutInflater.from(context), this))
    }

    super.onViewInitialized()

    (binding.listItemThermostatContainer?.layoutParams as LayoutParams).topMargin =
      resources.getDimension(R.dimen.distance_default).times(scale).roundToInt()

    binding.listItemIndicator?.layoutParams?.width = 12.dp.toPx().scaled(scale, lowerLimit = 1f).roundToInt()
    binding.listItemIndicator?.layoutParams?.height = 12.dp.toPx().scaled(scale, lowerLimit = 1f).roundToInt()

    binding.listItemSubvalue?.let { scale(it, R.dimen.font_size_body2, lowerLimit = 1f) }

  }

  override fun listItemIcon(): AppCompatImageView? = binding.listItemIcon
  override fun listItemContainer(): ViewGroup? = binding.listItemThermostatContainer
  override fun listItemValue(): TextView? = binding.listItemValue

  override fun update(data: SlideableListItemData) {
    super.update(data)

    val (thermostatData) = guardLet(data as? SlideableListItemData.Thermostat) { return }

    binding.listItemIcon?.visibleIf(thermostatData.iconProvider != null)
    binding.listItemIndicator?.visibleIf(thermostatData.indicatorIcon != null)

    thermostatData.iconProvider?.let { binding.listItemIcon?.setImageBitmap(it(context)) }
    thermostatData.indicatorIcon?.let { binding.listItemIndicator?.setImageResource(it) }
    binding.listItemValue?.text = thermostatData.value
    binding.listItemSubvalue?.text = thermostatData.subValue
  }

  data class BindingContainer(
    val normal: LiIncThermostatItemBinding? = null,
    val big: LiIncThermostatItemBigBinding? = null
  ) {
    val listItemIcon: AppCompatImageView?
      get() = normal?.listItemIcon ?: big?.listItemIcon

    val listItemIndicator: AppCompatImageView?
      get() = normal?.listItemIndicator ?: big?.listItemIndicator

    val listItemValue: TextView?
      get() = normal?.listItemValue ?: big?.listItemValue

    val listItemSubvalue: TextView?
      get() = normal?.listItemSubvalue ?: big?.listItemSubvalue

    val listItemThermostatContainer: ViewGroup?
      get() = normal?.listItemThermostatContainer ?: big?.listItemThermostatContainer
  }
}