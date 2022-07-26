package org.supla.android.channeldetail

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

import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.os.Bundle
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.db.DbHelper
import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelGroup
import org.supla.android.databinding.DetailFragmentGenericBinding

/*
GenericDetailFrament shows detail view for channel that
would otherwise had no detail view.
*/
@AndroidEntryPoint
class GenericDetailFragment(private val viewModelFactory: ViewModelProvider.Factory): DetailFragment(), HasDefaultViewModelProviderFactory {
    private val viewModel: ChannelDetailViewModel by viewModels()
    
    private lateinit var binding: DetailFragmentGenericBinding
    @Inject lateinit var dbh: DbHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
                                          R.layout.detail_fragment_generic,
                                          container, false)
        binding.lifecycleOwner = requireActivity()
        binding.viewModel = viewModel

        return binding.root
    }

    private var _remoteId: Int = 0
    private var _isGroup: Boolean = false

    override var channelBase: ChannelBase
        get() = viewModel.channel
        set(value) {
            viewModel.channel = value
            _remoteId = value.getRemoteId()
            _isGroup = value is ChannelGroup
        }

    override fun getChannelFromDatabase(): ChannelBase {
        return if(_isGroup) dbh.getChannelGroup(getRemoteId()) else dbh.getChannel(getRemoteId())
    }

    override fun getRemoteId(): Int {
        return _remoteId
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun detailWillHide(offline: Boolean): Boolean {
        return true
    }

    override fun onDetailShow() {}
    override fun onDetailHide() {}
    override fun onChannelDataChanged() {}

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return viewModelFactory
    }   
}
