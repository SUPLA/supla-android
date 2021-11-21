package org.supla.android.cfg

import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.databinding.DataBindingUtil
import org.supla.android.databinding.FragmentLocationReorderBinding
import org.supla.android.R

class LocationOrderingFragment: Fragment() {
 
    private lateinit var binding: FragmentLocationReorderBinding
    private val viewModel: LocationReorderViewModel by viewModels()
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,
                                          R.layout.fragment_location_reorder,
                                          container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }
   
}
