package org.supla.android.scenes;

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

import java.util.Date;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.CountDownTimer;
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
import org.supla.android.db.Scene;
import org.supla.android.images.ImageCache;
import org.supla.android.images.ImageId;
import org.supla.android.listview.LineView;

public class SceneLayout extends LinearLayout implements View.OnLongClickListener {


  private boolean buttonSliding = false;
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
  private TextView sceneDurationTimer;
  private CountDownTimer sceneCountDown;
  private LineView bottom_line;
  private Handler uiThreadHandler;

  private boolean Anim;

  private boolean RightButtonEnabled;
  private boolean LeftButtonEnabled;
  private boolean DetailSliderEnabled;

  private float heightScaleFactor = 1f;
  private boolean shouldUpdateChannelStateLayout;
    
  private Preferences prefs;
  private float LastXtouch = -1;
  private float LastYtouch = -1;
  private Listener listener;
  private final String TIMER_INACTIVE = "--:--:--";

  public interface Listener {
    void onLeftButtonClick(SceneLayout l);
    void onRightButtonClick(SceneLayout l);
    void onButtonSlide(SceneLayout l);
    void onMove(SceneLayout l);
  }


  public SceneLayout(Context context) {
    super(context);

    init(context);
  }

  private void init(Context context) {
    uiThreadHandler = new Handler(Looper.getMainLooper());
    prefs = new Preferences(context);
    setOrientation(LinearLayout.HORIZONTAL);

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

    sceneDurationTimer = newTimerView(context);
    content.addView(sceneDurationTimer);
    RelativeLayout.LayoutParams sdlp = new RelativeLayout
      .LayoutParams((int)getResources().getDimension(R.dimen.channel_imgtext_width),
                    (int)(getResources().getDimension(R.dimen.default_text_size)
                    * 1.5));
    sdlp.addRule(RelativeLayout.ABOVE, right_onlineStatus.getId());
    sdlp.addRule(RelativeLayout.ALIGN_RIGHT, right_onlineStatus.getId());
    sdlp.setMargins(0, 0, 0,
                    (int)getResources().getDimension(R.dimen.form_element_spacing));
    sceneDurationTimer.setLayoutParams(sdlp);
    sceneDurationTimer.setText(TIMER_INACTIVE);
                                                                       

    channelIconContainer = new RelativeLayout(context);
    content.addView(channelIconContainer);
    channelIconContainer
      .setLayoutParams(getChannelIconContainerLayoutParams());

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

  }


  public SceneLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public SceneLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public void setSceneListener(Listener l) {
    listener = l;
  }

  private RelativeLayout.LayoutParams getChannelIconContainerLayoutParams() {
    RelativeLayout.LayoutParams lp;

    lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                         RelativeLayout.LayoutParams.WRAP_CONTENT);
    lp.addRule(RelativeLayout.CENTER_IN_PARENT);

    return lp;
  }

  private TextView newTimerView(Context context) {

    TextView tv = new TextView(context);
    tv.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                   getResources().getDimension(R.dimen.default_text_size));
    tv.setTextColor(getResources().getColor(R.color.label_grey));
    tv.setGravity(Gravity.BOTTOM | Gravity.RIGHT);

    return tv;
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

    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dot_size, dot_size);

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

  public void cloneSlide(SceneLayout other) {
    int offset = other.content.getLeft();
    if(offset == 0) {
      return;
    }

    uiThreadHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          Slide(offset);
        }
      }, 50);
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
    if(up) {
      if(prefs.isButtonAutohide()) {
        AnimateToRestingPosition(true);
      }
      if(v == left_btn) {
        listener.onLeftButtonClick(this);
      } else {
        listener.onRightButtonClick(this);
      }

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

    if(!start_pos) {
      listener.onButtonSlide(this);
    }

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

    

  public void setScene(Scene scn) {
    int[] standardIcons = {
      R.drawable.scene0, R.drawable.scene1,
      R.drawable.scene2, R.drawable.scene3,
      R.drawable.scene4, R.drawable.scene5,
      R.drawable.scene6, R.drawable.scene7,
      R.drawable.scene8, R.drawable.scene9,
      R.drawable.scene10, R.drawable.scene11,
      R.drawable.scene12, R.drawable.scene13,
      R.drawable.scene14, R.drawable.scene15,
      R.drawable.scene16, R.drawable.scene17,
      R.drawable.scene18, R.drawable.scene19 };

    int iconId = scn.getAltIcon();
    if(iconId == 0) {
      iconId = scn.getUserIcon();
    }
    
    ImageId imgId;
    if(iconId < standardIcons.length) {
      imgId = new ImageId(standardIcons[iconId]);
    } else {
      imgId = new ImageId(iconId, 0);
    }

    imgl.setImage(imgId);


    setRightBtnText(getResources().getString(R.string.btn_execute));
    setLeftBtnText(getResources().getString(R.string.btn_abort));

    SuplaChannelStatus.ShapeType state;

    if(scn.isExecuting()) {
      Date ssd = scn.getStartedAt(), eed = scn.getEstimatedEndDate();
      long sceneDuration = eed.getTime() - ssd.getTime();
      sceneCountDown = new CountDownTimer(sceneDuration, 1000) {
          private long duration = sceneDuration;
          public void onTick(long millisUntilFinished) {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                  duration -= 1000;
                  sceneDurationTimer.setText(formatMillis(duration));
                }
              });
          }
          
          public void onFinish() {
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                  sceneDurationTimer.setText(TIMER_INACTIVE);
                }
              });
          }
        };
      sceneCountDown.start();
      state = SuplaChannelStatus.ShapeType.Dot;
    } else {
      state = SuplaChannelStatus.ShapeType.Ring;
    }
      

    left_onlineStatus.setShapeType(state);
    right_onlineStatus.setShapeType(state);

    setLeftButtonEnabled(true);
    setRightButtonEnabled(true);


    caption_text.setText(scn.getCaption());

  }

  private String formatMillis(long v) {
    long r = v, k = 0;
    StringBuilder sb = new StringBuilder();

    k = r / 3600000;
    sb.append(String.format("%02d:", k));
    r -= k * 3600000;
    k = r / 60000;
    sb.append(String.format("%02d:", k));
    r -= k * 60000;
    sb.append(String.format("%02d", r / 1000));

    return sb.toString();
  }

  @Override
  public boolean onLongClick(View v) {
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
    private ImageId Img1Id;

    private float heightScaleFactor = 1f;

    public ChannelImageLayout(Context context, float heightScaleFactor) {
      super(context);


      this.heightScaleFactor = heightScaleFactor;

      setId(ViewHelper.generateViewId());
      Img1 = newImageView(context);

      configureSubviews();
      SetDimensions();
    }

    private void configureSubviews() {
      removeAllViews();
      setOrientation(LinearLayout.HORIZONTAL);
      addView(Img1);
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


    private void SetImgDimensions(ImageView Img, int width, int height) {
      int sw = scaledDimension(width),
        sh = scaledDimension(height);

      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sw, sh);


      Img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      Img.setLayoutParams(lp);

    }

    private void SetImgDimensions(ImageView Img) {
      SetImgDimensions(Img,
                       getResources().getDimensionPixelSize(R.dimen.channel_img_width),
                       getResources().getDimensionPixelSize(R.dimen.channel_img_height));
    }

    private void SetDimensions() {
      setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

      int h = getResources().getDimensionPixelSize(R.dimen.channel_img_height),
        sh = scaledDimension(h);

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                                                                       LayoutParams.WRAP_CONTENT, sh);
            
      lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            
      setLayoutParams(lp);

      SetImgDimensions(Img1);
    }

    public void setImage(ImageId img1Id) {


      Img1Id = img1Id;

      if (Img1Id == null) {
        Img1.setVisibility(View.GONE);
      } else {
        Img1.setImageBitmap(ImageCache.getBitmap(getContext(), img1Id));
        Img1.setVisibility(View.VISIBLE);
      }
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



  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    int action = ev.getAction();
    float X = ev.getX();
    float Y = ev.getY();

    float deltaY = Math.abs(Y - LastYtouch);
    float deltaX = Math.abs(X - LastXtouch);

    if(action == MotionEvent.ACTION_DOWN) {
      LastXtouch = X;
      LastYtouch = Y;
      buttonSliding = false;
      return true;
    } else if(action == MotionEvent.ACTION_MOVE) {
      if(!Sliding() && deltaY >= deltaX) {
        return super.onTouchEvent(ev);
      }

      if(X != LastXtouch) {
        Slide((int)(X - LastXtouch));
        buttonSliding = true;
        listener.onMove(this);
      }
      LastXtouch = X;
      LastYtouch = Y;
      if(Sliding()) {
        return true;
      }
    } else if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
      AnimateToRestingPosition(!buttonSliding);
    }
    return super.onTouchEvent(ev);
  }
}
