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
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.SuplaEvent;
import org.supla.android.listview.ChannelLayout;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.ListViewCursorAdapter;
import org.supla.android.db.DbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends NavigationActivity implements OnClickListener, ChannelListView.OnChannelButtonTouchListener, ChannelListView.OnDetailListener {

    private ChannelListView cLV;
    private ListViewCursorAdapter listViewCursorAdapter;
    private DbHelper DbH_ListView;

    private RelativeLayout NotificationView;
    private Handler notif_handler;
    private Runnable notif_nrunnable;
    private ImageView notif_img;
    private TextView notif_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // Trace.d("MainActivity", "Created!");

        notif_handler = null;
        notif_nrunnable = null;

        setContentView(R.layout.activity_main);

        NotificationView = (RelativeLayout)Inflate(R.layout.notification, null);
        NotificationView.setVisibility(View.GONE);


        RelativeLayout NotifBgLayout = (RelativeLayout)NotificationView.findViewById(R.id.notif_bg_layout);
        NotifBgLayout.setOnClickListener(this);
        NotifBgLayout.setBackgroundColor(getResources().getColor(R.color.notification_bg));

        getRootLayout().addView(NotificationView);

        notif_img = (ImageView)NotificationView.findViewById(R.id.notif_img);
        notif_text = (TextView)NotificationView.findViewById(R.id.notif_txt);

        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/OpenSans-Regular.ttf");
        notif_text.setTypeface(type);

        cLV = (ChannelListView) findViewById(R.id.channelsListView);
        cLV.setOnChannelButtonTouchListener(this);
        cLV.setOnDetailListener(this);

        DbH_ListView = new DbHelper(this);

        RegisterMessageHandler();
        showMenuBar();
        showMenuButton();

    }


    private boolean SetListCursorAdapter() {

        if ( listViewCursorAdapter == null ) {

            listViewCursorAdapter = new ListViewCursorAdapter(this, DbH_ListView.getChannelListCursor());
            cLV.setAdapter(listViewCursorAdapter);

            return true;

        } else if ( listViewCursorAdapter.getCursor() == null ) {

            listViewCursorAdapter.changeCursor(DbH_ListView.getChannelListCursor());
        }

        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cLV.hideDetail(false);
    }

    @Override
    protected void onResume() {

        super.onResume();

        if ( !SetListCursorAdapter() ) {
            cLV.setSelection(0);
            cLV.Refresh(DbH_ListView.getChannelListCursor(), true);
        }


        cLV.hideDetail(false);

        RateApp ra = new RateApp(this);
        ra.showDialog(1000);

    }

    @Override
    protected void onDestroy() {
       // Trace.d("MainActivity", "Destroyed!");
        super.onDestroy();
    }

    @Override
    protected void OnDataChangedMsg(int ChannelId) {

        if ( cLV.detail_getChannelId() == ChannelId ) {

            Channel c = cLV.detail_getChannel();

            if ( c != null && !c.getOnLine() )
                cLV.hideDetail(false);
            else
                cLV.detail_OnChannelDataChanged();

        }

        cLV.Refresh(DbH_ListView.getChannelListCursor(), false);
    }

    @Override
    protected void OnDisconnectedMsg() {

        if ( listViewCursorAdapter != null )
           listViewCursorAdapter.changeCursor(null);

    }

    @Override
    protected void OnConnectingMsg () {
        SetListCursorAdapter();
    }

    @Override
    protected void OnEventMsg(SuplaEvent event) {
        super.OnEventMsg(event);

        if ( event.Owner ) return;

        DbHelper DbH = new DbHelper(this);

        long acceddid = DbH.getCurrentAccessId();
        Channel channel = DbH.getChannel(acceddid, event.ChannelID, false);

        if ( channel == null ) return;

        int ImgIdx = ChannelLayout.getImageIdx(channel.StateUp(), channel.getFunc(), channel.getAltIcon(), 1);

        if ( ImgIdx == -1 ) return;

        String msg;

        switch(event.Event) {
            case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGATEWAYLOCK:
                msg = getResources().getString(R.string.event_openedthegateway);
                break;
            case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGATE:
                msg = getResources().getString(R.string.event_openedclosedthegate);
                break;
            case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEGARAGEDOOR:
                msg = getResources().getString(R.string.event_openedclosedthegatedoors);
                break;
            case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEDOORLOCK:
                msg = getResources().getString(R.string.event_openedthedoor);
                break;
            case SuplaConst.SUPLA_EVENT_CONTROLLINGTHEROLLERSHUTTER:
                msg = getResources().getString(R.string.event_openedcloserollershutter);
                break;
            case SuplaConst.SUPLA_EVENT_POWERONOFF:
                msg = getResources().getString(R.string.event_poweronoff);
                break;
            case SuplaConst.SUPLA_EVENT_LIGHTONOFF:
                msg = getResources().getString(R.string.event_turnedthelightonoff);
                break;
            default:
                return;
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        msg = sdf.format(new Date()) + " " + event.SenderName + " " + msg;


        if (!channel.getCaption().equals("")) {
            msg = msg + " (" + channel.getCaption()+")";
        }


        ShowNotificationMessage(msg, ImgIdx);
    }

    private void ShowHideNotificationView(final boolean show) {

        if (!show && NotificationView.getVisibility() == View.GONE )
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

    public void ShowNotificationMessage(String msg, int img) {

        notif_img.setImageResource(img);
        notif_text.setText(msg);

        ShowHideNotificationView(true);

        if ( notif_handler != null
                && notif_nrunnable != null ) {
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

        if ( v.getParent() == NotificationView ) {
            HideNotificationMessage();
        }
    }

        @Override
    public void onChannelButtonTouch(boolean left, boolean up, int channelId, int channelFunc) {


        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if ( client == null )
            return;


        if ( !up
             || channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER  )   {

            SuplaApp.Vibrate(this);
        }


        if ( up ) {

            if ( channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER )
                client.Open(channelId, 0);

        } else {

            int Open = 0;

            if ( left ) {
                Open = channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER ? 1 : 0;
            } else {
                Open = channelFunc == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER ? 2 : 1;
            }

            client.Open(channelId, Open);

        }

    }

    @Override
    public void onBackPressed() {

        if ( cLV.isDetailVisible() ) {
            cLV.hideDetail(true);
        } else {
            gotoMain();
        }

    }


    @Override
    public void onChannelDetailShow() {
        hideMenuButton();
    }

    @Override
    public void onChannelDetailHide() {
        showMenuButton();
    }

}

