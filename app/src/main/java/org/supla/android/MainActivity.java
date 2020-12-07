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
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.DbHelper;
import org.supla.android.db.Location;
import org.supla.android.images.ImageCache;
import org.supla.android.images.ImageId;
import org.supla.android.lib.SuplaChannelState;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.SuplaEvent;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.ListViewCursorAdapter;
import org.supla.android.listview.SectionLayout;
import org.supla.android.listview.draganddrop.ListViewDragListener;
import org.supla.android.restapi.DownloadUserIcons;
import org.supla.android.restapi.SuplaRestApiClientTask;
import org.supla.android.rx.SubscriptionHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.rxjava3.disposables.Disposable;

import static org.supla.android.rx.SubscriptionHelper.subscribe;

public class MainActivity extends NavigationActivity implements OnClickListener,
        ChannelListView.OnChannelButtonTouchListener,
        ChannelListView.OnDetailListener,
        SectionLayout.OnSectionLayoutTouchListener, SuplaRestApiClientTask.IAsyncResults, ChannelListView.OnChannelButtonClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ChannelListView channelLV;
    private ChannelListView cgroupLV;
    private ListViewCursorAdapter channelListViewCursorAdapter;
    private ListViewCursorAdapter cgroupListViewCursorAdapter;
    private DbHelper DbH_ListView;
    private DownloadUserIcons downloadUserIcons = null;

    private RelativeLayout NotificationView;
    private Handler notif_handler;
    private Runnable notif_nrunnable;
    private ImageView notif_img;
    private TextView notif_text;
    private ChannelStatePopup channelStatePopup;

    // Used in reordering. The initial position of taken item is saved here.
    private Integer dragInitialPosition;
    private Disposable dragUpdateDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Trace.d("MainActivity", "Created!");

        notif_handler = null;
        notif_nrunnable = null;

        setContentView(R.layout.activity_main);

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
        channelLV.setOnDetailListener(this);

        cgroupLV = findViewById(R.id.channelGroupListView);
        cgroupLV.setOnChannelButtonTouchListener(this);
        cgroupLV.setOnDetailListener(this);

        DbH_ListView = new DbHelper(this);
        new DbHelper(this, true); // For upgrade purposes

        RegisterMessageHandler();
        showMenuBar();
        showMenuButton();

    }

    @Override
    protected void onStop() {
        super.onStop();
        dragUpdateDisposable.dispose();
    }

    private boolean SetListCursorAdapter() {

        if (channelListViewCursorAdapter == null) {

            channelListViewCursorAdapter = new ListViewCursorAdapter(this, DbH_ListView.getChannelListCursor());
            channelListViewCursorAdapter.setOnSectionLayoutTouchListener(this);
            channelLV.setAdapter(channelListViewCursorAdapter);

            channelLV.setOnItemLongClickListener(this::onDragStarted);
            channelLV.setOnDragListener(new ListViewDragListener(channelLV, this::onDragStopped, this::onDragPositionChanged));

            return true;

        } else if (channelListViewCursorAdapter.getCursor() == null) {

            channelListViewCursorAdapter.changeCursor(DbH_ListView.getChannelListCursor());
        }

        return false;
    }

    private boolean onDragStarted(AdapterView<?> parent, View view, int position, long id) {
        if (channelLV.isDetailSliding()) {
            dragInitialPosition = null;
            return false;
        }
        dragInitialPosition = position;
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDrag(null, shadowBuilder, channelLV.getItemAtPosition(position), 0);
        return true;
    }

    private void onDragStopped(int droppedPosition) {
        channelListViewCursorAdapter.stopReorderingMode();
        if (dragInitialPosition == null || droppedPosition == ListViewDragListener.INVALID_POSITION) {
            // Moved somewhere outside the list view or Something wrong, initial position not initialized.
            return;
        }
        if (!channelListViewCursorAdapter.isReorderPossible(dragInitialPosition, droppedPosition)) {
            // Moving outside of the section not allowed.
            return;
        }
        ListViewCursorAdapter.Item initialPositionItem = channelListViewCursorAdapter.getItemForPosition(dragInitialPosition);
        ListViewCursorAdapter.Item finalPositionItem = channelListViewCursorAdapter.getItemForPosition(droppedPosition);

        dragUpdateDisposable = subscribe(DbH_ListView.reorderChannels(initialPositionItem, finalPositionItem),
                () -> channelListViewCursorAdapter.changeCursor(DbH_ListView.getChannelListCursor()),
                throwable -> Log.w(TAG, "Channels reordering failed", throwable));
    }

    private void onDragPositionChanged(int position) {
        if (dragInitialPosition == null || position == ListViewDragListener.INVALID_POSITION) {
            // Moved somewhere outside the list view or Something wrong, initial position not initialized.
            channelListViewCursorAdapter.stopReorderingMode();
            return;
        }
        if (!channelListViewCursorAdapter.isReorderPossible(dragInitialPosition, position)) {
            // Moving outside of the section not allowed.
            channelListViewCursorAdapter.stopReorderingMode();
            return;
        }
        channelListViewCursorAdapter.updateReorderingMode(dragInitialPosition, position);
    }

    private boolean SetGroupListCursorAdapter() {

        if (cgroupListViewCursorAdapter == null) {

            cgroupListViewCursorAdapter = new ListViewCursorAdapter(this, DbH_ListView.getGroupListCursor(), true);
            cgroupListViewCursorAdapter.setOnSectionLayoutTouchListener(this);
            cgroupLV.setAdapter(cgroupListViewCursorAdapter);

            return true;

        } else if (cgroupListViewCursorAdapter.getCursor() == null) {

            cgroupListViewCursorAdapter.changeCursor(DbH_ListView.getGroupListCursor());
        }

        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        channelLV.hideDetail(false);
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!SetListCursorAdapter()) {
            channelLV.setSelection(0);
            channelLV.Refresh(DbH_ListView.getChannelListCursor(), true);
        }

        if (!SetGroupListCursorAdapter()) {
            cgroupLV.setSelection(0);
            cgroupLV.Refresh(DbH_ListView.getGroupListCursor(), true);
        }

        if (channelLV.getVisibility() == View.VISIBLE) {
            channelLV.hideDetail(false);
        } else {
            cgroupLV.hideDetail(false);
        }

        runDownloadTask();

        RateApp ra = new RateApp(this);
        ra.showDialog(1000);

    }

    @Override
    protected void onDestroy() {
        // Trace.d("MainActivity", "Destroyed!");
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

        ChannelListView LV = null;
        int Id = 0;

        if (ChannelId > 0) {
            Id = ChannelId;
            LV = channelLV;
        } else if (GroupId > 0) {
            Id = GroupId;
            LV = cgroupLV;
        }

        if (LV != null) {

            if (LV.detail_getRemoteId() == Id) {

                ChannelBase cbase = LV.detail_getChannel();

                if (cbase != null && !cbase.getOnLine())
                    LV.hideDetail(false, true);
                else
                    LV.detail_OnChannelDataChanged();
            }

            LV.Refresh(LV == channelLV ? DbH_ListView.getChannelListCursor() :
                    DbH_ListView.getGroupListCursor(), true);

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
    }

    @Override
    protected void onDisconnectedMsg() {

        if (channelListViewCursorAdapter != null)
            channelListViewCursorAdapter.changeCursor(null);

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

        DbHelper DbH = new DbHelper(this);

        Channel channel = DbH.getChannel(event.ChannelID);
        if (channel == null) return;


        int imgResId = 0;
        ImageId imgId = null;
        String msg = "";

        if (event.Event == SuplaConst.SUPLA_EVENT_SET_BRIDGE_VALUE_FAILED) {
            if ((channel.getFlags() & SuplaConst.SUPLA_CHANNEL_FLAG_ZWAVE_BRIDGE) > 0) {
                msg = getResources().getString(R.string.zwave_device_communication_error);
                imgResId = R.drawable.zwave_device_error;
            } else {
                return;
            }
        } else {
            int msgId = 0;
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

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            msg = sdf.format(new Date()) + " " + event.SenderName + " " + msg;
        }

        if (!channel.getCaption().equals("")) {
            msg = msg + " (" + channel.getCaption() + ")";
        }


        ShowNotificationMessage(msg, imgId, imgResId);

    }

    private void ShowHideNotificationView(final boolean show) {

        if (!show && NotificationView.getVisibility() == View.GONE)
            return;

        float height = getResources().getDimension(R.dimen.channel_layout_height);

        NotificationView.setVisibility(View.VISIBLE);
        NotificationView.bringToFront();
        NotificationView.setTranslationY(show ? height : 0);

        NotificationView.animate()
                .translationY(show ? 0 : height)
                .setDuration(100)
                .setListener(new AnimatorListenerAdapter() {
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

        if (notif_handler != null
                && notif_nrunnable != null) {
            notif_handler.removeCallbacks(notif_nrunnable);
        }

        notif_handler = new Handler();
        notif_nrunnable = new Runnable() {
            @Override
            public void run() {

                HideNotificationMessage();

                notif_handler = null;
                notif_nrunnable = null;
            }
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

    @Override
    protected void onGroupButtonTouch(boolean On) {
        if (On) {
            channelLV.setVisibility(View.GONE);
            cgroupLV.setVisibility(View.VISIBLE);
        } else {
            channelLV.setVisibility(View.VISIBLE);
            cgroupLV.setVisibility(View.GONE);
        }
    }

    private void ShowValveAlertDialog(final int channelId, final Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(android.R.string.dialog_alert_title);
        builder.setMessage(R.string.valve_open_warning);

        builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SuplaClient client = SuplaApp.getApp().getSuplaClient();
                        if (client != null) {
                            SuplaApp.Vibrate(context);
                            client.open(channelId, false, 1);
                        }
                        dialog.cancel();
                    }
                });

        builder.setNeutralButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onChannelButtonTouch(ChannelListView clv, boolean left, boolean up, int remoteId, int channelFunc) {


        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        clv.hideButton(false);

        if (client == null)
            return;

        if (!left && !up && (channelFunc == SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE
                || channelFunc == SuplaConst.SUPLA_CHANNELFNC_VALVE_PERCENTAGE)) {
            DbHelper dbH = new DbHelper(this);
            Channel channel = dbH.getChannel(remoteId);
            if (channel != null
                    && channel.getValue().isClosed()
                    && (channel.getValue().flooding()
                    || channel.getValue().isManuallyClosed())) {
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
                Open = channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
                        || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
                        ? 1 : 0;
            } else {
                Open = channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
                        || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
                        ? 2 : 1;
            }

            client.open(remoteId, clv == cgroupLV, Open);

        }

    }

    @Override
    public void onBackPressed() {

        if (channelLV.isDetailVisible()) {
            channelLV.onBackPressed();
        } else if (cgroupLV.isDetailVisible()) {
            cgroupLV.onBackPressed();
        } else {
            gotoMain();
        }

    }


    @Override
    public void onChannelDetailShow(ChannelBase channel) {
        setMenubarDetailTitle(channel.getNotEmptyCaption(this));
        showBackButton();
    }

    @Override
    public void onChannelDetailHide() {
        showMenuButton();
    }

    @Override
    public void onSectionLayoutTouch(Object sender, String caption, int locationId) {

        int _collapsed;
        DbHelper dbHelper = new DbHelper(this);

        if (sender == channelLV.getAdapter()) {
            _collapsed = 0x1;
        } else if (sender == cgroupLV.getAdapter()) {
            _collapsed = 0x2;
        } else {
            return;
        }

        Location location = dbHelper.getLocation(locationId);
        int collapsed = location.getCollapsed();

        if ((collapsed & _collapsed) > 0) {
            collapsed ^= _collapsed;
        } else {
            collapsed |= _collapsed;
        }

        location.setCollapsed(collapsed);
        dbHelper.updateLocation(location);

        if (sender == channelLV.getAdapter()) {
            channelLV.Refresh(DbH_ListView.getChannelListCursor(), true);
        } else {
            cgroupLV.Refresh(DbH_ListView.getGroupListCursor(), true);
        }

    }

    @Override
    public void onRestApiTaskStarted(SuplaRestApiClientTask task) {

    }

    @Override
    public void onRestApiTaskFinished(SuplaRestApiClientTask task) {
        if (downloadUserIcons != null) {
            if (downloadUserIcons.downloadCount() > 0) {
                if (channelLV != null) {
                    channelLV.Refresh(DbH_ListView.getChannelListCursor(), true);
                }

                if (cgroupLV != null) {
                    cgroupLV.Refresh(DbH_ListView.getGroupListCursor(), true);
                }
            }
            downloadUserIcons = null;
        }
    }

    @Override
    public void onRestApiTaskProgressUpdate(SuplaRestApiClientTask task, Double progress) {

    }

    @Override
    public void onChannelStateButtonClick(ChannelListView clv, int remoteId) {

        if (channelStatePopup == null) {
            channelStatePopup = new ChannelStatePopup(this);
        }

        channelStatePopup.show(remoteId);
    }

    @Override
    public void onChannelWarningButtonClick(ChannelListView clv, int remoteId) {
        DbHelper dbH = new DbHelper(this);
        Channel channel = dbH.getChannel(remoteId);
        if (channel != null) {
            String warning = channel.getChannelWarningMessage(this);
            if (warning != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(android.R.string.dialog_alert_title);
                builder.setMessage(warning);

                builder.setNeutralButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }
}

