package org.supla.android

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.db.ChannelBase
import org.supla.android.db.MeasurementsDbHelper
import org.supla.android.images.ImageCache
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaChannelState
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.SuplaEvent
import org.supla.android.listview.ChannelListView
import org.supla.android.listview.ChannelListView.*
import org.supla.android.listview.ListViewCursorAdapter
import org.supla.android.listview.draganddrop.ListViewDragListener
import org.supla.android.navigator.MainNavigator
import org.supla.android.restapi.DownloadUserIcons
import org.supla.android.restapi.SuplaRestApiClientTask
import org.supla.android.restapi.SuplaRestApiClientTask.IAsyncResults
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

@AndroidEntryPoint
class MainActivity : NavigationActivity(), View.OnClickListener, OnChannelButtonTouchListener, OnDetailListener,
  OnSectionLayoutTouchListener, IAsyncResults, OnChannelButtonClickListener, OnCaptionLongClickListener, OnSharedPreferenceChangeListener,
  NavigationBarView.OnItemSelectedListener {
  //  private ChannelListView channelLV;
  //  private ChannelListView cgroupLV;
  //  private View scenesView;
  private var channelListViewCursorAdapter: ListViewCursorAdapter? = null
  private var cgroupListViewCursorAdapter: ListViewCursorAdapter? = null
  private var downloadUserIcons: DownloadUserIcons? = null
  private var NotificationView: RelativeLayout? = null
  private var notif_handler: Handler? = null
  private var notif_nrunnable: Runnable? = null
  private var notif_img: ImageView? = null
  private var notif_text: TextView? = null
  private var channelStatePopup: ChannelStatePopup? = null
  private lateinit var bottomNavigation: BottomNavigationView
  private lateinit var bottomBar: BottomAppBar
  private val channelsOpened = true

  @Inject
  lateinit var navigator: MainNavigator

  // Used in reordering. The initial position of taken item is saved here.
  private var dragInitialPosition: Int? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Trace.d("MainActivity", "Created!");
    notif_handler = null
    notif_nrunnable = null
    setContentView(R.layout.activity_main)
    bottomNavigation = findViewById(R.id.bottomnavbar)
    bottomBar = findViewById(R.id.bottombar)
    NotificationView = Inflate(R.layout.notification, null) as RelativeLayout
    NotificationView!!.visibility = View.GONE
    val NotifBgLayout = NotificationView!!.findViewById<RelativeLayout>(R.id.notif_bg_layout)
    NotifBgLayout.setOnClickListener(this)
    NotifBgLayout.setBackgroundColor(resources.getColor(R.color.notification_bg))
    rootLayout.addView(NotificationView)
    notif_img = NotificationView!!.findViewById(R.id.notif_img)
    notif_text = NotificationView!!.findViewById<TextView?>(R.id.notif_txt).also {
      it.typeface = SuplaApp.getApp().typefaceOpenSansRegular
    }

//    channelLV = findViewById(R.id.channelsListView);
//    channelLV.setOnChannelButtonClickListener(this);
//    channelLV.setOnChannelButtonTouchListener(this);
//    channelLV.setOnCaptionLongClickListener(this);
//    channelLV.setOnDetailListener(this);
//
//    cgroupLV = findViewById(R.id.channelGroupListView);
//    cgroupLV.setOnChannelButtonTouchListener(this);
//    cgroupLV.setOnDetailListener(this);
//
//    scenesView = findViewById(R.id.scenesView);
    MeasurementsDbHelper.getInstance(this) // For upgrade purposes
    RegisterMessageHandler()
//    showMenuBar()
//    showMenuButton()
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    prefs.registerOnSharedPreferenceChangeListener(this)

    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHostFragment.navController

    navController.setGraph(R.navigation.main_nav_graph)

    val cfg = AppBarConfiguration(setOf(R.id.channel_list_fragment, R.id.group_list_fragment, R.id.scene_list_fragment))
    NavigationUI.setupWithNavController(
      bottomBar,
      navController,
      cfg
    )
    //bottomBar.setupWithNavController(navController, cfg)
    //bottomNavigation.setupWithNavController(navController, cfg)
    bottomNavigation.setOnItemSelectedListener {
      return@setOnItemSelectedListener it.onNavDestinationSelected(navController) || super.onOptionsItemSelected(it)
    }

    navController.addOnDestinationChangedListener { _, destination, _ -> configureNavBar(destination) }
    findViewById<Toolbar>(R.id.nav_toolbar).apply {
      setNavigationIcon(R.drawable.hamburger)
      setNavigationOnClickListener {
        if (menuIsVisible()) {
          hideMenu(true)
        } else {
          showMenu(true)
        }
      }
    }

  }

  private fun configureNavBar(destination: NavDestination) {
    if (destination.id == R.id.channel_list_fragment) {
      bottomNavigation.selectedItemId = R.id.channel_list_fragment
    }
  }

  override fun onStop() {
    super.onStop()
    if (channelListViewCursorAdapter != null && channelListViewCursorAdapter!!.isInReorderingMode) {
      channelListViewCursorAdapter!!.stopReorderingMode()
    }
    if (cgroupListViewCursorAdapter != null && cgroupListViewCursorAdapter!!.isInReorderingMode) {
      cgroupListViewCursorAdapter!!.stopReorderingMode()
    }
  }

  private fun SetListCursorAdapter(): Boolean {

//    if (channelListViewCursorAdapter == null) {
//
//      channelListViewCursorAdapter =
//          new ListViewCursorAdapter(this, getDbHelper().getChannelListCursor());
//      channelLV.setAdapter(channelListViewCursorAdapter);
//
//      channelLV.setOnItemLongClickListener(
//          (parent, view, position, id) -> onDragStarted(view, position, channelLV));
//      channelLV.setOnDragListener(
//          new ListViewDragListener(
//              channelLV,
//              droppedPosition ->
//                  onDragStopped(
//                      droppedPosition, channelListViewCursorAdapter, this::doChannelsReorder),
//              position -> onDragPositionChanged(position, channelListViewCursorAdapter)));
//      channelLV.setOnSectionLayoutTouchListener(this);
//
//      return true;
//
//    } else if (channelListViewCursorAdapter.getCursor() == null) {
//
//      channelListViewCursorAdapter.changeCursor(getDbHelper().getChannelListCursor());
//    }
    return false
  }

  private fun onDragStarted(view: View, position: Int, listView: ChannelListView): Boolean {
    if (listView.isDetailVisible
      || listView.isDetailSliding
      || listView.isChannelLayoutSlided
    ) {
      dragInitialPosition = null
      return false
    }
    dragInitialPosition = position
    val shadowBuilder = View.DragShadowBuilder(view)
    view.startDrag(null, shadowBuilder, listView.getItemAtPosition(position), 0)
    return true
  }

  private fun onDragStopped(droppedPosition: Int, adapter: ListViewCursorAdapter, reorder: Reorder) {
    adapter.stopReorderingMode()
    if (dragInitialPosition == null || droppedPosition == ListViewDragListener.INVALID_POSITION) {
      // Moved somewhere outside the list view or Something wrong, initial position not initialized.
      return
    }
    if (!adapter.isReorderPossible(dragInitialPosition!!, droppedPosition)) {
      // Moving outside of the section not allowed.
      return
    }
    val initialPositionItem = adapter.getItemForPosition(dragInitialPosition!!)
    val finalPositionItem = adapter.getItemForPosition(droppedPosition)
    reorder.doReorder(initialPositionItem, finalPositionItem)
  }

  private fun onDragPositionChanged(position: Int, adapter: ListViewCursorAdapter) {
    if (dragInitialPosition == null || position == ListViewDragListener.INVALID_POSITION) {
      // Moved somewhere outside the list view or Something wrong, initial position not initialized.
      adapter.stopReorderingMode()
      return
    }
    if (!adapter.isReorderPossible(dragInitialPosition!!, position)) {
      // Moving outside of the section not allowed.
      adapter.stopReorderingMode()
      return
    }
    adapter.updateReorderingMode(dragInitialPosition!!, position)
  }

  private fun doChannelsReorder(
    initialPositionItem: ListViewCursorAdapter.Item,
    finalPositionItem: ListViewCursorAdapter.Item
  ) {
    subscribe(
      dbHelper.reorderChannels(initialPositionItem, finalPositionItem),
      { channelListViewCursorAdapter!!.changeCursor(dbHelper.channelListCursor) }
    ) { throwable: Throwable? -> Trace.w(TAG, "Channels reordering failed", throwable) }
  }

  private fun doGroupsReorder(
    initialPositionItem: ListViewCursorAdapter.Item,
    finalPositionItem: ListViewCursorAdapter.Item
  ) {
    subscribe(
      dbHelper.reorderGroups(initialPositionItem, finalPositionItem),
      { cgroupListViewCursorAdapter!!.changeCursor(dbHelper.groupListCursor) }
    ) { throwable: Throwable? -> Trace.w(TAG, "Groups reordering failed", throwable) }
  }

  private fun SetGroupListCursorAdapter(): Boolean {

//    if (cgroupListViewCursorAdapter == null) {
//
//      cgroupListViewCursorAdapter =
//          new ListViewCursorAdapter(this, getDbHelper().getGroupListCursor(), true);
//      cgroupLV.setAdapter(cgroupListViewCursorAdapter);
//
//      cgroupLV.setOnItemLongClickListener(
//          (parent, view, position, id) -> onDragStarted(view, position, cgroupLV));
//      cgroupLV.setOnDragListener(
//          new ListViewDragListener(
//              cgroupLV,
//              droppedPosition ->
//                  onDragStopped(
//                      droppedPosition, cgroupListViewCursorAdapter, this::doGroupsReorder),
//              position -> onDragPositionChanged(position, cgroupListViewCursorAdapter)));
//      cgroupLV.setOnSectionLayoutTouchListener(this);
//
//      return true;
//
//    } else if (cgroupListViewCursorAdapter.getCursor() == null) {
//
//      cgroupListViewCursorAdapter.changeCursor(getDbHelper().getGroupListCursor());
//    }
    return false
  }

  protected fun hideDetail() {
//    if (channelLV.getVisibility() == View.VISIBLE) {
//      channelLV.hideDetail(false);
//    } else {
//      cgroupLV.hideDetail(false);
//    }
  }

  override fun onPause() {
    super.onPause()
    if (!SuperuserAuthorizationDialog.lastOneIsStillShowing()) {
      hideDetail()
    }
  }

  override fun onResume() {
    super.onResume()
    if (SuperuserAuthorizationDialog.lastOneIsStillShowing()) {
      return
    }
    resetListViews()
    hideDetail()
    runDownloadTask()
    val ra = RateApp(this)
    ra.showDialog(1000)
  }

  private fun resetListViews() {

//    if (!SetListCursorAdapter()) {
//      channelLV.setSelection(0);
//      channelLV.Refresh(getDbHelper().getChannelListCursor(), true);
//    }
//
//    if (!SetGroupListCursorAdapter()) {
//      cgroupLV.setSelection(0);
//      cgroupLV.Refresh(getDbHelper().getGroupListCursor(), true);
//    }
    reloadScenes()
  }

  override fun onDestroy() {
    // Trace.d("MainActivity", "Destroyed!");
    PreferenceManager.getDefaultSharedPreferences(this)
      .unregisterOnSharedPreferenceChangeListener(this)
    super.onDestroy()
  }

  private fun runDownloadTask() {
    Trace.d("RubDownloadTask", "RunDownloadTask")
    if (downloadUserIcons != null && !downloadUserIcons!!.isAlive(90)) {
      downloadUserIcons!!.cancel(true)
      downloadUserIcons = null
    }
    if (downloadUserIcons == null) {
      downloadUserIcons = DownloadUserIcons(this)
      downloadUserIcons!!.delegate = this
      downloadUserIcons!!.execute()
    }
  }

  override fun onDataChangedMsg(ChannelId: Int, GroupId: Int, extendedValue: Boolean) {

//    ChannelListView LV = channelLV;
//    int Id = ChannelId;
//
//    if (GroupId > 0) {
//      Id = GroupId;
//      LV = cgroupLV;
//    }
//
//    if (LV != null) {
//
//      if (LV.detail_getRemoteId() == Id) {
//
//        ChannelBase cbase = LV.detail_getChannel();
//        if (cbase != null && !cbase.getOnLine()) LV.hideDetail(false, true);
//        else LV.detail_OnChannelDataChanged();
//      }
//
//      LV.Refresh(
//          LV == channelLV
//              ? getDbHelper().getChannelListCursor()
//              : getDbHelper().getGroupListCursor(),
//          true);
//    }
//
//    if (channelStatePopup != null
//        && channelStatePopup.isVisible()
//        && channelStatePopup.getRemoteId() == ChannelId) {
//      channelStatePopup.update(ChannelId);
//    }
  }

  override fun onChannelState(state: SuplaChannelState) {
    if (state != null && channelStatePopup != null && channelStatePopup!!.isVisible && channelStatePopup!!.remoteId == state.channelID) {
      channelStatePopup!!.update(state)
    }
  }

  override fun onRegisteredMsg() {
    runDownloadTask()
    resetListViews()
  }

  override fun onDisconnectedMsg() {
    if (channelListViewCursorAdapter != null) channelListViewCursorAdapter!!.changeCursor(null)
  }

  override fun onConnectingMsg() {
    SetListCursorAdapter()
    SetGroupListCursorAdapter()
  }

  override fun onEventMsg(event: SuplaEvent) {
    super.onEventMsg(event)
    if ((event == null || event.Owner) && event.Event != SuplaConst.SUPLA_EVENT_SET_BRIDGE_VALUE_FAILED || event.ChannelID == 0) return
    val channel = dbHelper.getChannel(event.ChannelID) ?: return
    var imgResId = 0
    var imgId: ImageId? = null
    var msg: String
    if (event.Event == SuplaConst.SUPLA_EVENT_SET_BRIDGE_VALUE_FAILED) {
      if (channel.flags and SuplaConst.SUPLA_CHANNEL_FLAG_ZWAVE_BRIDGE > 0) {
        msg = resources.getString(R.string.zwave_device_communication_error)
        imgResId = R.drawable.zwave_device_error
      } else {
        return
      }
    } else {
      val msgId: Int
      msgId = when (event.Event) {
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGATEWAYLOCK -> R.string.event_openedthegateway
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGATE -> R.string.event_openedclosedthegate
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGARAGEDOOR -> R.string.event_openedclosedthegatedoors
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEDOORLOCK -> R.string.event_openedthedoor
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEROLLERSHUTTER -> R.string.event_openedcloserollershutter
        SuplaConst.SUPLA_EVENT_CONTROLLINGTHEROOFWINDOW -> R.string.event_openedclosedtheroofwindow
        SuplaConst.SUPLA_EVENT_POWERONOFF -> R.string.event_poweronoff
        SuplaConst.SUPLA_EVENT_LIGHTONOFF -> R.string.event_turnedthelightonoff
        SuplaConst.SUPLA_EVENT_VALVEOPENCLOSE -> R.string.event_openedclosedthevalve
        else -> return
      }
      imgId = channel.imageIdx
      msg = resources.getString(msgId)
      @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("HH:mm:ss")
      msg = sdf.format(Date()) + " " + event.SenderName + " " + msg
    }
    if (channel.caption != "") {
      msg = msg + " (" + channel.caption + ")"
    }
    ShowNotificationMessage(msg, imgId, imgResId)
  }

  private fun ShowHideNotificationView(show: Boolean) {
    if (!show && NotificationView!!.visibility == View.GONE) return
    val height = resources.getDimension(R.dimen.channel_layout_height)
    NotificationView!!.visibility = View.VISIBLE
    NotificationView!!.bringToFront()
    NotificationView!!.setTranslationY(if (show) height else 0f)
    NotificationView!!.animate()
      .translationY(if (show) 0f else height)
      .setDuration(100)
      .setListener(
        object : AnimatorListenerAdapter() {
          override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            if (!show) {
              NotificationView!!.visibility = View.GONE
            }
          }
        })
  }

  fun ShowNotificationMessage(msg: String?, imgId: ImageId?, imgResId: Int) {
    notif_img!!.setImageBitmap(null)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      notif_img!!.background = null
    } else {
      notif_img!!.setBackgroundDrawable(null)
    }
    if (imgId != null) {
      notif_img!!.setImageBitmap(ImageCache.getBitmap(this, imgId))
    } else if (imgResId > 0) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        notif_img!!.background = resources.getDrawable(imgResId)
      } else {
        notif_img!!.setBackgroundDrawable(resources.getDrawable(imgResId))
      }
    }
    notif_text!!.text = msg
    ShowHideNotificationView(true)
    if (notif_handler != null && notif_nrunnable != null) {
      notif_handler!!.removeCallbacks(notif_nrunnable!!)
    }
    notif_handler = Handler()
    notif_nrunnable = Runnable {
      HideNotificationMessage()
      notif_handler = null
      notif_nrunnable = null
    }
    notif_handler!!.postDelayed(notif_nrunnable!!, 5000)
  }

  fun HideNotificationMessage() {
    ShowHideNotificationView(false)
  }

  override fun onClick(v: View) {
    super.onClick(v)
    if (v.parent === NotificationView) {
      HideNotificationMessage()
    }
  }

  private fun ShowValveAlertDialog(channelId: Int, context: Context) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(android.R.string.dialog_alert_title)
    builder.setMessage(R.string.valve_open_warning)
    builder.setPositiveButton(
      R.string.yes
    ) { dialog: DialogInterface, id: Int ->
      val client = SuplaApp.getApp().getSuplaClient()
      if (client != null) {
        SuplaApp.Vibrate(context)
        client.open(channelId, false, 1)
      }
      dialog.cancel()
    }
    builder.setNeutralButton(R.string.no) { dialog: DialogInterface, id: Int -> dialog.cancel() }
    val alert = builder.create()
    alert.show()
  }

  override fun onChannelButtonTouch(
    clv: ChannelListView, left: Boolean, up: Boolean, remoteId: Int, channelFunc: Int
  ) {

//    if (menuIsVisible()) return;
//
//    SuplaClient client = SuplaApp.getApp().getSuplaClient();
//    if (new Preferences(this).isButtonAutohide()) clv.hideButton(false);
//
//    if (client == null) return;
//
//    if (!up && client.turnOnOff(this, !left, remoteId, clv == cgroupLV, channelFunc, true)) {
//      return;
//    }
//
//    if (!left
//        && !up
//        && (channelFunc == SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE
//            || channelFunc == SuplaConst.SUPLA_CHANNELFNC_VALVE_PERCENTAGE)) {
//      Channel channel = getDbHelper().getChannel(remoteId);
//      if (channel != null
//          && channel.getValue().isClosed()
//          && (channel.getValue().flooding() || channel.getValue().isManuallyClosed())) {
//        ShowValveAlertDialog(remoteId, this);
//        return;
//      }
//    }
//
//    if (!up
//        || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
//        || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW) {
//
//      SuplaApp.Vibrate(this);
//    }
//
//    if (up) {
//
//      if (channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
//          || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW) {
//        client.open(remoteId, clv == cgroupLV, 0);
//      }
//
//    } else {
//
//      int Open;
//
//      if (left) {
//        Open =
//            channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
//                    || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
//                ? 1
//                : 0;
//      } else {
//        Open =
//            channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
//                    || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
//                ? 2
//                : 1;
//      }
//
//      client.open(remoteId, clv == cgroupLV, Open);
//    }
  }

  override fun onBackPressed() {
    if (menuIsVisible()) {
      hideMenu(true)
    } else if (!navigator.back()) {
      finishAffinity()
    }
  }

  override fun onChannelDetailShow(channel: ChannelBase) {
    setMenubarDetailTitle(channel.getNotEmptyCaption(this))
    showBackButton()
    dismissProfileSelector()
    bottomBar!!.visibility = View.GONE
  }

  override fun onChannelDetailHide() {
    showMenuButton()
    bottomBar!!.visibility = View.VISIBLE
  }

  override fun onSectionClick(clv: ChannelListView, caption: String, locationId: Int) {

//    int _collapsed;
//    if (clv == channelLV) {
//      _collapsed = 0x1;
//    } else if (clv == cgroupLV) {
//      _collapsed = 0x2;
//    } else {
//      return;
//    }
//
//    Location location = getDbHelper().getLocation(locationId);
//    int collapsed = location.getCollapsed();
//
//    if ((collapsed & _collapsed) > 0) {
//      collapsed ^= _collapsed;
//    } else {
//      collapsed |= _collapsed;
//    }
//
//    location.setCollapsed(collapsed);
//    getDbHelper().updateLocation(location);
//
//    if (clv == channelLV) {
//      channelLV.Refresh(getDbHelper().getChannelListCursor(), true);
//    } else {
//      cgroupLV.Refresh(getDbHelper().getGroupListCursor(), true);
//    }
  }

  override fun onRestApiTaskStarted(task: SuplaRestApiClientTask) {}
  override fun onRestApiTaskFinished(task: SuplaRestApiClientTask) {
//    if (downloadUserIcons != null) {
//      if (downloadUserIcons.downloadCount() > 0) {
//        if (channelLV != null) {
//          channelLV.Refresh(getDbHelper().getChannelListCursor(), true);
//        }
//        if (cgroupLV != null) {
//          cgroupLV.Refresh(getDbHelper().getGroupListCursor(), true);
//        }
//        reloadScenes();
//      }
//      downloadUserIcons = null;
//    }
  }

  override fun onRestApiTaskProgressUpdate(task: SuplaRestApiClientTask, progress: Double) {}
  override fun onChannelStateButtonClick(clv: ChannelListView, remoteId: Int) {
    if (channelStatePopup == null) {
      channelStatePopup = ChannelStatePopup(this)
    }
    channelStatePopup!!.show(remoteId)
  }

  override fun onChannelCaptionLongClick(clv: ChannelListView, remoteId: Int) {
    SuplaApp.Vibrate(this)
    val editor = ChannelCaptionEditor(this)
    editor.edit(remoteId)
  }

  override fun onLocationCaptionLongClick(clv: ChannelListView, locationId: Int) {
    SuplaApp.Vibrate(this)
    val editor = LocationCaptionEditor(this)
    editor.edit(locationId)
  }

  override fun onSharedPreferenceChanged(prefss: SharedPreferences, key: String) {
    if (key == Preferences.pref_channel_height) {
      /* Ivalidate the adapter, so that channel list can
      be rebuilt with new layout. */
      channelListViewCursorAdapter = null
      cgroupListViewCursorAdapter = null
    }
  }

  override fun onProfileChanged() {
    super.onProfileChanged()
    resetListViews()
    runDownloadTask()
  }

  private interface Reorder {
    fun doReorder(firstItem: ListViewCursorAdapter.Item?, secondItem: ListViewCursorAdapter.Item?)
  }

  override fun onNavigationItemSelected(item: MenuItem): Boolean {
//    when(item.itemId) {
//      R.id.channels_item -> navigator.replaceTo(R.id.channel_list_fragment)
//      R.id.group_list_fragment -> navigator.replaceTo(R.id.group_list_fragment)
//      R.id.scenes_item -> navigator.replaceTo(R.id.scene_list_fragment)
//    }
//    if (menuIsVisible() || channelLV.isDetailSliding() || cgroupLV.isDetailSliding()) return false;
//
//    int scenesVisible = View.GONE, groupsVisible = View.GONE, channelsVisible = View.GONE;
//
//    switch (item.getItemId()) {
//      case R.id.channels_item:
//        channelsVisible = View.VISIBLE;
//        break;
//      case R.id.groups_item:
//        groupsVisible = View.VISIBLE;
//        break;
//      case R.id.scenes_item:
//        scenesVisible = View.VISIBLE;
//        break;
//    }
//
//    channelLV.setVisibility(channelsVisible);
//    cgroupLV.setVisibility(groupsVisible);
//    scenesView.setVisibility(scenesVisible);
//
//    channelsOpened = channelsVisible == View.VISIBLE;
    return true
  }

  //  ScenesFragment scenesFragment() {
  //    FragmentManager fmgr = getSupportFragmentManager();
  //    return (ScenesFragment) fmgr.findFragmentById(R.id.scenesFragment);
  //  }
  private fun reloadScenes() {
//    scenesFragment().reload();
  }

  companion object {
    private val TAG = MainActivity::class.java.simpleName
  }
}