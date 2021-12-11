package org.supla.android.listview;

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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.widget.AppCompatTextView;
import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.SuplaChannelStatus;
import org.supla.android.SuplaWarningIcon;
import org.supla.android.ViewHelper;
import org.supla.android.Preferences;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ChannelValue;
import org.supla.android.images.ImageCache;
import org.supla.android.images.ImageId;
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaConst;

public class ChannelLayout extends LinearLayout implements View.OnLongClickListener {


    private int mRemoteId;
    private int mFunc;
    private boolean mMeasurementSubChannel;
    private boolean mGroup;

    private ChannelListView mParentListView;
    private RelativeLayout content;
    private FrameLayout right_btn;
    private FrameLayout left_btn;

    private RelativeLayout channelIconContainer;

    private ChannelImageLayout imgl;

    private TextView left_btn_text;
    private TextView right_btn_text;
    private CaptionView caption_text;

    private SuplaChannelStatus right_onlineStatus;
    private SuplaChannelStatus right_ActiveStatus;
    private SuplaChannelStatus left_onlineStatus;
    private ImageView channelStateIcon;
    private SuplaWarningIcon channelWarningIcon;

    private LineView bottom_line;

    private boolean Anim;

    private boolean RightButtonEnabled;
    private boolean LeftButtonEnabled;
    private boolean DetailSliderEnabled;

    private float heightScaleFactor = 1f;
    private boolean shouldUpdateChannelStateLayout;
    
    private Preferences prefs;


    public ChannelLayout(Context context, ChannelListView parentListView) {
        super(context);

        prefs = new Preferences(context);
        setOrientation(LinearLayout.HORIZONTAL);

        mParentListView = parentListView;
        setBackgroundColor(getResources().getColor(R.color.channel_cell));

        right_btn = new FrameLayout(context);
        left_btn = new FrameLayout(context);

        shouldUpdateChannelStateLayout = true;

        heightScaleFactor = (prefs.getChannelHeight() + 0f) / 100f;
        int channelHeight = (int)(((float)getResources().getDimensionPixelSize(R.dimen.channel_layout_height)) * heightScaleFactor);

        right_btn.setLayoutParams(new LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.channel_layout_button_width), channelHeight));

        right_btn.setBackgroundColor(getResources().getColor(R.color.channel_btn));

        left_btn.setLayoutParams(new LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.channel_layout_button_width), channelHeight));

        left_btn.setBackgroundColor(getResources().getColor(R.color.channel_btn));

        content = new RelativeLayout(context);
        content.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, channelHeight));

        content.setBackgroundColor(getResources().getColor(R.color.channel_cell));

        addView(content);
        addView(left_btn);
        addView(right_btn);

        left_btn_text = newTextView(context);
        left_btn.addView(left_btn_text);

        right_btn_text = newTextView(context);
        right_btn.addView(right_btn_text);

        right_onlineStatus = newOnlineStatus(context, true);
        right_onlineStatus.setId(ViewHelper.generateViewId());
        content.addView(right_onlineStatus);
        left_onlineStatus = newOnlineStatus(context, false);
        left_onlineStatus.setId(ViewHelper.generateViewId());
        content.addView(left_onlineStatus);

        channelIconContainer = new RelativeLayout(context);
        content.addView(channelIconContainer);
        channelIconContainer
            .setLayoutParams(getChannelIconContainerLayoutParams());

        channelStateIcon = new ImageView(context);
        channelStateIcon.setId(ViewHelper.generateViewId());
        content.addView(channelStateIcon);
        channelStateIcon.setLayoutParams(getChannelStateImageLayoutParams());

        channelWarningIcon = new SuplaWarningIcon(context);
        channelWarningIcon.setId(ViewHelper.generateViewId());
        content.addView(channelWarningIcon);
        channelWarningIcon.setLayoutParams(getChannelWarningImageLayoutParams());

        right_ActiveStatus = new SuplaChannelStatus(context);
        right_ActiveStatus.setSingleColor(true);
        right_ActiveStatus.setOnlineColor(getResources().getColor(R.color.channel_dot_on));

        {
            int dot_size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    dot_size / 2, dot_size * 2);

            lp.addRule(RelativeLayout.LEFT_OF, right_onlineStatus.getId());
            lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            right_ActiveStatus.setLayoutParams(lp);
        }

        right_ActiveStatus.setVisibility(View.GONE);
        content.addView(right_ActiveStatus);

        bottom_line = new LineView(context);
        content.addView(bottom_line);

        imgl = new ChannelImageLayout(context, heightScaleFactor);
        channelIconContainer.addView(imgl);

        caption_text = new CaptionView(context, imgl.getId(), heightScaleFactor);
        caption_text.setOnLongClickListener(this);
        channelIconContainer.addView(caption_text);

        OnTouchListener tl = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if (action == MotionEvent.ACTION_DOWN)
                    onActionBtnTouchDown(v);
                else if (action == MotionEvent.ACTION_UP)
                    onActionBtnTouchUp(v);

                return true;
            }
        };

        left_btn.setOnTouchListener(tl);
        right_btn.setOnTouchListener(tl);

        right_onlineStatus.setVisibility(INVISIBLE);
        left_onlineStatus.setVisibility(INVISIBLE);
    }


    public ChannelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private RelativeLayout.LayoutParams getChannelIconContainerLayoutParams() {
        RelativeLayout.LayoutParams lp;

        lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                             RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        return lp;
    }

    private TextView newTextView(Context context) {

        TextView tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        tv.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.channel_btn_text_size));
        tv.setTextColor(getResources().getColor(R.color.channel_btn_text));
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        return tv;
    }

    protected RelativeLayout.LayoutParams getOnlineStatusLayoutParams(boolean right) {

        int dot_size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                mGroup ? dot_size / 2 : dot_size, mGroup ? dot_size * 2 : dot_size);

        int margin = getResources().getDimensionPixelSize(R.dimen.channel_dot_margin);

        if (right) {
            lp.rightMargin = margin;
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        } else {
            lp.leftMargin = margin;
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        }

        lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        return lp;
    }

    protected RelativeLayout.LayoutParams getChannelStateImageLayoutParams() {

        int size = getResources().getDimensionPixelSize(R.dimen.channel_state_image_size);
        int margin = getResources().getDimensionPixelSize(R.dimen.channel_dot_margin);

        if(mFunc == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE)
            margin = 0;
            
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(size, size);
        lp.leftMargin = margin;

        lp.addRule(RelativeLayout.RIGHT_OF, left_onlineStatus.getId());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            lp.addRule(RelativeLayout.END_OF, left_onlineStatus.getId());
        }

        lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        return lp;
    }

    protected RelativeLayout.LayoutParams getChannelWarningImageLayoutParams() {

        int size = getResources().getDimensionPixelSize(R.dimen.channel_warning_image_size);
        int margin = getResources().getDimensionPixelSize(R.dimen.channel_dot_margin);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(size, size);
        lp.rightMargin = margin;

        lp.addRule(RelativeLayout.LEFT_OF, right_onlineStatus.getId());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            lp.addRule(RelativeLayout.START_OF, right_onlineStatus.getId());
        }

        lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        return lp;
    }

    protected SuplaChannelStatus newOnlineStatus(Context context, boolean right) {

        SuplaChannelStatus result = new SuplaChannelStatus(context);

        result.setLayoutParams(getOnlineStatusLayoutParams(right));
        result.setOfflineColor(getResources().getColor(R.color.channel_dot_off));
        result.setOnlineColor(getResources().getColor(R.color.channel_dot_on));

        return result;
    }

    public void setLeftBtnText(String Text) {
        left_btn_text.setText(Text);
    }

    public void setRightBtnText(String Text) {
        right_btn_text.setText(Text);
    }

    public void Slide(int delta) {

        if (Anim)
            return;

        if (!LeftButtonEnabled
                && delta > 0 && content.getLeft() + delta > 0)
            delta = content.getLeft() * -1;

        if (!RightButtonEnabled
                && delta < 0 && content.getLeft() + delta < 0)
            delta = content.getLeft() * -1;

        content.layout(content.getLeft() + delta, content.getTop(), content.getWidth() + content.getLeft() + delta, content.getHeight());

        int bcolor = getResources().getColor(R.color.channel_btn);

        left_btn.setBackgroundColor(bcolor);
        right_btn.setBackgroundColor(bcolor);

        UpdateLeftBtn();
        UpdateRightBtn();

    }

    public void hideButtonImmediately() {
        if (Slided() > 0) {
            Slide(content.getLeft() * -1);
        }
    }

    public float percentOfSliding() {
        if (content.getLeft() < 0)
            return right_btn.getWidth() > 0 ?
                    content.getLeft() * -100f / right_btn.getWidth() : 0;

        return left_btn.getWidth() > 0 ?
                content.getLeft() * 100f / left_btn.getWidth() : 0;
    }

    public boolean Sliding() {
        return Anim ||  percentOfSliding() > 0;
    }

    public int Slided() {

        if (Anim)
            return 10;

        if (content.getLeft() > 0)
            return content.getLeft() == right_btn.getWidth() ? 100 : 1;

        if (content.getLeft() < 0)
            return content.getLeft() == left_btn.getWidth()* -1 ? 200 : 2;

        return 0;
    }

    private void UpdateLeftBtn() {

        float pr = content.getLeft() * 100 / left_btn.getWidth();

        if (pr <= 0) pr = 0;
        else if (pr > 100) pr = 100;

        left_btn.setRotationY(90 - 90 * pr / 100);

        int left = content.getLeft() / 2 - left_btn.getWidth() / 2;
        int right = left_btn.getWidth() + (content.getLeft() / 2 - left_btn.getWidth() / 2);

        if (left > 0) left = 0;
        if (right > left_btn.getWidth()) right = left_btn.getWidth();

        left_btn.layout(left, 0, right, left_btn.getHeight());

    }

    private void onActionBtnTouchUpDown(boolean up, View v) {
        if (mParentListView != null
                && mParentListView.getOnChannelButtonTouchListener() != null) {
            mParentListView.getOnChannelButtonTouchListener().onChannelButtonTouch(mParentListView, v == left_btn, up, mRemoteId, mFunc);
        }

    }

    private void onActionBtnTouchDown(View v) {
        if (Slided() == 0) {
            return;
        }

        if (v == left_btn || v == right_btn) {
            v.setBackgroundColor(getResources().getColor(R.color.channel_btn_pressed));
        }


        onActionBtnTouchUpDown(false, v);
    }

    private void onActionBtnTouchUp(View v) {
        if (Slided() == 0) {
            return;
        }

        if (v == left_btn || v == right_btn) {

            final View _v = v;

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    _v.setBackgroundColor(getResources().getColor(R.color.channel_btn));
                }
            }, 200);

        }


        onActionBtnTouchUpDown(true, v);
    }

    private void UpdateRightBtn() {

        float pr = (content.getLeft() * -1) * 100 / right_btn.getWidth();

        if (pr <= 0) pr = 0;
        else if (pr > 100) pr = 100;

        right_btn.setRotationY(-90 + 90 * pr / 100);

        int left = getWidth() + (content.getLeft() / 2 - right_btn.getWidth() / 2);

        if (content.getLeft() * -1 > right_btn.getWidth())
            left = getWidth() - right_btn.getWidth();

        right_btn.layout(left, 0, left + right_btn.getWidth(), right_btn.getHeight());

    }

    public void AnimateToRestingPosition(boolean start_pos) {

        if (!start_pos
                && Anim) return;


        ObjectAnimator btn_animr = null;
        ObjectAnimator btn_animx = null;
        ObjectAnimator content_animx = null;

        final AnimParams params = new AnimParams();

        params.left_btn_rotation = 90;
        params.left_btn_left = left_btn.getWidth() * -1;
        params.left_btn_right = 0;

        if (content.getLeft() > 0) {

            if (!start_pos
                    && content.getLeft() >= left_btn.getWidth() / 2) {

                params.content_left = left_btn.getWidth();
                params.content_right = getWidth() + left_btn.getWidth();

                btn_animr = ObjectAnimator.ofFloat(left_btn, "RotationY", left_btn.getRotationY(), 0f);
                btn_animx = ObjectAnimator.ofFloat(left_btn, "x", left_btn.getLeft(), 0);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), params.content_left);

            } else {

                params.content_left = 0;
                params.content_right = content.getWidth();

                btn_animr = ObjectAnimator.ofFloat(left_btn, "RotationY", left_btn.getRotationY(), 90f);
                btn_animx = ObjectAnimator.ofFloat(left_btn, "x", left_btn.getLeft(), left_btn.getWidth() / 2 * -1);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), 0f);

            }

        } else if (content.getLeft() < 0) {

            if (!start_pos
                    && content.getLeft() * -1 >= right_btn.getWidth() / 2) {

                params.content_left = right_btn.getWidth() * -1;
                params.content_right = getWidth() - right_btn.getWidth();

                btn_animr = ObjectAnimator.ofFloat(right_btn, "RotationY", right_btn.getRotationY(), 0f);
                btn_animx = ObjectAnimator.ofFloat(right_btn, "x", right_btn.getLeft(), params.content_right);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), params.content_left);

            } else {

                params.content_left = 0;
                params.content_right = content.getWidth();

                btn_animr = ObjectAnimator.ofFloat(right_btn, "RotationY", right_btn.getRotationY(), -90f);
                btn_animx = ObjectAnimator.ofFloat(right_btn, "x", right_btn.getLeft(), getWidth() + right_btn.getWidth() / 2);
                content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), 0f);

            }

        }


        if (content_animx != null) {

            AnimatorSet as = new AnimatorSet();
            as.playTogether(btn_animr, btn_animx, content_animx);
            as.setDuration(200);

            as.addListener(new Animator.AnimatorListener() {


                @Override
                public void onAnimationStart(Animator animation) {
                    Anim = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    content.setTranslationX(0);
                    content.layout(params.content_left, content.getTop(), params.content_right, getWidth());

                    left_btn.setTranslationX(0);
                    right_btn.setTranslationX(0);
                    UpdateLeftBtn();
                    UpdateRightBtn();
                    Anim = false;

                    if (mParentListView != null)
                        mParentListView.onSlideAnimationEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            as.start();
        }

    }

    public boolean getRightButtonEnabled() {
        return RightButtonEnabled;
    }

    private void setRightButtonEnabled(boolean rightButtonEnabled) {

        if (RightButtonEnabled != rightButtonEnabled) {
            AnimateToRestingPosition(true);
            RightButtonEnabled = rightButtonEnabled;
        }

    }

    private void setLeftButtonEnabled(boolean leftButtonEnabled) {

        if (LeftButtonEnabled != leftButtonEnabled) {
            AnimateToRestingPosition(true);
            LeftButtonEnabled = leftButtonEnabled;
        }

    }

    public boolean getDetailSliderEnabled() {
		if(RightButtonEnabled) {
			// Only enable detail slider if right button is
			// already expanded.
			if(Slided() == 200)
				return DetailSliderEnabled;
			else
				return false;
		} else {
			return DetailSliderEnabled;
		}
    }

    private void setDetailSliderEnabled(boolean detailSliderEnabled) {
        DetailSliderEnabled = detailSliderEnabled;
    }

    public boolean getButtonsEnabled() {
        return LeftButtonEnabled || RightButtonEnabled;
    }

    public String getCaption() {
        return caption_text.getText().toString();
    }

    public void setBackgroundColor(int color) {

        super.setBackgroundColor(color);

        if (content != null)
            content.setBackgroundColor(color);
    }

    public void setChannelData(ChannelBase cbase) {

        int OldFunc = mFunc;
        mFunc = cbase.getFunc();
        mRemoteId = cbase.getRemoteId();
        boolean OldGroup = mGroup;
        mGroup = cbase instanceof ChannelGroup;

        

        imgl.setImage(cbase.getImageIdx(ChannelBase.WhichOne.First),
                cbase.getImageIdx(ChannelBase.WhichOne.Second));

        imgl.setText1(cbase.getHumanReadableValue());
        imgl.setText2(cbase.getHumanReadableValue(ChannelBase.WhichOne.Second));

        channelStateIcon.setVisibility(INVISIBLE);
        channelWarningIcon.setChannel(cbase);

        boolean _mMeasurementSubChannel = !mGroup
                && (((Channel)cbase).getValue().getSubValueType()
                == SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
                || ((Channel)cbase).getValue().getSubValueType()
                == SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS);


        if (OldFunc != mFunc || _mMeasurementSubChannel != mMeasurementSubChannel) {
            mMeasurementSubChannel = _mMeasurementSubChannel;
            imgl.SetDimensions();
            shouldUpdateChannelStateLayout = true;
        }

        {

            SuplaChannelStatus.ShapeType shapeType = mGroup ?
                    SuplaChannelStatus.ShapeType.LinearVertical : SuplaChannelStatus.ShapeType.Dot;

            if (mGroup != OldGroup) {
                left_onlineStatus.setLayoutParams(getOnlineStatusLayoutParams(false));
                right_onlineStatus.setLayoutParams(getOnlineStatusLayoutParams(true));
            }

            left_onlineStatus.setPercent(cbase.getOnLinePercent());
            left_onlineStatus.setShapeType(shapeType);
            right_onlineStatus.setPercent(cbase.getOnLinePercent());
            right_onlineStatus.setShapeType(shapeType);
        }

        int activePercent;

        if (mGroup
                && (activePercent = ((ChannelGroup) cbase).getActivePercent()) >= 0) {
            right_ActiveStatus.setVisibility(View.VISIBLE);
            right_ActiveStatus.setPercent(activePercent);
        } else {
            right_ActiveStatus.setVisibility(View.GONE);
            int stateIcon = 0;

            if (cbase instanceof Channel && prefs.isShowChannelInfo()) {
                stateIcon = ((Channel) cbase).getChannelStateIcon();
            }

            if (stateIcon != 0) {
                channelStateIcon.setImageResource(stateIcon);
                if(shouldUpdateChannelStateLayout) {
                    channelStateIcon.setLayoutParams(getChannelStateImageLayoutParams());
                    shouldUpdateChannelStateLayout = false;
                }
                channelStateIcon.setVisibility(VISIBLE);
            }
        }

        {
            int lidx = -1;
            int ridx = -1;

            switch (mFunc) {
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:

                    ridx = R.string.channel_btn_open;
                    break;

                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:

                    ridx = R.string.channel_btn_openclose;
                    break;

                case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:

                    ridx = R.string.channel_btn_on;
                    lidx = R.string.channel_btn_off;
                    break;

                case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
                    ridx = R.string.channel_btn_open;
                    lidx = R.string.channel_btn_close;
                    break;

            }

            setRightBtnText(ridx == -1 ? "" : getResources().getString(ridx));
            setLeftBtnText(lidx == -1 ? "" : getResources().getString(lidx));
        }

        {
            boolean lenabled = false;
            boolean renabled = false;
            boolean dslider = false;

            switch (mFunc) {
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:

                    left_onlineStatus.setVisibility(View.INVISIBLE);
                    right_onlineStatus.setVisibility(View.VISIBLE);

                    renabled = true;

                    break;
                case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
                case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:

                    left_onlineStatus.setVisibility(View.VISIBLE);
                    right_onlineStatus.setVisibility(View.VISIBLE);

                    lenabled = true;
                    renabled = true;

                    if (cbase instanceof Channel && ((Channel)cbase).getValue() != null) {
                        if (((Channel)cbase).getValue().getSubValueType()
                                == SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS
                                || ((Channel)cbase).getValue().getSubValueType()
                                == SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS ) {
                            dslider = true;
                        }
                    }

                    break;
                case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:

                    left_onlineStatus.setVisibility(View.VISIBLE);
                    right_onlineStatus.setVisibility(View.VISIBLE);

                    lenabled = true;
                    renabled = true;
                    break;

                case SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:
                case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW:
                case SuplaConst.SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW:
                case SuplaConst.SUPLA_CHANNELFNC_MAILSENSOR:

                    left_onlineStatus.setVisibility(View.VISIBLE);
                    left_onlineStatus.setShapeType(SuplaChannelStatus.ShapeType.Ring);
                    right_onlineStatus.setVisibility(View.VISIBLE);
                    right_onlineStatus.setShapeType(SuplaChannelStatus.ShapeType.Ring);

                    break;

                case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                case SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER:
                case SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER:
                case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
                case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
                case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT:
                case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
                case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL:
                case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
                case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:

                    left_onlineStatus.setVisibility(View.INVISIBLE);
                    right_onlineStatus.setVisibility(View.VISIBLE);
                    dslider = true;
                    break;

                default:
                    left_onlineStatus.setVisibility(View.INVISIBLE);
                    right_onlineStatus.setVisibility(View.INVISIBLE);
                    break;
            }

            setLeftButtonEnabled(lenabled && cbase.getOnLine());
            setRightButtonEnabled(renabled && cbase.getOnLine());
            setDetailSliderEnabled(dslider && cbase.getOnLine());


        }
        caption_text.setText(cbase.getNotEmptyCaption(getContext()));

    }

    @Override
    public boolean onLongClick(View v) {
        if (mParentListView.getOnCaptionLongClickListener() != null) {
            mParentListView.getOnCaptionLongClickListener().
                    onChannelCaptionLongClick(mParentListView, mRemoteId);
        }
        return true;
    }

    private class AnimParams {
        public int content_left;
        public int content_right;
        public int left_btn_rotation;
        public int left_btn_left;
        public int left_btn_right;
    }


    class CaptionView extends androidx.appcompat.widget.AppCompatTextView {


        public CaptionView(Context context, int imgl_id, float heightScaleFactor) {
            super(context);
            float textSize = getResources().getDimension(R.dimen.channel_caption_text_size);
            if(heightScaleFactor > 1.0) textSize *= heightScaleFactor;
            setTypeface(SuplaApp.getApp().getTypefaceOpenSansBold());
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            setTextColor(getResources().getColor(R.color.channel_caption_text));
            setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                                                             LayoutParams.WRAP_CONTENT);

            if (imgl_id != -1)
                lp.addRule(RelativeLayout.BELOW, imgl_id);

            lp.topMargin = (int)(getResources().getDimensionPixelSize(R.dimen.channel_caption_top_margin)
                                 * heightScaleFactor);
            setLayoutParams(lp);
        }

    }

    private class ChannelImageLayout extends LinearLayout {

        private ImageView Img1;
        private ImageView Img2;
        private ImageId Img1Id;
        private ImageId Img2Id;
        private TextView Text1;
        private TextView Text2;
        private float heightScaleFactor = 1f;
        private int mOldFunc;

        public ChannelImageLayout(Context context, float heightScaleFactor) {
            super(context);


            this.heightScaleFactor = heightScaleFactor;

            setId(ViewHelper.generateViewId());
            mFunc = 0; mOldFunc = 0;
            Img1 = newImageView(context);
            Text1 = newTextView(context);

            Img2 = newImageView(context);
            Text2 = newTextView(context);

            configureSubviews();
            SetDimensions();
        }

        private void configureSubviews() {
            removeAllViews();
            if(mFunc ==  SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR) {
                setOrientation(LinearLayout.VERTICAL);
                addView(Text1);
                addView(Img1);
                addView(Text2);
                addView(Img2);
            } else {
                setOrientation(LinearLayout.HORIZONTAL);
                addView(Img1);
                addView(Text1);
                addView(Img2);
                addView(Text2);
            }
        }

        private ImageView newImageView(Context context) {

            ImageView Img = new ImageView(context);
            Img.setId(ViewHelper.generateViewId());
            Img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            return Img;
        }

        private int scaledDimension(int dim) {
            return (int)(dim * heightScaleFactor);
        }

        private TextView newTextView(Context context) {

            AppCompatTextView Text = new AppCompatTextView(context);
            Text.setId(ViewHelper.generateViewId());

            Text.setTypeface(SuplaApp.getApp().getTypefaceOpenSansRegular());

            float textSize = getResources().getDimension(R.dimen.channel_imgtext_size);
            float sts = scaledDimension((int)textSize);
            textSize = (sts>textSize)?sts:textSize;
            Text.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            Text.setMaxLines(1);

            Text.setTextColor(getResources().getColor(R.color.channel_imgtext_color));
            Text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);


            return Text;
        }

        private void SetTextDimensions(TextView Text, ImageView Img,
                                       Boolean visible) {
            int h = getResources().getDimensionPixelSize(R.dimen.channel_img_height);
            int sh = scaledDimension(h);

            boolean empty = Text.getText().length() == 0;

            Text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, sh);

            int textMargin = empty?0:getResources().getDimensionPixelSize(R.dimen.channel_imgtext_leftmargin);
            lp.setMargins(textMargin, 0, 0, 0);
            Text.setLayoutParams(lp);
            Text.setVisibility(visible ? View.VISIBLE : View.GONE);


        }

        private void SetImgDimensions(ImageView Img, int width, int height) {
			int sw = scaledDimension(width),
				sh = scaledDimension(height);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sw, sh);

            if (Img == Img2) {
				int textMargin = getResources().getDimensionPixelSize(R.dimen.channel_imgtext_leftmargin);
				lp.setMargins(2 * textMargin, 0, 0, 0);
            }

            Img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Img.setLayoutParams(lp);

        }

        private void SetImgDimensions(ImageView Img) {
            SetImgDimensions(Img,
                    getResources().getDimensionPixelSize(R.dimen.channel_img_width),
                    getResources().getDimensionPixelSize(R.dimen.channel_img_height));
        }

        private void SetDimensions() {
            if(mOldFunc != mFunc) {
                mOldFunc = mFunc;
                configureSubviews();
            }
            setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

            int h = getResources().getDimensionPixelSize(R.dimen.channel_img_height),
                sh = scaledDimension(h);

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
			      LayoutParams.WRAP_CONTENT, sh);
            
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            
            setLayoutParams(lp);

            if (mFunc == SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR) {

                int sdw, sdh, dh, dw;

                dw = getResources().getDimensionPixelSize(R.dimen.channel_distanceimg_width);
                dh = getResources().getDimensionPixelSize(R.dimen.channel_distanceimg_height);
                sdw = scaledDimension(dw);
                sdh = scaledDimension(dh);

                LinearLayout.LayoutParams _lp = new LinearLayout.LayoutParams(sdw, sdh>dh?sdh:dh);
                Img1.setLayoutParams(_lp);
                Img1.setVisibility(View.VISIBLE);

                _lp = new LinearLayout.LayoutParams(
                                                    scaledDimension(getResources().getDimensionPixelSize(R.dimen.channel_distanceimgtext_width)),
                                                    LayoutParams.WRAP_CONTENT);

                Text1.setLayoutParams(_lp);
                Text1.setVisibility(View.VISIBLE);
                Img2.setVisibility(View.GONE);
                Text2.setVisibility(View.GONE);
            } else {
                SetTextDimensions(Text1, Img1, true);
                SetImgDimensions(Img1);
                SetImgDimensions(Img2);
                SetTextDimensions(Text2, Img2, mFunc == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE);
            }
        }

        public void setImage(ImageId img1Id, ImageId img2Id) {

            if (ImageId.equals(img1Id, Img1Id)
                    && ImageId.equals(img2Id, Img2Id)) {
                return;
            }

            Img1Id = img1Id;
            Img2Id = img2Id;

            if (Img1Id == null) {
                Img1.setVisibility(View.GONE);
            } else {
                Img1.setImageBitmap(ImageCache.getBitmap(getContext(), img1Id));
                Img1.setVisibility(View.VISIBLE);
            }

            if (Img2Id == null) {
                Img2.setVisibility(View.GONE);
            } else {
                Img2.setImageBitmap(ImageCache.getBitmap(getContext(), img2Id));
                Img2.setVisibility(View.VISIBLE);
            }

        }

        private void setText(CharSequence text, TextView tv) {
            if (text == null) {
                text = "";
            }
            if (!text.equals(tv.getText())) {
                tv.setText(text);
            }
        }

        public void setText1(CharSequence text) {
            setText(text, Text1);
        }

        public void setText2(CharSequence text) {
            setText(text, Text2);
        }
    }

    private boolean iconTouched(int x, int y, ImageView icon) {
        if (icon.getVisibility() == VISIBLE) {
            Rect rect1 = new Rect();
            Rect rect2 = new Rect();

            getHitRect(rect1);
            icon.getHitRect(rect2);

            rect2.left += rect1.left;
            rect2.right += rect1.left;
            rect2.top += rect1.top;
            rect2.bottom += rect1.top;

            return rect2.contains(x, y);
        }

        return false;
    }

    public Point stateIconTouched(int x, int y) {
        return iconTouched(x, y, channelStateIcon) ? new Point(x,y) : null;
    }

    public int getRemoteId() {
        return mRemoteId;
    }
}
