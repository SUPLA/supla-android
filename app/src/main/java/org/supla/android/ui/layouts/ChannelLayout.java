package org.supla.android.ui.layouts;

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
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Date;
import javax.inject.Inject;
import org.supla.android.Preferences;
import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.SuplaChannelStatus;
import org.supla.android.SuplaWarningIcon;
import org.supla.android.ViewHelper;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelExtendedValue;
import org.supla.android.db.ChannelGroup;
import org.supla.android.events.ListsEventsManager;
import org.supla.android.images.ImageCache;
import org.supla.android.images.ImageId;
import org.supla.android.lib.SuplaChannelExtendedValue;
import org.supla.android.lib.SuplaChannelValue;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.SuplaTimerState;
import org.supla.android.ui.lists.SlideableItem;
import org.supla.android.ui.lists.SwapableListItem;

@AndroidEntryPoint
public class ChannelLayout extends LinearLayout implements SlideableItem, SwapableListItem {

  @Inject ListsEventsManager eventsManager;
  @Inject DurationTimerHelper durationTimerHelper;

  private ChannelBase channelBase;
  private int mFunc;
  private boolean mGroup;

  public String locationCaption;

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

  private float heightScaleFactor = 1f;
  private boolean shouldUpdateChannelStateLayout;

  private Preferences prefs;

  private Listener listener;

  private Disposable changesDisposable = null;

  private TextView durationTimer;
  private CountDownTimer countDownTimer;

  @NonNull
  @Override
  public String getLocationCaption() {
    return locationCaption;
  }

  public interface Listener {

    void onLeftButtonClick(int channelId);

    void onRightButtonClick(int channelId);

    void onCaptionLongPress(int channelId);
  }

  public ChannelLayout(Context context) {
    super(context);
    init(context);
  }

  public ChannelLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public ChannelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public void setChannelListener(Listener l) {
    listener = l;
  }

  public void setInfoIconClickListener(OnClickListener listener) {
    channelStateIcon.setOnClickListener(listener);
  }

  private void init(Context context) {
    prefs = new Preferences(context);
    setOrientation(LinearLayout.HORIZONTAL);

    setBackgroundColor(getResources().getColor(R.color.channel_cell));

    right_btn = new FrameLayout(context);
    left_btn = new FrameLayout(context);

    shouldUpdateChannelStateLayout = true;

    heightScaleFactor = (prefs.getChannelHeight() + 0f) / 100f;
    int channelHeight =
        (int)
            (((float) getResources().getDimensionPixelSize(R.dimen.channel_layout_height))
                * heightScaleFactor);

    right_btn.setLayoutParams(
        new LayoutParams(
            getResources().getDimensionPixelSize(R.dimen.channel_layout_button_width),
            channelHeight));

    right_btn.setBackgroundColor(getResources().getColor(R.color.supla_green));

    left_btn.setLayoutParams(
        new LayoutParams(
            getResources().getDimensionPixelSize(R.dimen.channel_layout_button_width),
            channelHeight));

    left_btn.setBackgroundColor(getResources().getColor(R.color.supla_green));

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

    durationTimer = durationTimerHelper.createTimerView(context);
    content.addView(durationTimer);
    durationTimer.setLayoutParams(
        durationTimerHelper.getTimerViewLayoutParams(
            context, right_onlineStatus.getId(), right_onlineStatus.getId()));

    channelIconContainer = new RelativeLayout(context);
    content.addView(channelIconContainer);
    channelIconContainer.setLayoutParams(getChannelIconContainerLayoutParams());

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
    right_ActiveStatus.setOnlineColor(getResources().getColor(R.color.supla_green));

    {
      int dot_size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);
      RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dot_size / 2, dot_size * 2);

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
    channelIconContainer.addView(caption_text);

    left_btn.setOnClickListener(v -> listener.onLeftButtonClick(channelBase.getRemoteId()));
    right_btn.setOnClickListener(v -> listener.onRightButtonClick(channelBase.getRemoteId()));

    right_onlineStatus.setVisibility(INVISIBLE);
    left_onlineStatus.setVisibility(INVISIBLE);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    observeChanges();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (changesDisposable != null && !changesDisposable.isDisposed()) {
      changesDisposable.dispose();
    }
  }

  private void observeChanges() {
    if (changesDisposable != null && !changesDisposable.isDisposed()) {
      changesDisposable.dispose();
    }

    if (mGroup) {
      changesDisposable =
          eventsManager
              .observeGroup(channelBase.getRemoteId())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribeOn(Schedulers.io())
              .subscribe(this::configureBasedOnData);
    } else {
      changesDisposable =
          eventsManager
              .observeChannel(channelBase.getRemoteId())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribeOn(Schedulers.io())
              .subscribe(this::configureBasedOnData);
    }
  }

  private RelativeLayout.LayoutParams getChannelIconContainerLayoutParams() {
    RelativeLayout.LayoutParams lp;

    lp =
        new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    lp.addRule(RelativeLayout.CENTER_IN_PARENT);

    return lp;
  }

  private TextView newTextView(Context context) {

    TextView tv = new TextView(context);
    tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    tv.setTypeface(SuplaApp.getApp().getTypefaceOpenSansRegular());

    tv.setTextSize(
        TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.channel_btn_text_size));
    tv.setTextColor(getResources().getColor(R.color.channel_btn_text));
    tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

    return tv;
  }

  protected RelativeLayout.LayoutParams getOnlineStatusLayoutParams(boolean right) {

    int dot_size = getResources().getDimensionPixelSize(R.dimen.channel_dot_size);

    RelativeLayout.LayoutParams lp =
        new RelativeLayout.LayoutParams(
            mGroup ? dot_size / 2 : dot_size, mGroup ? dot_size * 2 : dot_size);

    int margin = getResources().getDimensionPixelSize(R.dimen.distance_default);

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
    int margin = getResources().getDimensionPixelSize(R.dimen.distance_default);

    if (mFunc == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) margin = 0;

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
    int margin = getResources().getDimensionPixelSize(R.dimen.distance_default);

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
    result.setOfflineColor(getResources().getColor(R.color.red));
    result.setOnlineColor(getResources().getColor(R.color.supla_green));

    return result;
  }

  public void setLeftBtnText(String Text) {
    left_btn_text.setText(Text);
  }

  public void setRightBtnText(String Text) {
    right_btn_text.setText(Text);
  }

  public void slide(int delta) {

    if (!LeftButtonEnabled && delta > 0) delta = 0;

    if (!RightButtonEnabled && delta < 0) delta = 0;

    content.layout(delta, content.getTop(), content.getWidth() + delta, content.getHeight());

    int bcolor = getResources().getColor(R.color.primary);

    left_btn.setBackgroundColor(bcolor);
    right_btn.setBackgroundColor(bcolor);

    UpdateLeftBtn();
    UpdateRightBtn();
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

  private void UpdateRightBtn() {

    float pr = (content.getLeft() * -1) * 100 / right_btn.getWidth();

    if (pr <= 0) pr = 0;
    else if (pr > 100) pr = 100;

    right_btn.setRotationY(-90 + 90 * pr / 100);

    int left = getWidth() + (content.getLeft() / 2 - right_btn.getWidth() / 2);

    if (content.getLeft() * -1 > right_btn.getWidth()) left = getWidth() - right_btn.getWidth();

    right_btn.layout(left, 0, left + right_btn.getWidth(), right_btn.getHeight());
  }

  public void AnimateToRestingPosition(boolean start_pos) {

    if (!start_pos && Anim) return;

    ObjectAnimator btn_animr = null;
    ObjectAnimator btn_animx = null;
    ObjectAnimator content_animx = null;

    final AnimParams params = new AnimParams();

    params.left_btn_rotation = 90;
    params.left_btn_left = left_btn.getWidth() * -1;
    params.left_btn_right = 0;

    if (content.getLeft() > 0) {

      if (!start_pos && content.getLeft() >= left_btn.getWidth() / 2) {

        params.content_left = left_btn.getWidth();
        params.content_right = getWidth() + left_btn.getWidth();

        btn_animr = ObjectAnimator.ofFloat(left_btn, "RotationY", left_btn.getRotationY(), 0f);
        btn_animx = ObjectAnimator.ofFloat(left_btn, "x", left_btn.getLeft(), 0);
        content_animx =
            ObjectAnimator.ofFloat(content, "x", content.getLeft(), params.content_left);

      } else {

        params.content_left = 0;
        params.content_right = content.getWidth();

        btn_animr = ObjectAnimator.ofFloat(left_btn, "RotationY", left_btn.getRotationY(), 90f);
        btn_animx =
            ObjectAnimator.ofFloat(left_btn, "x", left_btn.getLeft(), left_btn.getWidth() / 2 * -1);
        content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), 0f);
      }

    } else if (content.getLeft() < 0) {

      if (!start_pos && content.getLeft() * -1 >= right_btn.getWidth() / 2) {

        params.content_left = right_btn.getWidth() * -1;
        params.content_right = getWidth() - right_btn.getWidth();

        btn_animr = ObjectAnimator.ofFloat(right_btn, "RotationY", right_btn.getRotationY(), 0f);
        btn_animx =
            ObjectAnimator.ofFloat(right_btn, "x", right_btn.getLeft(), params.content_right);
        content_animx =
            ObjectAnimator.ofFloat(content, "x", content.getLeft(), params.content_left);

      } else {

        params.content_left = 0;
        params.content_right = content.getWidth();

        btn_animr = ObjectAnimator.ofFloat(right_btn, "RotationY", right_btn.getRotationY(), -90f);
        btn_animx =
            ObjectAnimator.ofFloat(
                right_btn, "x", right_btn.getLeft(), getWidth() + right_btn.getWidth() / 2);
        content_animx = ObjectAnimator.ofFloat(content, "x", content.getLeft(), 0f);
      }
    }

    if (content_animx != null) {

      AnimatorSet as = new AnimatorSet();
      as.playTogether(btn_animr, btn_animx, content_animx);
      as.setDuration(200);

      as.addListener(
          new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
              Anim = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

              content.setTranslationX(0);
              content.layout(
                  params.content_left, content.getTop(), params.content_right, getWidth());

              left_btn.setTranslationX(0);
              right_btn.setTranslationX(0);
              UpdateLeftBtn();
              UpdateRightBtn();
              Anim = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
              onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
          });

      as.start();
    }
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

  public String getCaption() {
    return caption_text.getText().toString();
  }

  public void setBackgroundColor(int color) {

    super.setBackgroundColor(color);

    if (content != null) content.setBackgroundColor(color);
  }

  public ChannelBase getChannelBase() {
    return channelBase;
  }

  public void setChannelData(ChannelBase cbase) {
    configureBasedOnData(cbase);
    observeChanges();
  }

  public void setLocationCaption(String locationCaption) {
    this.locationCaption = locationCaption;
  }

  private void configureBasedOnData(ChannelBase cbase) {
    int OldFunc = mFunc;
    mFunc = cbase.getFunc();
    boolean OldGroup = mGroup;
    mGroup = cbase instanceof ChannelGroup;

    imgl.setImage(
        cbase.getImageIdx(ChannelBase.WhichOne.First),
        cbase.getImageIdx(ChannelBase.WhichOne.Second));

    imgl.setText1(cbase.getHumanReadableValue());
    imgl.setText2(cbase.getHumanReadableValue(ChannelBase.WhichOne.Second));

    channelStateIcon.setVisibility(INVISIBLE);
    channelWarningIcon.setChannel(cbase);

    boolean isMeasurementChannel =
        !mGroup
            && (((Channel) cbase).getValue().getSubValueType()
                    == SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
                || ((Channel) cbase).getValue().getSubValueType()
                    == SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS);
    boolean wasMeasurementChannel =
        !mGroup
            && channelBase != null
            && (((Channel) channelBase).getValue().getSubValueType()
                    == SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
                || ((Channel) channelBase).getValue().getSubValueType()
                    == SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS);

    if (OldFunc != mFunc || isMeasurementChannel != wasMeasurementChannel) {
      imgl.SetDimensions();
      shouldUpdateChannelStateLayout = true;
    }

    channelBase = cbase;

    {
      SuplaChannelStatus.ShapeType shapeType =
          mGroup ? SuplaChannelStatus.ShapeType.LinearVertical : SuplaChannelStatus.ShapeType.Dot;

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

    if (mGroup && (activePercent = ((ChannelGroup) cbase).getActivePercent()) >= 0) {
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
        if (shouldUpdateChannelStateLayout) {
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
        case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
        case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
        case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
          ridx = R.string.channel_btn_on;
          lidx = R.string.channel_btn_off;
          break;

        case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
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
        case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
        case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
        case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
        case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
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
        case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
        case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT:
        case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
          left_onlineStatus.setVisibility(View.VISIBLE);
          left_onlineStatus.setShapeType(SuplaChannelStatus.ShapeType.Ring);
          right_onlineStatus.setVisibility(View.VISIBLE);
          right_onlineStatus.setShapeType(SuplaChannelStatus.ShapeType.Ring);

          break;

        case SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER:
        case SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER:
        case SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER:
        case SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER:
        case SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER:
        case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
        case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL:
        case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL:
          left_onlineStatus.setVisibility(View.INVISIBLE);
          right_onlineStatus.setVisibility(View.VISIBLE);
          break;

        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
          lenabled = true;
          renabled = true;

          left_onlineStatus.setVisibility(View.INVISIBLE);
          right_onlineStatus.setVisibility(View.VISIBLE);
          break;

        default:
          left_onlineStatus.setVisibility(View.INVISIBLE);
          right_onlineStatus.setVisibility(View.INVISIBLE);
          break;
      }

      setLeftButtonEnabled(lenabled && cbase.getOnLine());
      setRightButtonEnabled(renabled && cbase.getOnLine());
    }
    caption_text.setText(cbase.getNotEmptyCaption(getContext()));

    caption_text.setOnLongClickListener(
        v -> {
          listener.onCaptionLongPress(channelBase.getRemoteId());
          return true;
        });
    caption_text.setClickable(false);
    caption_text.setLongClickable(true);

    setupTimer(cbase);
  }

  private void setupTimer(ChannelBase cbase) {
    if (!(cbase instanceof Channel)) {
      return;
    }

    if (countDownTimer != null) {
      countDownTimer.cancel();
      durationTimer.setVisibility(GONE);
    }

    ChannelExtendedValue extendedValue = ((Channel) cbase).getExtendedValue();
    if (extendedValue == null) {
      return;
    }
    SuplaChannelExtendedValue suplaExtendedValue = extendedValue.getExtendedValue();
    if (suplaExtendedValue == null) {
      return;
    }
    SuplaTimerState timerState = suplaExtendedValue.TimerStateValue;
    if (timerState == null) {
      return;
    }
    Date endsAt = timerState.getCountdownEndsAt();
    if (endsAt == null) {
      return;
    }
    Date now = new Date();
    if (endsAt.before(now)) {
      return;
    }
    Long leftTime = endsAt.getTime() - now.getTime();

    durationTimer.setVisibility(VISIBLE);
    countDownTimer =
        new CountDownTimer(leftTime, 100) {
          @Override
          public void onTick(long millisUntilFinished) {
            durationTimer.setText(durationTimerHelper.formatMillis(millisUntilFinished));
          }

          @Override
          public void onFinish() {
            countDownTimer = null;
            durationTimer.setVisibility(GONE);
          }
        };
    countDownTimer.start();
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
      if (heightScaleFactor > 1.0) textSize *= heightScaleFactor;
      setTypeface(SuplaApp.getApp().getTypefaceOpenSansBold());
      setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
      setTextColor(getResources().getColor(R.color.channel_caption_text));
      setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

      RelativeLayout.LayoutParams lp =
          new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

      if (imgl_id != -1) lp.addRule(RelativeLayout.BELOW, imgl_id);

      lp.topMargin =
          (int)
              (getResources().getDimensionPixelSize(R.dimen.channel_caption_top_margin)
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
      mFunc = 0;
      mOldFunc = 0;
      Img1 = newImageView(context);
      Text1 = newTextView(context);

      Img2 = newImageView(context);
      Text2 = newTextView(context);

      configureSubviews();
      SetDimensions();
    }

    private void configureSubviews() {
      removeAllViews();
      if (mFunc == SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR) {
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
      return (int) (dim * heightScaleFactor);
    }

    private TextView newTextView(Context context) {

      AppCompatTextView Text = new AppCompatTextView(context);
      Text.setId(ViewHelper.generateViewId());

      Text.setTypeface(SuplaApp.getApp().getTypefaceOpenSansRegular());

      float textSize = getResources().getDimension(R.dimen.channel_imgtext_size);
      float sts = scaledDimension((int) textSize);
      textSize = Math.max(sts, textSize);
      Text.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

      Text.setMaxLines(1);

      Text.setTextColor(getResources().getColor(R.color.channel_imgtext_color));
      Text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

      return Text;
    }

    private void SetTextDimensions(TextView Text, ImageView Img, Boolean visible) {
      int h = getResources().getDimensionPixelSize(R.dimen.channel_img_height);
      int sh = scaledDimension(h);

      boolean empty = Text.getText().length() == 0;

      Text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, sh);

      int textMargin =
          empty ? 0 : getResources().getDimensionPixelSize(R.dimen.channel_imgtext_leftmargin);
      lp.setMargins(textMargin, 0, 0, 0);
      Text.setLayoutParams(lp);
      Text.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void SetImgDimensions(ImageView Img, int width, int height) {
      int sw = scaledDimension(width), sh = scaledDimension(height);

      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sw, sh);

      if (Img == Img2) {
        int textMargin = getResources().getDimensionPixelSize(R.dimen.channel_imgtext_leftmargin);
        lp.setMargins(2 * textMargin, 0, 0, 0);
      }

      Img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      Img.setLayoutParams(lp);
    }

    private void SetImgDimensions(ImageView Img) {
      SetImgDimensions(
          Img,
          getResources().getDimensionPixelSize(R.dimen.channel_img_width),
          getResources().getDimensionPixelSize(R.dimen.channel_img_height));
    }

    private void SetDimensions() {
      if (mOldFunc != mFunc) {
        mOldFunc = mFunc;
        configureSubviews();
      }
      setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

      int h = getResources().getDimensionPixelSize(R.dimen.channel_img_height),
          sh = scaledDimension(h);

      RelativeLayout.LayoutParams lp =
          new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, sh);

      lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

      setLayoutParams(lp);

      if (mFunc == SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR) {

        int sdw, sdh, dh, dw;

        dw = getResources().getDimensionPixelSize(R.dimen.channel_distanceimg_width);
        dh = getResources().getDimensionPixelSize(R.dimen.channel_distanceimg_height);
        sdw = scaledDimension(dw);
        sdh = scaledDimension(dh);

        LinearLayout.LayoutParams _lp = new LinearLayout.LayoutParams(sdw, sdh > dh ? sdh : dh);
        Img1.setLayoutParams(_lp);
        Img1.setVisibility(View.VISIBLE);

        _lp =
            new LinearLayout.LayoutParams(
                scaledDimension(
                    getResources().getDimensionPixelSize(R.dimen.channel_distanceimgtext_width)),
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

      if (ImageId.equals(img1Id, Img1Id) && ImageId.equals(img2Id, Img2Id)) {
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
}
