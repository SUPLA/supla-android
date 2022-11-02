package org.supla.android.scenes

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import org.supla.android.db.Scene

@BindingAdapter("scenes")
fun setScenes(recyclerView: RecyclerView, scenes: List<Scene>?) {
  scenes?.let {
    (recyclerView.adapter as ScenesAdapter).setScenes(scenes)
  }
}