package org.supla.android.cfg

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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.databinding.FragmentLocationReorderBinding

@AndroidEntryPoint
class LocationOrderingFragment : Fragment(R.layout.fragment_location_reorder) {

  private val binding by viewBinding(FragmentLocationReorderBinding::bind)
  private val viewModel: LocationReorderViewModel by viewModels()
  private lateinit var adapter: LocationReorderAdapter

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    adapter = LocationReorderAdapter(
      requireActivity(),
      viewModel.getLocations(),
      binding.locationList,
      viewModel
    )
    binding.locationList.adapter = adapter
  }
}
