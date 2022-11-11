package org.supla.android;

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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.Location;
import org.supla.android.db.MeasurementsDbHelper;
import org.supla.android.images.ImageCache;
import org.supla.android.images.ImageId;
import org.supla.android.lib.SuplaChannelState;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.SuplaEvent;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.ListViewCursorAdapter;
import org.supla.android.listview.draganddrop.ListViewDragListener;
import org.supla.android.restapi.DownloadUserIcons;
import org.supla.android.restapi.SuplaRestApiClientTask;
import org.supla.android.scenes.ScenesFragment;

public class MainActivity extends NavigationActivity
    implements OnClickListener,
        ChannelListView.OnChannelButtonTouchListener,
        ChannelListView.OnDetailListener,
        ChannelListView.OnSectionLayoutTouchListener,
        SuplaRestApiClientTask.IAsyncResults,
        ChannelListView.OnChannelButtonClickListener,
        ChannelListView.OnCaptionLongClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        NavigationBarView.OnItemSelectedListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  private ChannelListView channelLV;
  private ChannelListView cgroupLV;
  private View scenesView;
  private ListViewCursorAdapter channelListViewCursorAdapter;
  private ListViewCursorAdapter cgroupListViewCursorAdapter;
  private DownloadUserIcons downloadUserIcons = null;

  private RelativeLayout NotificationView;
  private Handler notif_handler;
  private Runnable notif_nrunnable;
  private ImageView notif_img;
  private TextView notif_text;
  private ChannelStatePopup channelStatePopup;
  private BottomNavigationView bottomNavigation;
  private BottomAppBar bottomBar;
  private boolean channelsOpened = true;

  // Used in reordering. The initial position of taken item is saved here.
  private Integer dragInitialPosition;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Trace.d("MainActivity", "Created!");

    notif_handler = null;
    notif_nrunnable = null;

    setContentView(R.layout.activity_main);

    bottomNavigation = findViewById(R.id.bottomnavbar);
    bottomNavigation.setOnItemSelectedListener(this);
    bottomBar = findViewById(R.id.bottombar);

    NotificationView = (RelativeLayout) Inflate(R.layout.notification, null);
    NotificationView.setVisibility(View.GONE);

    RelativeLayout NotifBgLayout = NotificationView.findViewById(R.id.notif_bg_layout);
    NotifBgLayout.setOnClickListener(this);
    NotifBgLayout.setBackgroundColor(getResources().getColor(R.color.notification_bg));

    getRootLayout().addView(NotificationView);

    notif_img = NotificationView.findViewById(R.id.notif_img);
    notif_text = NotificationView.findViewById(R.id.notif_txt);

    notif_text.setTypeface(SuplaApp.getApp().getTypefaceOpenSansRegular());

    channelLV = findViewById(R.id.channelsListView);
    channelLV.setOnChannelButtonClickListener(this);
    channelLV.setOnChannelButtonTouchListener(this);
    channelLV.setOnCaptionLongClickListener(this);
    channelLV.setOnDetailListener(this);

    cgroupLV = findViewById(R.id.channelGroupListView);
    cgroupLV.setOnChannelButtonTouchListener(this);
    cgroupLV.setOnDetailListener(this);

    scenesView = findViewById(R.id.scenesView);

    MeasurementsDbHelper.getInstance(this); // For upgrade purposes

    RegisterMessageHandler();
    showMenuBar();
    showMenuButton();

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onStop() {
    super.onStop();

    if (channelListViewCursorAdapter != null && channelListViewCursorAdapter.isInReorderingMode()) {
      channelListViewCursorAdapter.stopReorderingMode();
    }
    if (cgroupListViewCursorAdapter != null && cgroupListViewCursorAdapter.isInReorderingMode()) {
      cgroupListViewCursorAdapter.stopReorderingMode();
    }
  }

  private boolean SetListCursorAdapter() {

    if (channelListViewCursorAdapter == null) {

      channelListViewCursorAdapter =
          new ListViewCursorAdapter(this, getDbHelper().getChannelListCursor());
      channelLV.setAdapter(channelListViewCursorAdapter);

      channelLV.setOnItemLongClickListener(
          (parent, view, position, id) -> onDragStarted(view, position, channelLV));
      channelLV.setOnDragListener(
          new ListViewDragListener(
              channelLV,
              droppedPosition ->
                  onDragStopped(
                      droppedPosition, channelListViewCursorAdapter, this::doChannelsReorder),
              position -> onDragPositionChanged(position, channelListViewCursorAdapter)));
      channelLV.setOnSectionLayoutTouchListener(this);

      return true;

    } else if (channelListViewCursorAdapter.getCursor() == null) {

      channelListViewCursorAdapter.changeCursor(getDbHelper().getChannelListCursor());
    }

    return false;
  }

  private boolean onDragStarted(View view, int position, ChannelListView listView) {
    if (listView.isDetailVisible()
        || listView.isDetailSliding()
        || listView.isChannelLayoutSlided()) {
      dragInitialPosition = null;
      return false;
    }
    dragInitialPosition = position;
    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
    view.startDrag(null, shadowBuilder, listView.getItemAtPosition(position), 0);
    return true;
  }

  private void onDragStopped(int droppedPosition, ListViewCursorAdapter adapter, Reorder reorder) {
    adapter.stopReorderingMode();
    if (dragInitialPosition == null || droppedPosition == ListViewDragListener.INVALID_POSITION) {
      // Moved somewhere outside the list view or Something wrong, initial position not initialized.
      return;
    }
    if (!adapter.isReorderPossible(dragInitialPosition, droppedPosition)) {
      // Moving outside of the section not allowed.
      return;
    }
    ListViewCursorAdapter.Item initialPositionItem =
        adapter.getItemForPosition(dragInitialPosition);
    ListViewCursorAdapter.Item finalPositionItem = adapter.getItemForPosition(droppedPosition);

    reorder.doReorder(initialPositionItem, finalPositionItem);
  }

  private void onDragPositionChanged(int position, ListViewCursorAdapter adapter) {
    if (dragInitialPosition == null || position == ListViewDragListener.INVALID_POSITION) {
      // Moved somewhere outside the list view or Something wrong, initial position not initialized.
      adapter.stopReorderingMode();
      return;
    }
    if (!adapter.isReorderPossible(dragInitialPosition, position)) {
      // Moving outside of the section not allowed.
      adapter.stopReorderingMode();
      return;
    }
    adapter.updateReorderingMode(dragInitialPosition, position);
  }

  private void doChannelsReorder(
      ListViewCursorAdapter.Item initialPositionItem,
      ListViewCursorAdapter.Item finalPositionItem) {
    subscribe(
        getDbHelper().reorderChannels(initialPositionItem, finalPositionItem),
        () -> channelListViewCursorAdapter.changeCursor(getDbHelper().getChannelListCursor()),
        throwable -> Trace.w(TAG, "Channels reordering failed", throwable));
  }

  private void doGroupsReorder(
      ListViewCursorAdapter.Item initialPositionItem,
      ListViewCursorAdapter.Item finalPositionItem) {
    subscribe(
        getDbHelper().reorderGroups(initialPositionItem, finalPositionItem),
        () -> cgroupListViewCursorAdapter.changeCursor(getDbHelper().getGroupListCursor()),
        throwable -> Trace.w(TAG, "Groups reordering failed", throwable));
  }

  private boolean SetGroupListCursorAdapter() {

    if (cgroupListViewCursorAdapter == null) {

      cgroupListViewCursorAdapter =
          new ListViewCursorAdapter(this, getDbHelper().getGroupListCursor(), true);
      cgroupLV.setAdapter(cgroupListViewCursorAdapter);

      cgroupLV.setOnItemLongClickListener(
          (parent, view, position, id) -> onDragStarted(view, position, cgroupLV));
      cgroupLV.setOnDragListener(
          new ListViewDragListener(
              cgroupLV,
              droppedPosition ->
                  onDragStopped(
                      droppedPosition, cgroupListViewCursorAdapter, this::doGroupsReorder),
              position -> onDragPositionChanged(position, cgroupListViewCursorAdapter)));
      cgroupLV.setOnSectionLayoutTouchListener(this);

      return true;

    } else if (cgroupListViewCursorAdapter.getCursor() == null) {

      cgroupListViewCursorAdapter.changeCursor(getDbHelper().getGroupListCursor());
    }

    return false;
  }

  protected void hideDetail() {
    if (channelLV.getVisibility() == View.VISIBLE) {
      channelLV.hideDetail(false);
    } else {
      cgroupLV.hideDetail(false);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (!SuperuserAuthorizationDialog.lastOneIsStillShowing()) {
      hideDetail();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (SuperuserAuthorizationDialog.lastOneIsStillShowing()) {
      return;
    }

    resetListViews();

    hideDetail();
    runDownloadTask();

    RateApp ra = new RateApp(this);
    ra.showDialog(1000);
  }

  private void resetListViews() {

    if (!SetListCursorAdapter()) {
      channelLV.setSelection(0);
      channelLV.Refresh(getDbHelper().getChannelListCursor(), true);
    }

    if (!SetGroupListCursorAdapter()) {
      cgroupLV.setSelection(0);
      cgroupLV.Refresh(getDbHelper().getGroupListCursor(), true);
    }

    reloadScenes();
  }

  @Override
  protected void onDestroy() {
    // Trace.d("MainActivity", "Destroyed!");

    PreferenceManager.getDefaultSharedPreferences(this)
        .unregisterOnSharedPreferenceChangeListener(this);

    super.onDestroy();
  }

  private void runDownloadTask() {
    Trace.d("RubDownloadTask", "RunDownloadTask");
    if (downloadUserIcons != null && !downloadUserIcons.isAlive(90)) {
      downloadUserIcons.cancel(true);
      downloadUserIcons = null;
    }

    if (downloadUserIcons == null) {
      downloadUserIcons = new DownloadUserIcons(this);
      downloadUserIcons.setDelegate(this);
      downloadUserIcons.execute();
    }
  }

  @Override
  protected void onDataChangedMsg(int ChannelId, int GroupId, boolean extendedValue) {

    ChannelListView LV = channelLV;
    int Id = ChannelId;

    if (GroupId > 0) {
      Id = GroupId;
      LV = cgroupLV;
    }

    if (LV != null) {

      if (LV.detail_getRemoteId() == Id) {

        ChannelBase cbase = LV.detail_getChannel();
        if (cbase != null && !cbase.getOnLine()) LV.hideDetail(false, true);
        else LV.detail_OnChannelDataChanged();
      }

      LV.Refresh(
          LV == channelLV
              ? getDbHelper().getChannelListCursor()
              : getDbHelper().getGroupListCursor(),
          true);
    }

    if (channelStatePopup != null
        && channelStatePopup.isVisible()
        && channelStatePopup.getRemoteId() == ChannelId) {
      channelStatePopup.update(ChannelId);
    }
  }

  @Override
  protected void onChannelState(SuplaChannelState state) {
    if (state != null
        && channelStatePopup != null
        && channelStatePopup.isVisible()
        && channelStatePopup.getRemoteId() == state.getChannelID()) {
      channelStatePopup.update(state);
    }
  }

  @Override
  protected void onRegisteredMsg() {
    runDownloadTask();
    resetListViews();
  }

  @Override
  protected void onDisconnectedMsg() {

    if (channelListViewCursorAdapter != null) channelListViewCursorAdapter.changeCursor(null);
  }

  @Override
  protected void onConnectingMsg() {
    SetListCursorAdapter();
    SetGroupListCursorAdapter();
  }

  @Override
  protected void onEventMsg(SuplaEvent event) {
    super.onEventMsg(event);

    if (event == null
        || (event.Owner && event.Event != SuplaConst.SUPLA_EVENT_SET_BRIDGE_VALUE_FAILED)
        || event.ChannelID == 0) return;

    Channel channel = getDbHelper().getChannel(event.ChannelID);
    if (channel == null) return;

    int imgResId = 0;
    ImageId imgId = null;
    String msg;

    if (event.Event == SuplaConst.SUPLA_EVENT_SET_BRIDGE_VALUE_FAILED) {
      if ((channel.getFlags() & SuplaConst.SUPLA_CHANNEL_FLAG_ZWAVE_BRIDGE) > 0) {
        msg = getResources().getString(R.string.zwave_device_communication_error);
        imgResId = R.drawable.zwave_device_error;
      } else {
        return;
      }
    } else {
      int msgId;
      switch (event.Event) {
        case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGATEWAYLOCK:
          msgId = R.string.event_openedthegateway;
          break;
        case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGATE:
          msgId = R.string.event_openedclosedthegate;
          break;
        case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGARAGEDOOR:
          msgId = R.string.event_openedclosedthegatedoors;
          break;
        case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEDOORLOCK:
          msgId = R.string.event_openedthedoor;
          break;
        case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEROLLERSHUTTER:
          msgId = R.string.event_openedcloserollershutter;
          break;
        case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEROOFWINDOW:
          msgId = R.string.event_openedclosedtheroofwindow;
          break;
        case SuplaConst.SUPLA_EVENT_POWERONOFF:
          msgId = R.string.event_poweronoff;
          break;
        case SuplaConst.SUPLA_EVENT_LIGHTONOFF:
          msgId = R.string.event_turnedthelightonoff;
          break;
        case SuplaConst.SUPLA_EVENT_VALVEOPENCLOSE:
          msgId = R.string.event_openedclosedthevalve;
          break;
        default:
          return;
      }

      imgId = channel.getImageIdx();
      msg = getResources().getString(msgId);

      @SuppressLint("SimpleDateFormat")
      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
      msg = sdf.format(new Date()) + " " + event.SenderName + " " + msg;
    }

    if (!channel.getCaption().equals("")) {
      msg = msg + " (" + channel.getCaption() + ")";
    }

    ShowNotificationMessage(msg, imgId, imgResId);
  }

  private void ShowHideNotificationView(final boolean show) {

    if (!show && NotificationView.getVisibility() == View.GONE) return;

    float height = getResources().getDimension(R.dimen.channel_layout_height);

    NotificationView.setVisibility(View.VISIBLE);
    NotificationView.bringToFront();
    NotificationView.setTranslationY(show ? height : 0);

    NotificationView.animate()
        .translationY(show ? 0 : height)
        .setDuration(100)
        .setListener(
            new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (!show) {
                  NotificationView.setVisibility(View.GONE);
                }
              }
            });
  }

  public void ShowNotificationMessage(String msg, ImageId imgId, int imgResId) {

    notif_img.setImageBitmap(null);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      notif_img.setBackground(null);
    } else {
      notif_img.setBackgroundDrawable(null);
    }

    if (imgId != null) {
      notif_img.setImageBitmap(ImageCache.getBitmap(this, imgId));
    } else if (imgResId > 0) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        notif_img.setBackground(getResources().getDrawable(imgResId));
      } else {
        notif_img.setBackgroundDrawable(getResources().getDrawable(imgResId));
      }
    }

    notif_text.setText(msg);

    ShowHideNotificationView(true);

    if (notif_handler != null && notif_nrunnable != null) {
      notif_handler.removeCallbacks(notif_nrunnable);
    }

    notif_handler = new Handler();
    notif_nrunnable =
        () -> {
          HideNotificationMessage();

          notif_handler = null;
          notif_nrunnable = null;
        };

    notif_handler.postDelayed(notif_nrunnable, 5000);
  }

  public void HideNotificationMessage() {
    ShowHideNotificationView(false);
  }

  @Override
  public void onClick(View v) {
    super.onClick(v);

    if (v.getParent() == NotificationView) {
      HideNotificationMessage();
    }
  }

  private void ShowValveAlertDialog(final int channelId, final Context context) {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(android.R.string.dialog_alert_title);
    builder.setMessage(R.string.valve_open_warning);

    builder.setPositiveButton(
        R.string.yes,
        (dialog, id) -> {
          SuplaClient client = SuplaApp.getApp().getSuplaClient();
          if (client != null) {
            SuplaApp.Vibrate(context);
            client.open(channelId, false, 1);
          }
          dialog.cancel();
        });

    builder.setNeutralButton(R.string.no, (dialog, id) -> dialog.cancel());

    AlertDialog alert = builder.create();
    alert.show();
  }

  @Override
  public void onChannelButtonTouch(
      ChannelListView clv, boolean left, boolean up, int remoteId, int channelFunc) {

    if (menuIsVisible()) return;

    SuplaClient client = SuplaApp.getApp().getSuplaClient();
    if (new Preferences(this).isButtonAutohide()) clv.hideButton(false);

    if (client == null) return;

    if (!up && client.turnOnOff(this, !left, remoteId, clv == cgroupLV, channelFunc, true)) {
      return;
    }

    if (!left
        && !up
        && (channelFunc == SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE
            || channelFunc == SuplaConst.SUPLA_CHANNELFNC_VALVE_PERCENTAGE)) {
      Channel channel = getDbHelper().getChannel(remoteId);
      if (channel != null
          && channel.getValue().isClosed()
          && (channel.getValue().flooding() || channel.getValue().isManuallyClosed())) {
        ShowValveAlertDialog(remoteId, this);
        return;
      }
    }

    if (!up
        || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
        || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW) {

      SuplaApp.Vibrate(this);
    }

    if (up) {

      if (channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
          || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW) {
        client.open(remoteId, clv == cgroupLV, 0);
      }

    } else {

      int Open;

      if (left) {
        Open =
            channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
                    || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
                ? 1
                : 0;
      } else {
        Open =
            channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
                    || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
                ? 2
                : 1;
      }

      client.open(remoteId, clv == cgroupLV, Open);
    }
  }

  @Override
  public void onBackPressed() {
    if (menuIsVisible()) {
      hideMenu(true);
    } else if (channelLV.isDetailVisible()) {
      channelLV.onBackPressed();
    } else if (cgroupLV.isDetailVisible()) {
      cgroupLV.onBackPressed();
    } else if (!channelsOpened) {
      channelLV.setVisibility(View.VISIBLE);
      cgroupLV.setVisibility(View.GONE);
      scenesView.setVisibility(View.GONE);

      channelsOpened = true;
      bottomNavigation.getMenu().getItem(0).setChecked(true);
    } else {
      gotoMain();
    }
  }

  @Override
  public void onChannelDetailShow(ChannelBase channel) {
    setMenubarDetailTitle(channel.getNotEmptyCaption(this));
    showBackButton();
    dismissProfileSelector();
    bottomBar.setVisibility(View.GONE);
  }

  @Override
  public void onChannelDetailHide() {
    showMenuButton();
    bottomBar.setVisibility(View.VISIBLE);
  }

  @Override
  public void onSectionClick(ChannelListView clv, String caption, int locationId) {

    int _collapsed;
    if (clv == channelLV) {
      _collapsed = 0x1;
    } else if (clv == cgroupLV) {
      _collapsed = 0x2;
    } else {
      return;
    }

    Location location = getDbHelper().getLocation(locationId);
    int collapsed = location.getCollapsed();

    if ((collapsed & _collapsed) > 0) {
      collapsed ^= _collapsed;
    } else {
      collapsed |= _collapsed;
    }

    location.setCollapsed(collapsed);
    getDbHelper().updateLocation(location);

    if (clv == channelLV) {
      channelLV.Refresh(getDbHelper().getChannelListCursor(), true);
    } else {
      cgroupLV.Refresh(getDbHelper().getGroupListCursor(), true);
    }
  }

  @Override
  public void onRestApiTaskStarted(SuplaRestApiClientTask task) {}

  @Override
  public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
    if (downloadUserIcons != null) {
      if (downloadUserIcons.downloadCount() > 0) {
        if (channelLV != null) {
          channelLV.Refresh(getDbHelper().getChannelListCursor(), true);
        }
        if (cgroupLV != null) {
          cgroupLV.Refresh(getDbHelper().getGroupListCursor(), true);
        }
        reloadScenes();
      }
      downloadUserIcons = null;
    }
  }

  @Override
  public void onRestApiTaskProgressUpdate(SuplaRestApiClientTask task, Double progress) {}

  @Override
  public void onChannelStateButtonClick(ChannelListView clv, int remoteId) {

    if (channelStatePopup == null) {
      channelStatePopup = new ChannelStatePopup(this);
    }

    channelStatePopup.show(remoteId);
  }

  @Override
  public void onChannelCaptionLongClick(ChannelListView clv, int remoteId) {
    SuplaApp.Vibrate(this);

    ChannelCaptionEditor editor = new ChannelCaptionEditor(this);
    editor.edit(remoteId);
  }

  @Override
  public void onLocationCaptionLongClick(ChannelListView clv, int locationId) {
    SuplaApp.Vibrate(this);

    LocationCaptionEditor editor = new LocationCaptionEditor(this);
    editor.edit(locationId);
  }

  public void onSharedPreferenceChanged(SharedPreferences prefss, String key) {
    if (key.equals(Preferences.pref_channel_height)) {
      /* Ivalidate the adapter, so that channel list can
      be rebuilt with new layout. */
      channelListViewCursorAdapter = null;
      cgroupListViewCursorAdapter = null;
    }
  }

  @Override
  public void onProfileChanged() {
    super.onProfileChanged();
    resetListViews();
    runDownloadTask();
  }

  private interface Reorder {
    void doReorder(ListViewCursorAdapter.Item firstItem, ListViewCursorAdapter.Item secondItem);
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    if (menuIsVisible() || channelLV.isDetailSliding() || cgroupLV.isDetailSliding()) return false;

    int scenesVisible = View.GONE, groupsVisible = View.GONE, channelsVisible = View.GONE;

    switch (item.getItemId()) {
      case R.id.channels_item:
        channelsVisible = View.VISIBLE;
        break;
      case R.id.groups_item:
        groupsVisible = View.VISIBLE;
        break;
      case R.id.scenes_item:
        scenesVisible = View.VISIBLE;
        break;
    }

    channelLV.setVisibility(channelsVisible);
    cgroupLV.setVisibility(groupsVisible);
    scenesView.setVisibility(scenesVisible);

    channelsOpened = channelsVisible == View.VISIBLE;

    return true;
  }

  ScenesFragment scenesFragment() {
    FragmentManager fmgr = getSupportFragmentManager();
    return (ScenesFragment) fmgr.findFragmentById(R.id.scenesFragment);
  }

  private void reloadScenes() {
    scenesFragment().reload();
  }

}
