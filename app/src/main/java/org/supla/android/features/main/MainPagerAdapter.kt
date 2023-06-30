package org.supla.android.features.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.supla.android.R
import org.supla.android.features.channellist.ChannelListFragment
import org.supla.android.features.grouplist.GroupListFragment
import org.supla.android.features.scenelist.SceneListFragment

class StandardDetailPagerAdapter(
  private val pages: Array<ListPage>,
  fragment: Fragment
) : FragmentStateAdapter(fragment) {

  override fun getItemCount(): Int = pages.size

  override fun createFragment(position: Int): Fragment = when (pages[position]) {
    ListPage.CHANNELS -> ChannelListFragment()
    ListPage.GROUPS -> GroupListFragment()
    ListPage.SCENES -> SceneListFragment()
  }
}

enum class ListPage(val menuId: Int) {
  CHANNELS(R.id.channel_list),
  GROUPS(R.id.group_list),
  SCENES(R.id.scene_list),
}
